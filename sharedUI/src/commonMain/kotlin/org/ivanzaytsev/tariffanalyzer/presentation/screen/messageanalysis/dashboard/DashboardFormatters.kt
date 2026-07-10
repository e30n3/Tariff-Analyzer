package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.DecimalAmount
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingIssueKind

internal fun DecimalAmount.formatRubles(): String {
    val rounded = toRoundedPlainString(2)
    val negative = rounded.startsWith('-')
    val unsigned = rounded.removePrefix("-")
    val integer = unsigned.substringBefore('.')
    val fraction = unsigned.substringAfter('.', "00")
    val grouped = integer.reversed().chunked(3).joinToString(" ").reversed()
    return buildString {
        if (negative) append('−')
        append(grouped)
        append(',')
        append(fraction)
        append(" ₽")
    }
}

internal fun Long.formatCount(): String {
    val negative = this < 0
    val grouped = toString().removePrefix("-").reversed().chunked(3).joinToString(" ").reversed()
    return if (negative) "−$grouped" else grouped
}

internal fun Long.formatPercent(total: Long): String {
    if (total <= 0L) return "0%"
    val percent = this.toDouble() * 100.0 / total.toDouble()
    val rounded = (percent * 10.0).toLong() / 10.0
    return if (rounded % 1.0 == 0.0) {
        "${rounded.toLong()}%"
    } else {
        "${rounded.toString().replace('.', ',')}%"
    }
}

internal fun ProcessingIssueKind.displayName(): String = when (this) {
    ProcessingIssueKind.TemplateNotFound -> "Шаблон не найден"
    ProcessingIssueKind.OperatorTrafficMappingNotFound -> "Нет типа для оператора"
    ProcessingIssueKind.TemplateConflict -> "Конфликт шаблонов"
    ProcessingIssueKind.CurrentTariffNotFound -> "Нет тарифа текущего типа"
    ProcessingIssueKind.CorrectTariffNotFound -> "Нет тарифа правильного типа"
}
