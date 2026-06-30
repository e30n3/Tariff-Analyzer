package org.ivanzaytsev.tariffanalyzer.data.repository

import kotlinx.coroutines.delay
import org.ivanzaytsev.tariffanalyzer.domain.model.Tariff
import org.ivanzaytsev.tariffanalyzer.domain.repository.TariffRepository
import kotlin.time.Duration.Companion.milliseconds

class MockTariffRepository : TariffRepository {

    override suspend fun getTariffs(): List<Tariff> {
        delay(800.milliseconds) // simulate network latency
        return mockTariffs
    }

    private companion object {
        val mockTariffs = listOf(
            Tariff(
                id = "1",
                name = "Start",
                provider = "MTS",
                monthlyPrice = 9.99,
                dataGb = 5,
                callMinutes = 300,
            ),
            Tariff(
                id = "2",
                name = "Smart",
                provider = "Beeline",
                monthlyPrice = 14.99,
                dataGb = 20,
                callMinutes = 600,
            ),
            Tariff(
                id = "3",
                name = "Premium",
                provider = "Megafon",
                monthlyPrice = 24.99,
                dataGb = 50,
                callMinutes = 1500,
            ),
            Tariff(
                id = "4",
                name = "Unlimited",
                provider = "Tele2",
                monthlyPrice = 34.99,
                dataGb = 200,
                callMinutes = 3000,
            ),
        )
    }
}
