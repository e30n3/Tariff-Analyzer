package org.ivanzaytsev.tariffanalyzer.presentation.navigation

sealed interface AppRoute {
    data object Tariffs : AppRoute
    data object Settings : AppRoute
}
