package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.OperatorAnalysisSummary
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.formatCount
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.formatPercent
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.formatRubles

@Composable
fun OperatorTableRow(
    operator: OperatorAnalysisSummary,
    isWide: Boolean,
) {
    val differenceColor: Color = when (operator.costDifference.signum()) {
        1 -> MaterialTheme.colorScheme.error
        -1 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurface
    }
    if (isWide) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(operator.operator, modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.bodyMedium)
            Text(operator.processedRows.formatCount(), modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
            Text(operator.mismatchRows.formatCount(), modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
            Text(operator.mismatchRows.formatPercent(operator.processedRows), modifier = Modifier.weight(0.6f), textAlign = TextAlign.End)
            Text(
                text = operator.costDifference.formatRubles(),
                modifier = Modifier.weight(1f),
                color = differenceColor,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(operator.operator, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = operator.costDifference.formatRubles(),
                    color = differenceColor,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Text(
                text = "${operator.processedRows.formatCount()} SMS · ${operator.mismatchRows.formatCount()} расхождений (${operator.mismatchRows.formatPercent(operator.processedRows)})",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
