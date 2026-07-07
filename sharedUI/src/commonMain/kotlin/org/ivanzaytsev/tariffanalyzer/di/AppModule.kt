package org.ivanzaytsev.tariffanalyzer.di

import com.russhwolf.settings.Settings
import org.ivanzaytsev.tariffanalyzer.data.repository.settings.MultiplatformSettingsRepository
import org.ivanzaytsev.tariffanalyzer.data.repository.MockTariffRepository
import org.ivanzaytsev.tariffanalyzer.domain.repository.SettingsRepository
import org.ivanzaytsev.tariffanalyzer.domain.repository.TariffRepository
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GetTariffsUseCase
import org.koin.dsl.module

val appModule = module {
    single { Settings() }
    single<SettingsRepository> { MultiplatformSettingsRepository(get()) }
    single<TariffRepository> { MockTariffRepository() }
    factory { GetTariffsUseCase(get()) }
}
