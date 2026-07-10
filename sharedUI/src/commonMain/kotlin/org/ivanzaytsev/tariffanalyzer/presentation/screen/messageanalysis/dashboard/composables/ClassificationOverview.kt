package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.designsystem.components.DonutChart
import org.ivanzaytsev.tariffanalyzer.designsystem.components.DonutChartSegment
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalysisSummary
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.formatCount
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.formatPercent
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection

@Composable
fun ClassificationOverview(
    summary: AnalysisSummary,
    modifier: Modifier = Modifier,
) {
    DashboardSection(modifier) {
        AnalyzerSectionHeader(
            title = "Классификация трафика",
            description = "Сравнение исходного и рассчитанного типов SMS.",
        )
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val chart = @Composable {
                DonutChart(
                    segments = listOf(
                        DonutChartSegment(summary.matchingTypeRows.toFloat(), MaterialTheme.colorScheme.primary),
                        DonutChartSegment(summary.mismatchRows.toFloat(), MaterialTheme.colorScheme.error),
                    ),
                    centerValue = summary.mismatchRows.formatPercent(summary.processedRows),
                    centerLabel = "расхождений",
                    accessibilityDescription = "${summary.mismatchRows} расхождений типов из ${summary.processedRows} SMS",
                )
            }
            val legend = @Composable {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Типы совпадают", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                summary.matchingTypeRows.formatCount(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(10.dp).background(MaterialTheme.colorScheme.error, CircleShape),
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Есть расхождение", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                summary.mismatchRows.formatCount(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                    Text(
                        text = "Правильный тип определён для ${summary.determinedCorrectTypeRows.formatPercent(summary.processedRows)} SMS",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (maxWidth >= 500.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    chart()
                    legend()
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    chart()
                    legend()
                }
            }
        }
    }
}
