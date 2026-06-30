package org.ivanzaytsev.tariffanalyzer.di

import org.ivanzaytsev.tariffanalyzer.data.repository.MockTariffRepository
import org.ivanzaytsev.tariffanalyzer.domain.repository.TariffRepository
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GetTariffsUseCase
import org.koin.dsl.module

val appModule = module {
    single<TariffRepository> { MockTariffRepository() }
    factory { GetTariffsUseCase(get()) }
}
