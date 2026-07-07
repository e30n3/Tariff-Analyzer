package org.ivanzaytsev.tariffanalyzer.presentation.navigation

sealed interface AppRoute {
    data object Configuration : AppRoute
    data object MessageAnalysis : AppRoute
    data object Settings : AppRoute
}
