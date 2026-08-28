package org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ivanzaytsev.tariffanalyzer.data.repository.InMemoryAnalyzerConfigRepository
import org.ivanzaytsev.tariffanalyzer.domain.analyzer.AnalysisSummaryAccumulator
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerConfig
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessMessagesRequest
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingUpdate
import org.ivanzaytsev.tariffanalyzer.domain.model.ThemeMode
import org.ivanzaytsev.tariffanalyzer.domain.repository.MessageAnalysisFileProcessor
import org.ivanzaytsev.tariffanalyzer.domain.repository.SettingsRepository
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GenerateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.LoadConfigStatusUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ProcessMessagesUseCase
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisContract
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class MessageAnalysisViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun processingProgressCompletesWithOutputPaths() = runTest(dispatcher) {
        val repository = InMemoryAnalyzerConfigRepository()
        generateConfig(repository)
        val viewModel = createViewModel(repository, debugMode = true)
        advanceUntilIdle()
        viewModel.onAction(MessageAnalysisContract.Action.ChooseMessagesCsv(file("full_msg.csv", AnalyzerFilePurpose.Messages)))

        viewModel.onAction(MessageAnalysisContract.Action.StartProcessing)
        advanceTimeBy(700.milliseconds)
        advanceUntilIdle()

        assertEquals(ConfigStatus.Valid, viewModel.state.value.configStatus)
        assertEquals(MessageAnalysisContract.ProcessingStatus.Completed, viewModel.state.value.processingStatus)
        assertEquals(1f, viewModel.state.value.progressFraction)
        assertNotNull(viewModel.state.value.outputCsvPath)
        assertNotNull(viewModel.state.value.logPath)
        assertNotNull(viewModel.state.value.summary)
    }

    @Test
    fun processingCanBeCancelled() = runTest(dispatcher) {
        val repository = InMemoryAnalyzerConfigRepository()
        generateConfig(repository)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onAction(MessageAnalysisContract.Action.ChooseMessagesCsv(file("full_msg.csv", AnalyzerFilePurpose.Messages)))

        viewModel.onAction(MessageAnalysisContract.Action.StartProcessing)
        advanceTimeBy(130.milliseconds)
        viewModel.onAction(MessageAnalysisContract.Action.CancelProcessing)
        advanceUntilIdle()

        assertEquals(MessageAnalysisContract.ProcessingStatus.Cancelled, viewModel.state.value.processingStatus)
        assertEquals(null, viewModel.state.value.outputCsvPath)
    }

    @Test
    fun newAnalysisClearsCompletedDashboardState() = runTest(dispatcher) {
        val repository = InMemoryAnalyzerConfigRepository()
        generateConfig(repository)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onAction(MessageAnalysisContract.Action.ChooseMessagesCsv(file("full_msg.csv", AnalyzerFilePurpose.Messages)))
        viewModel.onAction(MessageAnalysisContract.Action.StartProcessing)
        advanceUntilIdle()

        viewModel.onAction(MessageAnalysisContract.Action.StartNewAnalysis)

        assertEquals(MessageAnalysisContract.ProcessingStatus.Idle, viewModel.state.value.processingStatus)
        assertEquals(null, viewModel.state.value.selectedMessagesFile)
        assertEquals(null, viewModel.state.value.summary)
        assertEquals(null, viewModel.state.value.outputCsvPath)
        assertEquals(null, viewModel.state.value.logPath)
    }

    @Test
    fun dashboardSettingIsReflectedInState() = runTest(dispatcher) {
        val repository = InMemoryAnalyzerConfigRepository()
        val settingsRepository = FakeSettingsRepository(dashboardEnabled = false)
        val viewModel = createViewModel(repository, settingsRepository = settingsRepository)

        assertEquals(false, viewModel.state.value.isDashboardEnabled)

        settingsRepository.setDashboardEnabled(true)
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isDashboardEnabled)
    }

    private fun createViewModel(
        repository: InMemoryAnalyzerConfigRepository,
        debugMode: Boolean = false,
        settingsRepository: SettingsRepository = FakeSettingsRepository(debugMode = debugMode),
    ): MessageAnalysisViewModel =
        MessageAnalysisViewModel(
            loadConfigStatusUseCase = LoadConfigStatusUseCase(repository),
            processMessagesUseCase = ProcessMessagesUseCase(repository, FakeMessageAnalysisFileProcessor()),
            settingsRepository = settingsRepository,
        )

    private suspend fun generateConfig(repository: InMemoryAnalyzerConfigRepository) {
        GenerateConfigUseCase(repository)(
            templatesFile = file("message_templates.csv", AnalyzerFilePurpose.MessageTemplates),
            tariffFile = file("tariff.csv", AnalyzerFilePurpose.Tariff),
        )
    }

    private fun file(
        name: String,
        purpose: AnalyzerFilePurpose,
    ): AnalyzerFileReference = AnalyzerFileReference(
        name = name,
        path = "/tmp/$name",
        sizeBytes = 128,
        purpose = purpose,
    )

    private class FakeMessageAnalysisFileProcessor : MessageAnalysisFileProcessor {
        override fun process(
            request: ProcessMessagesRequest,
            config: AnalyzerConfig,
        ): Flow<ProcessingUpdate> = flow {
            val totalRowsHint = 1_000L
            for (step in 1..5) {
                delay(120.milliseconds)
                emit(
                    ProcessingUpdate.Progress(
                        processedRows = step * 200L,
                        totalRowsHint = totalRowsHint,
                        progressFraction = step / 5f,
                    ),
                )
            }
            emit(
                ProcessingUpdate.Completed(
                    processedRows = totalRowsHint,
                    outputCsvPath = "/tmp/full_msg_analyzed.csv",
                    logPath = "/tmp/full_msg_processing.log".takeIf { request.debugMode },
                    summary = AnalysisSummaryAccumulator().build(),
                ),
            )
        }
    }

    private class FakeSettingsRepository(
        debugMode: Boolean = false,
        dashboardEnabled: Boolean = true,
    ) : SettingsRepository {
        private val mutableThemeMode = MutableStateFlow(ThemeMode.System)
        private val mutableDebugMode = MutableStateFlow(debugMode)
        private val mutableDashboardEnabled = MutableStateFlow(dashboardEnabled)
        override val themeMode: StateFlow<ThemeMode> = mutableThemeMode
        override val debugMode: StateFlow<Boolean> = mutableDebugMode
        override val dashboardEnabled: StateFlow<Boolean> = mutableDashboardEnabled

        override fun setThemeMode(mode: ThemeMode) {
            mutableThemeMode.value = mode
        }

        override fun setDebugMode(enabled: Boolean) {
            mutableDebugMode.value = enabled
        }

        override fun setDashboardEnabled(enabled: Boolean) {
            mutableDashboardEnabled.value = enabled
        }
    }
}
