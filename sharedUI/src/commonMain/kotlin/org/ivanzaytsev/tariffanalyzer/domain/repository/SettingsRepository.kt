package org.ivanzaytsev.tariffanalyzer.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.ivanzaytsev.tariffanalyzer.domain.model.ThemeMode

interface SettingsRepository {
    val themeMode: StateFlow<ThemeMode>
    val debugMode: StateFlow<Boolean>
    val dashboardEnabled: StateFlow<Boolean>

    fun setThemeMode(mode: ThemeMode)

    fun setDebugMode(enabled: Boolean)

    fun setDashboardEnabled(enabled: Boolean)
}
