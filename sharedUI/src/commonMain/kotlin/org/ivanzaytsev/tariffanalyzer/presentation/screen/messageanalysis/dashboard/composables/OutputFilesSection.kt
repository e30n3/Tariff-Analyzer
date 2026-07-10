package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables

import androidx.compose.runtime.Composable
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.ResultRow

@Composable
fun OutputFilesSection(
    outputCsvPath: String?,
    logPath: String?,
) {
    DashboardSection {
        AnalyzerSectionHeader(
            title = "Файлы результата",
            description = "CSV сохранён в исходной кодировке и отдельной UTF-8 копии; лог содержит диагностику строк.",
        )
        ResultRow(label = "CSV", value = outputCsvPath ?: "Путь недоступен")
        ResultRow(label = "Лог", value = logPath ?: "Путь недоступен")
    }
}
