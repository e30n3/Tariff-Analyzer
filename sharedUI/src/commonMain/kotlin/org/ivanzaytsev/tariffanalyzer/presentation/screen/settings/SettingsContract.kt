package org.ivanzaytsev.tariffanalyzer.presentation.screen.settings

import org.ivanzaytsev.tariffanalyzer.domain.model.ThemeMode

interface SettingsContract {

    data class State(
        val selectedThemeMode: ThemeMode = ThemeMode.System,
        val isDashboardEnabled: Boolean = true,
        val isDebugModeEnabled: Boolean = false,
    )

    sealed interface Action {
        data class SelectThemeMode(val mode: ThemeMode) : Action
        data class SetDashboardEnabled(val enabled: Boolean) : Action
        data class SetDebugMode(val enabled: Boolean) : Action
    }

    sealed interface Effect
}
