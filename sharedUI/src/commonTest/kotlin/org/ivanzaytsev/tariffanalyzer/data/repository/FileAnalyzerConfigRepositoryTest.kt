package org.ivanzaytsev.tariffanalyzer.data.repository

import kotlinx.coroutines.test.runTest
import org.ivanzaytsev.tariffanalyzer.data.config.AnalyzerConfigFileStorage
import org.ivanzaytsev.tariffanalyzer.data.csv.CsvFileReader
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssueSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FileAnalyzerConfigRepositoryTest {

    @Test
    fun missingConfigReturnsMissingStatus() = runTest {
        val repository = repository(FakeConfigFileStorage())

        val result = repository.getConfigStatus()

        assertEquals(ConfigStatus.Missing, result.status)
        assertEquals(null, result.configPath)
        assertTrue(result.issues.isEmpty())
    }

    @Test
    fun generateConfigWritesParsedTemplatesAndTariffs() = runTest {
        val storage = FakeConfigFileStorage()
        val repository = repository(storage)

        val result = repository.generateConfig(
            templatesFile = file("message_templates.csv", AnalyzerFilePurpose.MessageTemplates),
            tariffFile = file("tariff.csv", AnalyzerFilePurpose.Tariff),
        )

        assertEquals(ConfigStatus.Valid, result.status)
        assertEquals(storage.configPath, result.configPath)
        assertNotNull(storage.text)
        assertTrue(storage.text.orEmpty().contains("\"templates\""))
        assertTrue(storage.text.orEmpty().contains("\"tariffs\""))
        assertTrue(storage.text.orEmpty().contains("\"id\": \"39324\""))
        assertTrue(storage.text.orEmpty().contains("\"operator\": \"mts\""))
        assertTrue(storage.text.orEmpty().contains("\"trafficType\": \"Сервисный\""))
        assertTrue(storage.text.orEmpty().contains("\"priceWithVat\": \"2.710000000000\""))
        assertTrue(result.issues.isEmpty())
    }

    @Test
    fun invalidJsonReturnsInvalidStatusWithError() = runTest {
        val repository = repository(FakeConfigFileStorage(text = "{"))

        val result = repository.validateConfig()

        assertEquals(ConfigStatus.Invalid, result.status)
        assertTrue(result.issues.any { it.severity == ValidationIssueSeverity.Error })
    }

    @Test
    fun jsonWithoutRequiredSectionsReturnsInvalidStatus() = runTest {
        val repository = repository(FakeConfigFileStorage(text = """{"templates": []}"""))

        val result = repository.validateConfig()

        assertEquals(ConfigStatus.Invalid, result.status)
        assertTrue(result.issues.any { it.location == "\$.tariffs" })
    }

    @Test
    fun jsonWithRequiredSectionsReturnsValidStatus() = runTest {
        val repository = repository(FakeConfigFileStorage(text = """{"templates": [], "tariffs": []}"""))

        val result = repository.validateConfig()

        assertEquals(ConfigStatus.Valid, result.status)
        assertTrue(result.issues.isEmpty())
    }

    private fun repository(
        storage: FakeConfigFileStorage,
        csvFileReader: CsvFileReader = FakeCsvFileReader(),
    ): FileAnalyzerConfigRepository = FileAnalyzerConfigRepository(storage, csvFileReader)

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
        override val configPath: String = "/tmp/tariff-analyzer-config.json",
        var text: String? = null,
    ) : AnalyzerConfigFileStorage {
        override suspend fun exists(): Boolean = text != null

        override suspend fun readText(): String = text.orEmpty()

        override suspend fun writeText(text: String) {
            this.text = text
        }
    }

    private class FakeCsvFileReader : CsvFileReader {
        override suspend fun readWindows1251Text(path: String): String =
            if (path.endsWith("message_templates.csv")) {
                """
                "ID";"Текст шаблона";"Имя отправителя";"Типы трафика"
                "39324";"код %d";"OTP_Bank";"СМС:mts:Сервисный, СМС:beeline:Сервисный"
                """.trimIndent()
            } else {
                """
                "Описание";"Количество";"Цена (руб. с НДС)";"Стоимость (руб. с НДС)"
                "Оплата трафика по услуге ""Пропуск sms-трафика"" beeline Сервисный (по шкале от 1)";"5000";"2.710000000000";"13550"
                """.trimIndent()
            }
    }
}
