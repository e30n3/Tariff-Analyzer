package org.ivanzaytsev.tariffanalyzer.di

import com.russhwolf.settings.Settings
import org.ivanzaytsev.tariffanalyzer.data.repository.settings.MultiplatformSettingsRepository
import org.ivanzaytsev.tariffanalyzer.data.repository.MockTariffRepository
import org.ivanzaytsev.tariffanalyzer.domain.repository.SettingsRepository
import org.ivanzaytsev.tariffanalyzer.domain.repository.TariffRepository
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GenerateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GetTariffsUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.LoadConfigStatusUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ProcessMessagesUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ValidateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.presentation.analyzer.AnalyzerViewModel
import org.ivanzaytsev.tariffanalyzer.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { Settings() }
    single<SettingsRepository> { MultiplatformSettingsRepository(get()) }
    single<TariffRepository> { MockTariffRepository() }
    factory { GetTariffsUseCase(get()) }
    factory { LoadConfigStatusUseCase() }
    factory { GenerateConfigUseCase() }
    factory { ValidateConfigUseCase() }
    factory { ProcessMessagesUseCase() }
    viewModelOf(::AnalyzerViewModel)
    viewModelOf(::SettingsViewModel)
}
