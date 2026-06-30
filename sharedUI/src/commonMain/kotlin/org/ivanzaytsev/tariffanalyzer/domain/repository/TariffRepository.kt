package org.ivanzaytsev.tariffanalyzer.domain.repository

import org.ivanzaytsev.tariffanalyzer.domain.model.Tariff

interface TariffRepository {
    suspend fun getTariffs(): List<Tariff>
}
