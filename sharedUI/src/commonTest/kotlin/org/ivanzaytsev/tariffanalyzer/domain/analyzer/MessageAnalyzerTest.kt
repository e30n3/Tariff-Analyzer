package org.ivanzaytsev.tariffanalyzer.domain.analyzer

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerConfig
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerOutputColumns
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.MessageTemplateRule
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TariffRange
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TariffRule
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TrafficMapping
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MessageAnalyzerTest {

    @Test
    fun matchesTemplateIgnoringCaseAndPunctuation() {
        val analyzer = MessageAnalyzer(
            AnalyzerConfig(
                templates = listOf(template(id = "1", text = "код %d", trafficType = "Сервисный")),
                tariffs = listOf(
                    tariff(operator = "tele2", trafficType = "Рекламный", price = "4.43"),
                    tariff(operator = "tele2", trafficType = "Сервисный", price = "1.90"),
                ),
            ),
        )

        val result = analyzer.analyze(
            row(
                sender = "OTP Bank",
                text = "Код: 1234!",
                currentType = "Рекламный",
                operator = "t2",
            ),
        )

        assertEquals("Сервисный", result.additionalValuesByColumn[AnalyzerOutputColumns.CORRECT_TYPE])
        assertEquals("да", result.additionalValuesByColumn[AnalyzerOutputColumns.TYPE_MISMATCH])
        assertEquals("определен", result.additionalValuesByColumn[AnalyzerOutputColumns.TEMPLATE_DEFINED])
        assertEquals("без конфликта", result.additionalValuesByColumn[AnalyzerOutputColumns.TEMPLATE_CONFLICT])
    }

    @Test
    fun conflictUsesFirstTemplateAndWritesConflictFlag() {
        val analyzer = MessageAnalyzer(
            AnalyzerConfig(
                templates = listOf(
                    template(id = "first", text = "код %d", trafficType = "Сервисный"),
                    template(id = "second", text = "код %d", trafficType = "Транзакционный"),
                ),
                tariffs = listOf(
                    tariff(operator = "tele2", trafficType = "Рекламный", price = "4.43"),
                    tariff(operator = "tele2", trafficType = "Сервисный", price = "1.90"),
                ),
            ),
        )

        val result = analyzer.analyze(row(text = "код 1234", currentType = "Рекламный"))

        assertEquals("Сервисный", result.additionalValuesByColumn[AnalyzerOutputColumns.CORRECT_TYPE])
        assertEquals("конфликт", result.additionalValuesByColumn[AnalyzerOutputColumns.TEMPLATE_CONFLICT])
        assertTrue(result.logEntries.any { it.contains("first") && it.contains("second") })
    }

    @Test
    fun unsupportedTemplateOperandFailsCompilation() {
        assertFailsWith<IllegalStateException> {
            TemplatePatternCompiler.compile(template(id = "bad", text = "код %w+"))
        }
    }

    @Test
    fun missingTariffLeavesPriceEmptyAndWritesError() {
        val analyzer = MessageAnalyzer(
            AnalyzerConfig(
                templates = listOf(template(id = "1", text = "код %d", trafficType = "Идентификационный")),
                tariffs = listOf(tariff(operator = "tele2", trafficType = "Авторизационный", price = "1.90")),
            ),
        )

        val result = analyzer.analyze(row(text = "код 1234", currentType = "Идентификационный"))

        assertEquals("", result.additionalValuesByColumn[AnalyzerOutputColumns.CORRECT_TYPE_PRICE])
        assertTrue(result.additionalValuesByColumn[AnalyzerOutputColumns.PROCESSING_ERRORS].orEmpty().contains("Тариф не найден"))
    }

    private fun row(
        sender: String = "OTP Bank",
        text: String,
        currentType: String,
        operator: String = "tele2",
    ): MessageCsvRow = MessageCsvRow(
        csvLineNumber = 2,
        valuesByColumn = mapOf(
            "Имя отправителя" to sender,
            "Текст SMS" to text,
            "Тип трафика" to currentType,
            "Оператор/направление" to operator,
        ),
    )

    private fun template(
        id: String,
        text: String,
        trafficType: String = "Сервисный",
    ): MessageTemplateRule = MessageTemplateRule(
        id = id,
        text = text,
        senderName = "OTP Bank",
        trafficMappings = listOf(
            TrafficMapping(
                channel = "СМС",
                operator = "tele2",
                trafficType = trafficType,
                sourceValue = "СМС:tele2:$trafficType",
            ),
        ),
    )

    private fun tariff(
        operator: String,
        trafficType: String,
        price: String,
        from: Long = 1,
        to: Long = 100,
    ): TariffRule = TariffRule(
        operator = operator,
        trafficType = trafficType,
        priceWithVat = price,
        quantity = to - from + 1,
        range = TariffRange(from = from, to = to),
    )
}
