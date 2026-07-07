package org.ivanzaytsev.tariffanalyzer.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.ivanzaytsev.tariffanalyzer.domain.model.ThemeMode

interface SettingsRepository {
    val themeMode: StateFlow<ThemeMode>

    fun setThemeMode(mode: ThemeMode)
}
