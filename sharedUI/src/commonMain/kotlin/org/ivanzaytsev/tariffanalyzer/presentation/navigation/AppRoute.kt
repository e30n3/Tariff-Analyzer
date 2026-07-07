package org.ivanzaytsev.tariffanalyzer.presentation.navigation

sealed interface AppRoute {
    data object Analyzer : AppRoute
    data object Settings : AppRoute
}
