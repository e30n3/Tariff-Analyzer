package org.ivanzaytsev.tariffanalyzer.presentation.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun AppNavigation() {
    val backStack = remember { mutableStateListOf(AppRoute.Configuration) }

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
        AppNavDisplay(
            backStack = backStack,
            modifier = Modifier.weight(1f),
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLast()
                }
            },
        )
    }
}
