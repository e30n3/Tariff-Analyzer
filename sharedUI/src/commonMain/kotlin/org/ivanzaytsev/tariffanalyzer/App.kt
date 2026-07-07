package org.ivanzaytsev.tariffanalyzer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
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
                transitionSpec = appScreenTransitionSpec(),
                popTransitionSpec = appScreenTransitionSpec(),
                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeLast()
                    }
                },
                entryProvider = { route ->
                    NavEntry(
                        key = route,
                        contentKey = route.navigationContentKey,
                    ) {
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

private fun appScreenTransitionSpec(): AnimatedContentTransitionScope<Scene<AppRoute>>.() -> ContentTransform =
    {
        val initialContentKey = initialState.entries.lastOrNull()?.contentKey as? String
        val targetContentKey = targetState.entries.lastOrNull()?.contentKey as? String
        val direction = targetContentKey.navigationOrder.compareTo(initialContentKey.navigationOrder)

        screenVerticalTransition(direction)
    }

private fun screenVerticalTransition(direction: Int): ContentTransform {
    val animationSpec = tween<IntOffset>(durationMillis = 320)
    val fadeSpec = tween<Float>(durationMillis = 220)
    val enterOffset: (Int) -> Int = { fullHeight -> fullHeight / 2 }
    val exitOffset: (Int) -> Int = { fullHeight -> fullHeight / 4 }

    return ContentTransform(
        targetContentEnter = slideInVertically(animationSpec) { fullHeight ->
            when {
                direction > 0 -> enterOffset(fullHeight)
                direction < 0 -> -enterOffset(fullHeight)
                else -> 0
            }
        } + fadeIn(fadeSpec),
        initialContentExit = slideOutVertically(animationSpec) { fullHeight ->
            when {
                direction > 0 -> -exitOffset(fullHeight)
                direction < 0 -> exitOffset(fullHeight)
                else -> 0
            }
        } + fadeOut(fadeSpec),
    )
}

private val AppRoute.navigationContentKey: String
    get() = when (this) {
        AppRoute.Settings -> "settings"
        AppRoute.Configuration -> "configuration"
        AppRoute.MessageAnalysis -> "message_analysis"
    }

private val String?.navigationOrder: Int
    get() = when (this) {
        AppRoute.Settings.navigationContentKey -> 0
        AppRoute.Configuration.navigationContentKey -> 1
        AppRoute.MessageAnalysis.navigationContentKey -> 2
        else -> 1
    }

private data class AppNavigationRailItem(
    val route: AppRoute,
    val icon: ImageVector,
    val label: String,
)
