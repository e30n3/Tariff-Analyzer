package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables

import androidx.compose.runtime.Composable
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.composables.RevealResultFileButton
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.ResultRow

@Composable
fun OutputFilesSection(
    outputCsvPath: String?,
    logPath: String?,
    onOpenOutputFolder: () -> Unit,
) {
    DashboardSection {
        AnalyzerSectionHeader(
            title = "Файлы результата",
            description = if (logPath == null) {
                "CSV сохранён в кодировке Windows-1251. Дополнительные debug-файлы отключены."
            } else {
                "CSV сохранён в Windows-1251 и UTF-8; лог содержит диагностику строк."
            },
        )
        ResultRow(label = "CSV", value = outputCsvPath ?: "Путь недоступен")
        if (logPath != null) {
            ResultRow(label = "Лог", value = logPath)
        }
        if (outputCsvPath != null) {
            RevealResultFileButton(onClick = onOpenOutputFolder)
        }
    }
}
