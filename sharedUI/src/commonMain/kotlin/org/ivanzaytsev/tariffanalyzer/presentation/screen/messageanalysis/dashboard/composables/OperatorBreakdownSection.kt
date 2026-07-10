package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.OperatorAnalysisSummary
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection

@Composable
fun OperatorBreakdownSection(operators: List<OperatorAnalysisSummary>) {
    val topOperators = operators.take(5)
    DashboardSection {
        AnalyzerSectionHeader(
            title = "Операторы с наибольшим влиянием",
            description = "Сортировка по абсолютной разнице стоимости на сравнимых строках.",
        )
        if (topOperators.isEmpty()) {
            Text(
                text = "Нет данных по операторам.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 720.dp
                Column(Modifier.fillMaxWidth()) {
                    if (isWide) OperatorTableHeader()
                    topOperators.forEachIndexed { index, operator ->
                        if (index > 0 || isWide) HorizontalDivider()
                        OperatorTableRow(operator, isWide)
                    }
                }
            }
        }
    }
}
