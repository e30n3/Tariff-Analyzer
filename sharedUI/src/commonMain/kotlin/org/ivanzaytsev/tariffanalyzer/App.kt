package org.ivanzaytsev.tariffanalyzer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import org.ivanzaytsev.tariffanalyzer.designsystem.theme.TariffAnalyzerTheme
import org.ivanzaytsev.tariffanalyzer.domain.model.ThemeMode
import org.ivanzaytsev.tariffanalyzer.domain.repository.SettingsRepository
import org.ivanzaytsev.tariffanalyzer.presentation.analyzer.AnalyzerScreen
import org.ivanzaytsev.tariffanalyzer.presentation.navigation.AppRoute
import org.ivanzaytsev.tariffanalyzer.presentation.settings.SettingsScreen
import org.koin.compose.koinInject

@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) {
    val settingsRepository = koinInject<SettingsRepository>()
    val themeMode by settingsRepository.themeMode.collectAsState(ThemeMode.System)
    val systemIsDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.System -> systemIsDark
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
    }
    val backStack = remember { mutableStateListOf<AppRoute>(AppRoute.Analyzer) }

    TariffAnalyzerTheme(
        isDark = isDark,
        onThemeChanged = onThemeChanged,
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLast()
                }
            },
            entryProvider = { route ->
                NavEntry(route) {
                    when (route) {
                        AppRoute.Analyzer -> AnalyzerScreen(
                            onNavigateToSettings = {
                                if (backStack.lastOrNull() != AppRoute.Settings) {
                                    backStack.add(AppRoute.Settings)
                                }
                            },
                        )

                        AppRoute.Settings -> SettingsScreen(
                            onNavigateBack = {
                                if (backStack.size > 1) {
                                    backStack.removeLast()
                                }
                            },
                        )
                    }
                }
            },
        )
    }
}
