package org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis.MessageAnalysisContract
import org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis.MessageAnalysisContract.ProcessingStatus

@Composable
fun ProcessingProgress(state: MessageAnalysisContract.State) {
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
