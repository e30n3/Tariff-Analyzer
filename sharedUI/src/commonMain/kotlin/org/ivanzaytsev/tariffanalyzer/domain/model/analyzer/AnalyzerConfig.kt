package org.ivanzaytsev.tariffanalyzer.domain.model.analyzer

data class AnalyzerConfig(
    val templates: List<MessageTemplateRule>,
    val tariffs: List<TariffRule>,
)

data class MessageTemplateRule(
    val id: String,
    val text: String,
    val senderName: String,
    val trafficMappings: List<TrafficMapping>,
    val source: AnalyzerSource? = null,
)

data class TrafficMapping(
    val channel: String,
    val operator: String,
    val trafficType: String,
    val sourceValue: String,
)

data class TariffRule(
    val operator: String,
    val trafficType: String,
    val priceWithVat: String,
    val quantity: Long,
    val range: TariffRange,
    val source: AnalyzerSource? = null,
)

data class TariffRange(
    val from: Long,
    val to: Long?,
) {
    fun contains(value: Long): Boolean = value >= from && (to == null || value <= to)
}

data class AnalyzerSource(
    val csvLine: Int?,
    val description: String? = null,
    val rawValue: String? = null,
)

object AnalyzerOutputColumns {
    const val CURRENT_TYPE_PRICE = "стоимость по текущему типу"
    const val CORRECT_TYPE = "правильный тип"
    const val CORRECT_TYPE_PRICE = "стоимость по правильному типу"
    const val TYPE_MISMATCH = "наличие расхождение типа"
    const val TEMPLATE_DEFINED = "определен ли шаблон"
    const val TEMPLATE_CONFLICT = "есть ли конфликт шаблонов"
    const val PROCESSING_ERRORS = "ошибки обработки"

    val all: List<String> = listOf(
        CURRENT_TYPE_PRICE,
        CORRECT_TYPE,
        CORRECT_TYPE_PRICE,
        TYPE_MISMATCH,
        TEMPLATE_DEFINED,
        TEMPLATE_CONFLICT,
        PROCESSING_ERRORS,
    )
}

object AnalyzerInputColumns {
    const val SENDER_NAME = "Имя отправителя"
    const val SMS_TEXT = "Текст SMS"
    const val CURRENT_TRAFFIC_TYPE = "Тип трафика"
    const val OPERATOR = "Оператор/направление"

    val required: List<String> = listOf(
        SENDER_NAME,
        SMS_TEXT,
        CURRENT_TRAFFIC_TYPE,
        OPERATOR,
    )
}
