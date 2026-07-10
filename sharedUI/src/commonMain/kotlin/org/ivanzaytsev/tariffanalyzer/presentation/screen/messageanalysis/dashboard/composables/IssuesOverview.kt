package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.designsystem.components.MetricCard
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalysisSummary
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.formatCount
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.formatPercent
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection

@Composable
fun IssuesOverview(summary: AnalysisSummary) {
    DashboardSection {
        AnalyzerSectionHeader(
            title = "Качество обработки",
            description = "Строка учитывается один раз в каждой severity; ниже показано число отдельных событий.",
        )
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 600.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MetricCard(
                        label = "SMS с ошибками",
                        value = summary.errorAffectedRows.formatCount(),
                        supportingText = summary.errorAffectedRows.formatPercent(summary.processedRows),
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    MetricCard(
                        label = "SMS с предупреждениями",
                        value = summary.warningAffectedRows.formatCount(),
                        supportingText = summary.warningAffectedRows.formatPercent(summary.processedRows),
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        label = "SMS с ошибками",
                        value = summary.errorAffectedRows.formatCount(),
                        supportingText = summary.errorAffectedRows.formatPercent(summary.processedRows),
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    MetricCard(
                        label = "SMS с предупреждениями",
                        value = summary.warningAffectedRows.formatCount(),
                        supportingText = summary.warningAffectedRows.formatPercent(summary.processedRows),
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }
        IssueBreakdownChart(summary.issueCounts)
    }
}
