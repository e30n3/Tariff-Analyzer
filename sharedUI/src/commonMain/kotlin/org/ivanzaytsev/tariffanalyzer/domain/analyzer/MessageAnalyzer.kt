package org.ivanzaytsev.tariffanalyzer.domain.analyzer

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerConfig
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerInputColumns
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerOutputColumns

class MessageAnalyzer(
    config: AnalyzerConfig,
) {
    private val templateMatcher = TemplateMatcher.compile(config.templates)
    private val tariffCalculator = TariffCalculator(config.tariffs)

    fun analyze(row: MessageCsvRow): MessageAnalysisResult {
        val senderName = row.value(AnalyzerInputColumns.SENDER_NAME)
        val smsText = row.value(AnalyzerInputColumns.SMS_TEXT)
        val rawOperator = row.value(AnalyzerInputColumns.OPERATOR)
        val operator = AnalyzerNormalization.normalizeOperator(rawOperator)
        val currentTrafficType = row.value(AnalyzerInputColumns.CURRENT_TRAFFIC_TYPE).trim()
        val errors = mutableListOf<String>()
        val logEntries = mutableListOf<String>()

        val matches = templateMatcher.findMatches(senderName, smsText)
        val selectedTemplate = matches.firstOrNull()
        val selectedMapping = selectedTemplate?.trafficMappings
            ?.firstOrNull { AnalyzerNormalization.normalizeOperator(it.operator) == operator }

        val correctTrafficType = when {
            selectedTemplate == null -> {
                val message = "Шаблон не найден"
                errors.add(message)
                logEntries.add(row.logMessage(message, senderName, rawOperator, currentTrafficType))
                currentTrafficType
            }
            selectedMapping == null -> {
                val message = "Не найден тип трафика для оператора '$rawOperator' в шаблоне '${selectedTemplate.id}'"
                errors.add(message)
                logEntries.add(
                    row.logMessage(
                        message = message,
                        senderName = senderName,
                        operator = rawOperator,
                        currentTrafficType = currentTrafficType,
                        matchedTemplateIds = matches.map { it.id },
                    ),
                )
                currentTrafficType
            }
            else -> selectedMapping.trafficType.trim()
        }

        if (matches.size > 1) {
            val message = "Конфликт шаблонов: ${matches.joinToString { it.id }}"
            errors.add(message)
            logEntries.add(
                row.logMessage(
                    message = message,
                    senderName = senderName,
                    operator = rawOperator,
                    currentTrafficType = currentTrafficType,
                    correctTrafficType = correctTrafficType,
                    matchedTemplateIds = matches.map { it.id },
                ),
            )
        }

        val currentPrice = tariffCalculator.priceFor(operator, currentTrafficType, TariffScenario.Current)
        if (currentPrice is TariffPriceResult.NotFound) {
            val message = "Тариф не найден для текущего типа: оператор '$rawOperator', тип '$currentTrafficType'"
            errors.add(message)
            logEntries.add(row.logMessage(message, senderName, rawOperator, currentTrafficType, correctTrafficType))
        }

        val correctPrice = tariffCalculator.priceFor(operator, correctTrafficType, TariffScenario.Correct)
        if (correctPrice is TariffPriceResult.NotFound) {
            val message = "Тариф не найден для правильного типа: оператор '$rawOperator', тип '$correctTrafficType'"
            errors.add(message)
            logEntries.add(row.logMessage(message, senderName, rawOperator, currentTrafficType, correctTrafficType))
        }

        val additionalValues = listOf(
            priceValue(currentPrice),
            correctTrafficType,
            priceValue(correctPrice),
            if (currentTrafficType != correctTrafficType) "да" else "нет",
            if (selectedTemplate == null) "не определен" else "определен",
            if (matches.size > 1) "конфликт" else "без конфликта",
            errors.distinct().joinToString(" | "),
        )

        return MessageAnalysisResult(
            additionalValuesByColumn = AnalyzerOutputColumns.all.zip(additionalValues).toMap(),
            additionalValues = additionalValues,
            logEntries = logEntries,
        )
    }

    private fun priceValue(result: TariffPriceResult): String = when (result) {
        is TariffPriceResult.Found -> result.priceWithVat
        is TariffPriceResult.NotFound -> ""
    }
}

data class MessageCsvRow(
    val csvLineNumber: Long,
    private val valuesByColumn: Map<String, String>,
) {
    fun value(columnName: String): String = valuesByColumn[columnName].orEmpty()
}

data class MessageAnalysisResult(
    val additionalValuesByColumn: Map<String, String>,
    val additionalValues: List<String>,
    val logEntries: List<String>,
)

private fun MessageCsvRow.logMessage(
    message: String,
    senderName: String,
    operator: String,
    currentTrafficType: String,
    correctTrafficType: String? = null,
    matchedTemplateIds: List<String> = emptyList(),
): String = buildString {
    append("line=").append(csvLineNumber)
    append("; sender=").append(senderName)
    append("; operator=").append(operator)
    append("; currentType=").append(currentTrafficType)
    if (correctTrafficType != null) {
        append("; correctType=").append(correctTrafficType)
    }
    if (matchedTemplateIds.isNotEmpty()) {
        append("; templates=").append(matchedTemplateIds.joinToString(","))
    }
    append("; message=").append(message)
}
