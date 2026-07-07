package org.ivanzaytsev.tariffanalyzer.presentation.configuration

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ivanzaytsev.tariffanalyzer.data.config.AnalyzerConfigFileStorage
import org.ivanzaytsev.tariffanalyzer.data.csv.CsvFileReader
import org.ivanzaytsev.tariffanalyzer.data.repository.FileAnalyzerConfigRepository
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GenerateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.LoadConfigStatusUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ValidateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationContract
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationContract.OperationStatus
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

        viewModel.onAction(
            ConfigurationContract.Action.ChooseTemplatesCsv(
                file(
                    "message_templates.csv",
                    AnalyzerFilePurpose.MessageTemplates
                )
            )
        )
        viewModel.onAction(
            ConfigurationContract.Action.ChooseTariffCsv(
                file(
                    "tariff.csv",
                    AnalyzerFilePurpose.Tariff
                )
            )
        )

        assertTrue(viewModel.state.value.canGenerateConfig)

        viewModel.onAction(ConfigurationContract.Action.GenerateConfig)
        advanceUntilIdle()

        assertEquals(ConfigStatus.Valid, viewModel.state.value.configStatus)
        assertNotNull(viewModel.state.value.configPath)
        assertEquals(OperationStatus.Idle, viewModel.state.value.operationStatus)
        assertTrue(viewModel.state.value.validationIssues.isEmpty())
    }

    @Test
    fun validationUpdatesIssues() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        generateConfig(viewModel)

        viewModel.onAction(ConfigurationContract.Action.ValidateConfig)
        advanceUntilIdle()

        assertEquals(ConfigStatus.Valid, viewModel.state.value.configStatus)
        assertTrue(viewModel.state.value.validationIssues.isEmpty())
    }

    @Test
    fun validationWithErrorIssueMakesConfigInvalid() = runTest(dispatcher) {
        val storage = FakeConfigFileStorage(
            text = """{"templates": []}""",
        )
        val viewModel = createViewModel(storage)
        advanceUntilIdle()

        viewModel.onAction(ConfigurationContract.Action.ValidateConfig)
        advanceUntilIdle()

        assertEquals(ConfigStatus.Invalid, viewModel.state.value.configStatus)
        assertTrue(viewModel.state.value.validationIssues.isNotEmpty())
    }

    @Test
    fun generateWithoutSelectedFilesShowsMessageAndDoesNotStartOperation() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effect = async { viewModel.effect.first() }
        viewModel.onAction(ConfigurationContract.Action.GenerateConfig)
        advanceUntilIdle()

        assertEquals(OperationStatus.Idle, viewModel.state.value.operationStatus)
        assertEquals(
            ConfigurationContract.Effect.ShowMessage("Выберите message_templates.csv и tariff.csv"),
            effect.await(),
        )
    }

    @Test
    fun validateWithoutConfigPathShowsMessageAndDoesNotStartOperation() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effect = async { viewModel.effect.first() }
        viewModel.onAction(ConfigurationContract.Action.ValidateConfig)
        advanceUntilIdle()

        assertEquals(OperationStatus.Idle, viewModel.state.value.operationStatus)
        assertEquals(
            ConfigurationContract.Effect.ShowMessage("Сначала сгенерируйте или загрузите конфигурацию"),
            effect.await(),
        )
    }

    private fun createViewModel(
        storage: FakeConfigFileStorage = FakeConfigFileStorage(),
    ): ConfigurationViewModel {
        val repository = FileAnalyzerConfigRepository(storage, FakeCsvFileReader())
        return ConfigurationViewModel(
            loadConfigStatusUseCase = LoadConfigStatusUseCase(repository),
            generateConfigUseCase = GenerateConfigUseCase(repository),
            validateConfigUseCase = ValidateConfigUseCase(repository),
        )
    }

    private fun TestScope.generateConfig(viewModel: ConfigurationViewModel) {
        viewModel.onAction(
            ConfigurationContract.Action.ChooseTemplatesCsv(
                file(
                    "message_templates.csv",
                    AnalyzerFilePurpose.MessageTemplates
                )
            )
        )
        viewModel.onAction(
            ConfigurationContract.Action.ChooseTariffCsv(
                file(
                    "tariff.csv",
                    AnalyzerFilePurpose.Tariff
                )
            )
        )
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

    private class FakeConfigFileStorage(
        path: String = "/tmp/tariff-analyzer-config.json",
        text: String? = null,
    ) : AnalyzerConfigFileStorage {
        private var currentText: String? = text

        override val configPath: String = path

        override suspend fun exists(): Boolean = currentText != null

        override suspend fun readText(): String = currentText.orEmpty()

        override suspend fun writeText(text: String) {
            currentText = text
        }
    }

    private class FakeCsvFileReader : CsvFileReader {
        override suspend fun readWindows1251Text(path: String): String =
            if (path.endsWith("message_templates.csv")) {
                """
                "ID";"Текст шаблона";"Имя отправителя";"Типы трафика"
                "39324";"код %d";"OTP_Bank";"СМС:mts:Сервисный"
                """.trimIndent()
            } else {
                """
                "Описание";"Количество";"Цена (руб. с НДС)";"Стоимость (руб. с НДС)"
                "Оплата трафика по услуге ""Пропуск sms-трафика"" mts Сервисный (по шкале от 1)";"5000";"3.000000000000";"15000"
                """.trimIndent()
            }
    }
}
