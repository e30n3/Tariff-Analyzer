package org.ivanzaytsev.tariffanalyzer.presentation.analyzer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GenerateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.LoadConfigStatusUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ProcessMessagesUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ValidateConfigUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyzerViewModelTest {

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
    fun initialStateWithoutConfig() = runTest(dispatcher) {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(ConfigStatus.Missing, viewModel.state.value.configStatus)
        assertEquals(null, viewModel.state.value.configPath)
        assertTrue(viewModel.state.value.validationIssues.isEmpty())
    }

    @Test
    fun selectingFilesAllowsConfigGeneration() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(AnalyzerContract.Action.ChooseTemplatesCsv(file("message_templates.csv", AnalyzerFilePurpose.MessageTemplates)))
        viewModel.onAction(AnalyzerContract.Action.ChooseTariffCsv(file("tariff.csv", AnalyzerFilePurpose.Tariff)))

        assertTrue(viewModel.state.value.canGenerateConfig)

        viewModel.onAction(AnalyzerContract.Action.GenerateConfig)
        advanceUntilIdle()

        assertEquals(ConfigStatus.Valid, viewModel.state.value.configStatus)
        assertNotNull(viewModel.state.value.configPath)
        assertTrue(viewModel.state.value.validationIssues.isNotEmpty())
    }

    @Test
    fun validationUpdatesIssues() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        generateConfig(viewModel)

        viewModel.onAction(AnalyzerContract.Action.ValidateConfig)
        advanceUntilIdle()

        assertEquals(ConfigStatus.Valid, viewModel.state.value.configStatus)
        assertEquals(1, viewModel.state.value.validationIssues.size)
    }

    @Test
    fun processingProgressCompletesWithOutputPaths() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        generateConfig(viewModel)
        viewModel.onAction(AnalyzerContract.Action.ChooseMessagesCsv(file("full_msg.csv", AnalyzerFilePurpose.Messages)))

        viewModel.onAction(AnalyzerContract.Action.StartProcessing)
        advanceTimeBy(700.milliseconds)
        advanceUntilIdle()

        assertEquals(AnalyzerContract.ProcessingStatus.Completed, viewModel.state.value.processingStatus)
        assertEquals(1f, viewModel.state.value.progressFraction)
        assertNotNull(viewModel.state.value.outputCsvPath)
        assertNotNull(viewModel.state.value.logPath)
    }

    @Test
    fun processingCanBeCancelled() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        generateConfig(viewModel)
        viewModel.onAction(AnalyzerContract.Action.ChooseMessagesCsv(file("full_msg.csv", AnalyzerFilePurpose.Messages)))

        viewModel.onAction(AnalyzerContract.Action.StartProcessing)
        advanceTimeBy(130.milliseconds)
        viewModel.onAction(AnalyzerContract.Action.CancelProcessing)
        advanceUntilIdle()

        assertEquals(AnalyzerContract.ProcessingStatus.Cancelled, viewModel.state.value.processingStatus)
        assertEquals(null, viewModel.state.value.outputCsvPath)
    }

    private fun createViewModel(): AnalyzerViewModel = AnalyzerViewModel(
        loadConfigStatusUseCase = LoadConfigStatusUseCase(),
        generateConfigUseCase = GenerateConfigUseCase(),
        validateConfigUseCase = ValidateConfigUseCase(),
        processMessagesUseCase = ProcessMessagesUseCase(),
    )

    private fun TestScope.generateConfig(viewModel: AnalyzerViewModel) {
        viewModel.onAction(AnalyzerContract.Action.ChooseTemplatesCsv(file("message_templates.csv", AnalyzerFilePurpose.MessageTemplates)))
        viewModel.onAction(AnalyzerContract.Action.ChooseTariffCsv(file("tariff.csv", AnalyzerFilePurpose.Tariff)))
        viewModel.onAction(AnalyzerContract.Action.GenerateConfig)
        advanceUntilIdle()
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
}
