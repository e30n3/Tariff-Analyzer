package org.ivanzaytsev.tariffanalyzer.domain.analyzer

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TariffRange
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TariffRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TariffCalculatorTest {

    @Test
    fun usesAllOperatorFallbackWhenExactOperatorIsMissing() {
        val calculator = TariffCalculator(
            listOf(tariff(operator = "all", trafficType = "Рекламный", price = "4.43")),
        )

        val result = calculator.priceFor("tele2", "Рекламный", TariffScenario.Current)

        assertIs<TariffPriceResult.Found>(result)
        assertEquals("4.43", result.priceWithVat)
    }

    @Test
    fun doesNotAliasIdentificationToAuthorization() {
        val calculator = TariffCalculator(
            listOf(tariff(operator = "tele2", trafficType = "Авторизационный", price = "1.90")),
        )

        val result = calculator.priceFor("tele2", "Идентификационный", TariffScenario.Current)

        assertIs<TariffPriceResult.NotFound>(result)
    }

    @Test
    fun currentAndCorrectScenariosHaveSeparateCounters() {
        val calculator = TariffCalculator(
            listOf(
                tariff(operator = "tele2", trafficType = "Сервисный", price = "1", from = 1, to = 1),
                tariff(operator = "tele2", trafficType = "Сервисный", price = "2", from = 2, to = 2),
            ),
        )

        assertEquals("1", (calculator.priceFor("tele2", "Сервисный", TariffScenario.Current) as TariffPriceResult.Found).priceWithVat)
        assertEquals("1", (calculator.priceFor("tele2", "Сервисный", TariffScenario.Correct) as TariffPriceResult.Found).priceWithVat)
        assertEquals("2", (calculator.priceFor("tele2", "Сервисный", TariffScenario.Current) as TariffPriceResult.Found).priceWithVat)
    }

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
