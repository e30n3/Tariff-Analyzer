package org.ivanzaytsev.tariffanalyzer.domain.usecase

import org.ivanzaytsev.tariffanalyzer.domain.model.Tariff
import org.ivanzaytsev.tariffanalyzer.domain.repository.TariffRepository

class GetTariffsUseCase(
    private val repository: TariffRepository,
) {
    suspend operator fun invoke(): List<Tariff> =
        repository.getTariffs().sortedBy { it.monthlyPrice }
}
