package org.ivanzaytsev.tariffanalyzer.data.processing

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerConfig
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerOutputColumns
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.MessageTemplateRule
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessMessagesRequest
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingUpdate
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TariffRange
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TariffRule
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TrafficMapping
import org.ivanzaytsev.tariffanalyzer.domain.repository.createMessageAnalysisFileProcessor
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JvmMessageAnalysisFileProcessorTest {

    @Test
    fun debugModeWritesAnalyzedCsvUtf8CopyAndProcessingLog() = runTest {
        val tempDir = Files.createTempDirectory("tariff-analyzer-test").toFile()
        val previousAppDir = System.getProperty("compose.application.dir")
        System.setProperty("compose.application.dir", tempDir.absolutePath)
        try {
            val input = File(tempDir, "messages.csv")
            input.writeText(
                """
                Наименование учётной записи;Имя отправителя;Текст SMS;Тип трафика;Оператор/направление
                0;OTP Bank;Код: 1234!;Рекламный;t2
                """.trimIndent(),
                WINDOWS_1251,
            )

            val updates = createMessageAnalysisFileProcessor()
                .process(
                    request = ProcessMessagesRequest(
                        messagesFile = AnalyzerFileReference(
                            name = input.name,
                            path = input.absolutePath,
                            sizeBytes = input.length(),
                            purpose = AnalyzerFilePurpose.Messages,
                        ),
                        debugMode = true,
                    ),
                    config = config(),
                )
                .toList()

            val completed = updates.filterIsInstance<ProcessingUpdate.Completed>().single()
            val output = File(completed.outputCsvPath).readText(WINDOWS_1251)
            val utf8OutputFile = File(completed.outputCsvPath.substringBeforeLast(".csv") + "_utf8.csv")
            val utf8Output = utf8OutputFile.readText(Charsets.UTF_8)
            val log = File(completed.logPath).readText()

            assertEquals(1L, completed.processedRows)
            assertEquals(1L, completed.summary.processedRows)
            assertEquals("4.43", completed.summary.currentCost.total.toPlainString())
            assertEquals("1.90", completed.summary.correctCost.total.toPlainString())
            assertEquals("2.53", completed.summary.costDifference.toPlainString())
            assertEquals(1L, completed.summary.mismatchRows)
            assertTrue(utf8OutputFile.isFile)
            assertEquals(output, utf8Output)
            assertTrue(output.lines().first().contains(AnalyzerOutputColumns.PROCESSING_ERRORS))
            assertTrue(output.contains(";4.43;Сервисный;1.90;да;определен;без конфликта;"))
            assertTrue(log.contains("Tariff Analyzer processing log"))
        } finally {
            if (previousAppDir == null) {
                System.clearProperty("compose.application.dir")
            } else {
                System.setProperty("compose.application.dir", previousAppDir)
            }
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun defaultModeWritesOnlyWindows1251Csv() = runTest {
        val tempDir = Files.createTempDirectory("tariff-analyzer-test").toFile()
        val previousAppDir = System.getProperty("compose.application.dir")
        System.setProperty("compose.application.dir", tempDir.absolutePath)
        try {
            val input = File(tempDir, "messages.csv")
            input.writeText(
                """
                Наименование учётной записи;Имя отправителя;Текст SMS;Тип трафика;Оператор/направление
                0;OTP Bank;Код: 1234!;Рекламный;t2
                """.trimIndent(),
                WINDOWS_1251,
            )

            val completed = createMessageAnalysisFileProcessor()
                .process(
                    request = ProcessMessagesRequest(
                        messagesFile = AnalyzerFileReference(
                            name = input.name,
                            path = input.absolutePath,
                            sizeBytes = input.length(),
                            purpose = AnalyzerFilePurpose.Messages,
                        ),
                    ),
                    config = config(),
                )
                .toList()
                .filterIsInstance<ProcessingUpdate.Completed>()
                .single()

            val generatedFiles = tempDir.listFiles().orEmpty().filterNot { it == input }
            assertNull(completed.logPath)
            assertEquals(1, generatedFiles.size)
            assertEquals(File(completed.outputCsvPath), generatedFiles.single())
            assertFalse(generatedFiles.any { it.name.contains("_utf8") || it.extension == "log" })
            assertTrue(File(completed.outputCsvPath).readText(WINDOWS_1251).contains("Сервисный"))
        } finally {
            if (previousAppDir == null) {
                System.clearProperty("compose.application.dir")
            } else {
                System.setProperty("compose.application.dir", previousAppDir)
            }
            tempDir.deleteRecursively()
        }
    }

    private fun config(): AnalyzerConfig = AnalyzerConfig(
        templates = listOf(
            MessageTemplateRule(
                id = "template-1",
                text = "код %d",
                senderName = "OTP Bank",
                trafficMappings = listOf(
                    TrafficMapping(
                        channel = "СМС",
                        operator = "tele2",
                        trafficType = "Сервисный",
                        sourceValue = "СМС:tele2:Сервисный",
                    ),
                ),
            ),
        ),
        tariffs = listOf(
            tariff("tele2", "Рекламный", "4.43"),
            tariff("tele2", "Сервисный", "1.90"),
        ),
    )

    private fun tariff(
        operator: String,
        trafficType: String,
        price: String,
    ): TariffRule = TariffRule(
        operator = operator,
        trafficType = trafficType,
        priceWithVat = price,
        quantity = 100,
        range = TariffRange(from = 1, to = 100),
    )

    private companion object {
        val WINDOWS_1251: Charset = Charset.forName("windows-1251")
    }
}
