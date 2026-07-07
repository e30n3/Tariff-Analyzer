package org.ivanzaytsev.tariffanalyzer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import org.ivanzaytsev.tariffanalyzer.designsystem.theme.TariffAnalyzerTheme
import org.ivanzaytsev.tariffanalyzer.domain.model.ThemeMode
import org.ivanzaytsev.tariffanalyzer.domain.repository.SettingsRepository
import org.ivanzaytsev.tariffanalyzer.presentation.navigation.AppNavigation
import org.koin.compose.koinInject

@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {},
) {
    val settingsRepository = koinInject<SettingsRepository>()
    val themeMode by settingsRepository.themeMode.collectAsState(ThemeMode.System)
    val systemIsDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.System -> systemIsDark
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
    }

    TariffAnalyzerTheme(
        isDark = isDark,
        onThemeChanged = onThemeChanged,
    ) {
        AppNavigation()
    }
}
