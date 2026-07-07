package org.ivanzaytsev.tariffanalyzer.presentation.configuration

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ivanzaytsev.tariffanalyzer.data.repository.InMemoryAnalyzerConfigRepository
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GenerateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.LoadConfigStatusUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ValidateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationContract
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigurationViewModelTest {

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

        viewModel.onAction(ConfigurationContract.Action.ChooseTemplatesCsv(file("message_templates.csv", AnalyzerFilePurpose.MessageTemplates)))
        viewModel.onAction(ConfigurationContract.Action.ChooseTariffCsv(file("tariff.csv", AnalyzerFilePurpose.Tariff)))

        assertTrue(viewModel.state.value.canGenerateConfig)

        viewModel.onAction(ConfigurationContract.Action.GenerateConfig)
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

        viewModel.onAction(ConfigurationContract.Action.ValidateConfig)
        advanceUntilIdle()

        assertEquals(ConfigStatus.Valid, viewModel.state.value.configStatus)
        assertEquals(1, viewModel.state.value.validationIssues.size)
    }

    private fun createViewModel(): ConfigurationViewModel {
        val repository = InMemoryAnalyzerConfigRepository()
        return ConfigurationViewModel(
            loadConfigStatusUseCase = LoadConfigStatusUseCase(repository),
            generateConfigUseCase = GenerateConfigUseCase(repository),
            validateConfigUseCase = ValidateConfigUseCase(repository),
        )
    }

    private fun TestScope.generateConfig(viewModel: ConfigurationViewModel) {
        viewModel.onAction(ConfigurationContract.Action.ChooseTemplatesCsv(file("message_templates.csv", AnalyzerFilePurpose.MessageTemplates)))
        viewModel.onAction(ConfigurationContract.Action.ChooseTariffCsv(file("tariff.csv", AnalyzerFilePurpose.Tariff)))
        viewModel.onAction(ConfigurationContract.Action.GenerateConfig)
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
