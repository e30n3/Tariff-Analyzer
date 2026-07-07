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
    initialState = State(selectedThemeMode = settingsRepository.themeMode.value),
    loggerTag = "SettingsViewModel",
) {

    init {
        viewModelScope.launch {
            settingsRepository.themeMode.collect { mode ->
                setState { it.copy(selectedThemeMode = mode) }
            }
        }
    }

    override fun reduce(action: Action) {
        when (action) {
            is Action.SelectThemeMode -> settingsRepository.setThemeMode(action.mode)
        }
    }
}
