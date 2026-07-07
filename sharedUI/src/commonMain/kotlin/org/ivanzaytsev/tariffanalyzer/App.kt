package org.ivanzaytsev.tariffanalyzer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Fact_check
import com.composables.icons.materialsymbols.rounded.Play_arrow
import com.composables.icons.materialsymbols.rounded.Settings
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import org.ivanzaytsev.tariffanalyzer.designsystem.theme.TariffAnalyzerTheme
import org.ivanzaytsev.tariffanalyzer.domain.model.ThemeMode
import org.ivanzaytsev.tariffanalyzer.domain.repository.SettingsRepository
import org.ivanzaytsev.tariffanalyzer.presentation.configuration.ConfigurationScreen
import org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis.MessageAnalysisScreen
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
    val backStack = remember { mutableStateListOf<AppRoute>(AppRoute.Configuration) }

    TariffAnalyzerTheme(
        isDark = isDark,
        onThemeChanged = onThemeChanged,
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AppNavigationRail(
                selectedRoute = backStack.lastOrNull() ?: AppRoute.Configuration,
                onRouteSelected = { route ->
                    if (backStack.lastOrNull() != route) {
                        backStack.clear()
                        backStack.add(route)
                    }
                },
            )
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.weight(1f),
                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeLast()
                    }
                },
                entryProvider = { route ->
                    NavEntry(route) {
                        when (route) {
                            AppRoute.Configuration -> ConfigurationScreen()
                            AppRoute.MessageAnalysis -> MessageAnalysisScreen()
                            AppRoute.Settings -> SettingsScreen()
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun AppNavigationRail(
    selectedRoute: AppRoute,
    onRouteSelected: (AppRoute) -> Unit,
) {
    NavigationRail(
        header = {
            Text("Hello")
        },
        content = {
            NavigationRailItem(
                selected = selectedRoute == AppRoute.Settings,
                onClick = { onRouteSelected(AppRoute.Settings) },
                icon = {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Settings,
                        contentDescription = null,
                    )
                },
                label = { Text("Настройки") },
            )
            NavigationRailItem(
                selected = selectedRoute == AppRoute.Configuration,
                onClick = { onRouteSelected(AppRoute.Configuration) },
                icon = {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Fact_check,
                        contentDescription = null,
                    )
                },
                label = { Text("Конфиг") },
            )
            NavigationRailItem(
                selected = selectedRoute == AppRoute.MessageAnalysis,
                onClick = { onRouteSelected(AppRoute.MessageAnalysis) },
                icon = {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Play_arrow,
                        contentDescription = null,
                    )
                },
                label = { Text("Анализ") },
            )
        }
    )
}
