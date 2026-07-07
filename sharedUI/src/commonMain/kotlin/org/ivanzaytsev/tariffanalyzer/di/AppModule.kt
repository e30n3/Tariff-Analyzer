package org.ivanzaytsev.tariffanalyzer.di

import com.russhwolf.settings.Settings
import org.ivanzaytsev.tariffanalyzer.data.config.createAnalyzerConfigFileStorage
import org.ivanzaytsev.tariffanalyzer.data.csv.createCsvFileReader
import org.ivanzaytsev.tariffanalyzer.data.repository.FileAnalyzerConfigRepository
import org.ivanzaytsev.tariffanalyzer.data.repository.settings.MultiplatformSettingsRepository
import org.ivanzaytsev.tariffanalyzer.data.repository.MockTariffRepository
import org.ivanzaytsev.tariffanalyzer.domain.repository.AnalyzerConfigRepository
import org.ivanzaytsev.tariffanalyzer.domain.repository.SettingsRepository
import org.ivanzaytsev.tariffanalyzer.domain.repository.TariffRepository
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GenerateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GetTariffsUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.LoadConfigStatusUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ProcessMessagesUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ValidateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationViewModel
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisViewModel
import org.ivanzaytsev.tariffanalyzer.presentation.screen.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { Settings() }
    single { createAnalyzerConfigFileStorage() }
    single { createCsvFileReader() }
    single<AnalyzerConfigRepository> { FileAnalyzerConfigRepository(get(), get()) }
    single<SettingsRepository> { MultiplatformSettingsRepository(get()) }
    single<TariffRepository> { MockTariffRepository() }
    factory { GetTariffsUseCase(get()) }
    factory { LoadConfigStatusUseCase(get()) }
    factory { GenerateConfigUseCase(get()) }
    factory { ValidateConfigUseCase(get()) }
    factory { ProcessMessagesUseCase() }
    viewModelOf(::ConfigurationViewModel)
    viewModelOf(::MessageAnalysisViewModel)
    viewModelOf(::SettingsViewModel)
}
