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
import androidx.compose.ui.unit.dp
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TrafficTypeTransitionSummary
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.formatCount
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection

@Composable
fun TypeTransitionsSection(transitions: List<TrafficTypeTransitionSummary>) {
    val topTransitions = transitions.take(5)
    val maxRows = topTransitions.maxOfOrNull { it.rows } ?: 0L
    DashboardSection {
        AnalyzerSectionHeader(
            title = "Основные расхождения типов",
            description = "Пять самых частых переходов от типа оператора к правильному типу.",
        )
        if (topTransitions.isEmpty()) {
            Text(
                text = "Расхождений типов не обнаружено.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                topTransitions.forEach { transition ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "${transition.currentType} → ${transition.correctType}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = transition.rows.formatCount(),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        LinearProgressIndicator(
                            progress = { if (maxRows == 0L) 0f else transition.rows.toFloat() / maxRows.toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        )
                    }
                }
            }
        }
    }
}
