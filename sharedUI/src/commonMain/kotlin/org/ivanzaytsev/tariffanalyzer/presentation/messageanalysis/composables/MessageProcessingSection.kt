package org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis.MessageAnalysisContract
import org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis.MessageAnalysisContract.ProcessingStatus
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.FilePickerTile

@Composable
fun MessageProcessingSection(
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
