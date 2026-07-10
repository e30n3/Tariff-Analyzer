package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingIssueKind
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingIssueSeverity
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.displayName
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.formatCount

@Composable
fun IssueBreakdownChart(issueCounts: Map<ProcessingIssueKind, Long>) {
    val nonZeroIssues = issueCounts.filterValues { it > 0L }.toList()
    val maxCount = nonZeroIssues.maxOfOrNull { it.second } ?: 0L

    if (nonZeroIssues.isEmpty()) {
        Text(
            text = "Проблем обработки не обнаружено.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("События по категориям", style = MaterialTheme.typography.titleSmall)
            nonZeroIssues.forEach { (kind, count) ->
                val color: Color = when (kind.severity) {
                    ProcessingIssueSeverity.Error -> MaterialTheme.colorScheme.error
                    ProcessingIssueSeverity.Warning -> MaterialTheme.colorScheme.tertiary
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(kind.displayName(), style = MaterialTheme.typography.bodyMedium)
                        Text(count.formatCount(), style = MaterialTheme.typography.labelLarge)
                    }
                    LinearProgressIndicator(
                        progress = { if (maxCount == 0L) 0f else count.toFloat() / maxCount.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                }
            }
        }
    }
}
