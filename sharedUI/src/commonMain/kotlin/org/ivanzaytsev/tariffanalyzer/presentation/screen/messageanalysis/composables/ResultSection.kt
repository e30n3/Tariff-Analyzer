package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.composables

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisContract
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.ResultRow

@Composable
fun ResultSection(state: MessageAnalysisContract.State) {
    DashboardSection {
        AnalyzerSectionHeader(
            title = "Результат",
            description = "Пути будущих выходных файлов после обработки.",
        )
        ResultRow(
            label = "CSV",
            value = state.outputCsvPath ?: "Пока не создан",
        )
        if (state.logPath != null) {
            ResultRow(
                label = "Лог",
                value = state.logPath,
            )
        }
        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
