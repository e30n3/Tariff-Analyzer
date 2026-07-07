package org.ivanzaytsev.tariffanalyzer.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationScreen
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisScreen
import org.ivanzaytsev.tariffanalyzer.presentation.screen.settings.SettingsScreen

@Composable
fun AppNavDisplay(
    backStack: SnapshotStateList<AppRoute>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        transitionSpec = appScreenTransitionSpec(),
        popTransitionSpec = appScreenTransitionSpec(),
        onBack = onBack,
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

private val String?.navigationOrder: Int
    get() = AppRoute.entries
        .firstOrNull { route -> route.navigationContentKey == this }
        ?.navigationOrder
        ?: AppRoute.Configuration.navigationOrder
