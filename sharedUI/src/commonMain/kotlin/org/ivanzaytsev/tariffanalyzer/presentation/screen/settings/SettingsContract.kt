package org.ivanzaytsev.tariffanalyzer.presentation.screen.settings

import org.ivanzaytsev.tariffanalyzer.domain.model.ThemeMode

interface SettingsContract {

    data class State(
        val selectedThemeMode: ThemeMode = ThemeMode.System,
    )

    sealed interface Action {
        data class SelectThemeMode(val mode: ThemeMode) : Action
    }

    sealed interface Effect
}
