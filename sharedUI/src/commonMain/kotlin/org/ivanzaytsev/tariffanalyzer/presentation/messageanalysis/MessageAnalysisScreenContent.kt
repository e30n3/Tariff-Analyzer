package org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Cancel
import com.composables.icons.materialsymbols.rounded.Play_arrow
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.AnalyzerContentScaffold
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.FilePickerTile
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.ResultRow
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.ValidationIssuesSection
import org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis.MessageAnalysisContract.Action
import org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis.MessageAnalysisContract.ProcessingStatus

@Composable
fun MessageAnalysisScreenContent(
    state: MessageAnalysisContract.State,
    snackbarHostState: SnackbarHostState,
    onAction: (Action) -> Unit,
) {
    AnalyzerContentScaffold(snackbarHostState) {
        item {
            MessageProcessingSection(
                state = state,
                onMessagesSelected = { onAction(Action.ChooseMessagesCsv(it)) },
                onStart = { onAction(Action.StartProcessing) },
                onCancel = { onAction(Action.CancelProcessing) },
            )
        }
        item {
            ResultSection(state)
        }
        item {
            ValidationIssuesSection(state.validationIssues)
        }
    }
}

@Composable
private fun MessageProcessingSection(
    state: MessageAnalysisContract.State,
    onMessagesSelected: (AnalyzerFileReference) -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit,
) {
    DashboardSection {
        AnalyzerSectionHeader(
            title = "Обработка сообщений",
            description = "Большой CSV выбирается как файл на диске; содержимое не читается в UI.",
        )
        FilePickerTile(
            title = "Файл сообщений CSV",
            file = state.selectedMessagesFile,
            purpose = AnalyzerFilePurpose.Messages,
            onFilePicked = onMessagesSelected,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ElevatedButton(
                enabled = state.canStartProcessing,
                onClick = onStart,
            ) {
                Icon(MaterialSymbols.Rounded.Play_arrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Запустить")
            }
            OutlinedButton(
                enabled = state.processingStatus is ProcessingStatus.Running,
                onClick = onCancel,
            ) {
                Icon(MaterialSymbols.Rounded.Cancel, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Отменить")
            }
        }
        ProcessingProgress(state)
    }
}

@Composable
private fun ProcessingProgress(state: MessageAnalysisContract.State) {
    val isRunning = state.processingStatus is ProcessingStatus.Running
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LinearProgressIndicator(
            progress = { state.progressFraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = when (state.processingStatus) {
                ProcessingStatus.Idle -> "Обработка еще не запускалась"
                ProcessingStatus.Running -> "Обработано строк: ${state.processedRows} из ${state.totalRowsHint ?: "?"}"
                ProcessingStatus.Completed -> "Обработка завершена: ${state.processedRows} строк"
                ProcessingStatus.Cancelled -> "Обработка отменена"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResultSection(state: MessageAnalysisContract.State) {
    DashboardSection {
        AnalyzerSectionHeader(
            title = "Результат",
            description = "Пути будущих выходных файлов после обработки.",
        )
        ResultRow(
            label = "CSV",
            value = state.outputCsvPath ?: "Пока не создан",
        )
        ResultRow(
            label = "Лог",
            value = state.logPath ?: "Пока не создан",
        )
        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
