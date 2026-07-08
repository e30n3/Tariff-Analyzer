package org.ivanzaytsev.tariffanalyzer.domain.analyzer

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TariffRule

class TariffCalculator(
    tariffs: List<TariffRule>,
) {
    private val rulesByKey = tariffs
        .groupBy { TariffKey.from(it.operator, it.trafficType) }
        .mapValues { (_, rules) -> rules.sortedBy { it.range.from } }
    private val currentCounters = mutableMapOf<TariffKey, Long>()
    private val correctCounters = mutableMapOf<TariffKey, Long>()

    fun priceFor(
        operator: String,
        trafficType: String,
        scenario: TariffScenario,
    ): TariffPriceResult {
        val exactKey = TariffKey.from(operator, trafficType)
        val counterKey = exactKey
        val sequenceNumber = nextSequenceNumber(counterKey, scenario)
        val rules = rulesByKey[exactKey] ?: rulesByKey[TariffKey.from("all", trafficType)]
        val rule = rules?.firstOrNull { it.range.contains(sequenceNumber) }
        return if (rule == null) {
            TariffPriceResult.NotFound(sequenceNumber)
        } else {
            TariffPriceResult.Found(rule.priceWithVat, sequenceNumber)
        }
    }

    private fun nextSequenceNumber(key: TariffKey, scenario: TariffScenario): Long {
        val counters = when (scenario) {
            TariffScenario.Current -> currentCounters
            TariffScenario.Correct -> correctCounters
        }
        val next = counters.getOrElse(key) { 0L } + 1L
        counters[key] = next
        return next
    }
}

enum class TariffScenario {
    Current,
    Correct,
}

sealed interface TariffPriceResult {
    val sequenceNumber: Long

    data class Found(
        val priceWithVat: String,
        override val sequenceNumber: Long,
    ) : TariffPriceResult

    data class NotFound(
        override val sequenceNumber: Long,
    ) : TariffPriceResult
}

private data class TariffKey(
    val operator: String,
    val trafficType: String,
) {
    companion object {
        fun from(operatorRaw: String, trafficTypeRaw: String): TariffKey = TariffKey(
            operator = AnalyzerNormalization.normalizeOperator(operatorRaw),
            trafficType = AnalyzerNormalization.normalizeTrafficType(trafficTypeRaw),
        )
    }
}
