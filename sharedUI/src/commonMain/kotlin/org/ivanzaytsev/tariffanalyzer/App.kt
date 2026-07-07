package org.ivanzaytsev.tariffanalyzer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Fact_check
import com.composables.icons.materialsymbols.rounded.Menu
import com.composables.icons.materialsymbols.rounded.Menu_open
import com.composables.icons.materialsymbols.rounded.Play_arrow
import com.composables.icons.materialsymbols.rounded.Settings
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppNavigationRail(
    selectedRoute: AppRoute,
    onRouteSelected: (AppRoute) -> Unit,
) {
    val railState = rememberWideNavigationRailState(
        initialValue = WideNavigationRailValue.Collapsed,
    )
    val scope = rememberCoroutineScope()
    val railExpanded = railState.targetValue == WideNavigationRailValue.Expanded
    val navigationItems = remember {
        listOf(
            AppNavigationRailItem(
                route = AppRoute.Settings,
                icon = MaterialSymbols.Rounded.Settings,
                label = "Настройки",
            ),
            AppNavigationRailItem(
                route = AppRoute.Configuration,
                icon = MaterialSymbols.Rounded.Fact_check,
                label = "Конфиг",
            ),
            AppNavigationRailItem(
                route = AppRoute.MessageAnalysis,
                icon = MaterialSymbols.Rounded.Play_arrow,
                label = "Анализ",
            ),
        )
    }

    WideNavigationRail(
        state = railState,
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        colors = WideNavigationRailDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        arrangement = Arrangement.spacedBy(4.dp),
        header = {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            railState.toggle()
                        }
                    },
                ) {
                    AnimatedContent(railExpanded) { isExpanded ->
                        Icon(
                            imageVector =
                                if (isExpanded) MaterialSymbols.Rounded.Menu_open
                                else MaterialSymbols.Rounded.Menu,
                            contentDescription =
                                if (isExpanded) "Свернуть навигацию"
                                else "Развернуть навигацию",
                        )
                    }
                }
                AnimatedVisibility(railExpanded) {
                    Text("Tariff Analyzer")
                }
            }
        },
        content = {
            navigationItems.forEach { item ->
                WideNavigationRailItem(
                    selected = selectedRoute == item.route,
                    onClick = { onRouteSelected(item.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(item.label) },
                    railExpanded = railExpanded,
                )
            }
        }
    )
}

private data class AppNavigationRailItem(
    val route: AppRoute,
    val icon: ImageVector,
    val label: String,
)
