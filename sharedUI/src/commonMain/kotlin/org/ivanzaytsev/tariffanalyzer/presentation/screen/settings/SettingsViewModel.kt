package org.ivanzaytsev.tariffanalyzer.presentation.screen.settings

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.ivanzaytsev.tariffanalyzer.domain.repository.SettingsRepository
import org.ivanzaytsev.tariffanalyzer.presentation.base.BaseViewModel
import org.ivanzaytsev.tariffanalyzer.presentation.screen.settings.SettingsContract.Action
import org.ivanzaytsev.tariffanalyzer.presentation.screen.settings.SettingsContract.Effect
import org.ivanzaytsev.tariffanalyzer.presentation.screen.settings.SettingsContract.State

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : BaseViewModel<State, Action, Effect>(
    initialState = State(
        selectedThemeMode = settingsRepository.themeMode.value,
        isDashboardEnabled = settingsRepository.dashboardEnabled.value,
        isDebugModeEnabled = settingsRepository.debugMode.value,
    ),
    loggerTag = "SettingsViewModel",
) {

    init {
        viewModelScope.launch {
            settingsRepository.themeMode.collect { mode ->
                setState { it.copy(selectedThemeMode = mode) }
            }
        }
        viewModelScope.launch {
            settingsRepository.dashboardEnabled.collect { enabled ->
                setState { it.copy(isDashboardEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.debugMode.collect { enabled ->
                setState { it.copy(isDebugModeEnabled = enabled) }
            }
        }
    }

    override fun reduce(action: Action) {
        when (action) {
            is Action.SelectThemeMode -> settingsRepository.setThemeMode(action.mode)
            is Action.SetDashboardEnabled -> settingsRepository.setDashboardEnabled(action.enabled)
            is Action.SetDebugMode -> settingsRepository.setDebugMode(action.enabled)
        }
    }
}
