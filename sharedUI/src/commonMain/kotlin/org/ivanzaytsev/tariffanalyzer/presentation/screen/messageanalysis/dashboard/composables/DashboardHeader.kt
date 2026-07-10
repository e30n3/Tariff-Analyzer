package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Play_arrow
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.formatCount
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection

@Composable
fun DashboardHeader(
    fileName: String,
    processedRows: Long,
    onStartNewAnalysis: () -> Unit,
) {
    DashboardSection {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val title = @Composable {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Анализ завершён", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "$fileName · ${processedRows.formatCount()} SMS",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            val action = @Composable {
                ElevatedButton(onClick = onStartNewAnalysis) {
                    Icon(MaterialSymbols.Rounded.Play_arrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Новый анализ")
                }
            }
            if (maxWidth >= 620.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    title()
                    action()
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    title()
                    action()
                }
            }
        }
    }
}
