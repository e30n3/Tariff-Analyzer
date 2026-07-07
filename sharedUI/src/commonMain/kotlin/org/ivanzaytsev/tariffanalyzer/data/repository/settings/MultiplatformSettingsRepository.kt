package org.ivanzaytsev.tariffanalyzer.data.repository.settings

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.ivanzaytsev.tariffanalyzer.domain.model.ThemeMode
import org.ivanzaytsev.tariffanalyzer.domain.repository.SettingsRepository

class MultiplatformSettingsRepository(
    private val settings: Settings,
) : SettingsRepository {

    private val _themeMode = MutableStateFlow(readThemeMode())
    override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    override fun setThemeMode(mode: ThemeMode) {
        settings.putString(KEY_THEME_MODE, mode.name)
        _themeMode.value = mode
    }

    private fun readThemeMode(): ThemeMode {
        val savedValue = settings.getStringOrNull(KEY_THEME_MODE)
        return ThemeMode.entries.firstOrNull { it.name == savedValue } ?: ThemeMode.System
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
    }
}
