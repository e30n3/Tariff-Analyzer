package org.ivanzaytsev.tariffanalyzer.presentation.navigation

enum class AppRoute(
    val navigationContentKey: String,
    val navigationOrder: Int,
) {
    Settings(
        navigationContentKey = "settings",
        navigationOrder = 0,
    ),
    Configuration(
        navigationContentKey = "configuration",
        navigationOrder = 1,
    ),
    MessageAnalysis(
        navigationContentKey = "message_analysis",
        navigationOrder = 2,
    ),
}
