package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisContract
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisContract.ProcessingStatus
import kotlin.math.roundToInt

@Composable
fun ProcessingProgress(state: MessageAnalysisContract.State) {
    val isRunning = state.processingStatus is ProcessingStatus.Running
    val progressPercent = (state.progressFraction.coerceIn(0f, 1f) * 100).roundToInt()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LinearProgressIndicator(
            progress = { state.progressFraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = when (state.processingStatus) {
                ProcessingStatus.Idle -> "Обработка еще не запускалась"
                ProcessingStatus.Running -> state.totalRowsHint?.let { totalRowsHint ->
                    "$progressPercent% · Обработано строк: ${state.processedRows} из примерно $totalRowsHint"
                } ?: "$progressPercent% · Подготовка оценки общего количества строк…"
                ProcessingStatus.Completed -> "100% · Обработка завершена: ${state.processedRows} строк"
                ProcessingStatus.Cancelled -> "Обработка отменена"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
