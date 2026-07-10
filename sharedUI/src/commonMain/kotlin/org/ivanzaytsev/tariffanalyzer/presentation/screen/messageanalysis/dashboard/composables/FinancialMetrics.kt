package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.ivanzaytsev.tariffanalyzer.designsystem.components.MetricCard
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalysisSummary
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.formatCount
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.formatRubles

@Composable
fun FinancialMetrics(summary: AnalysisSummary) {
    val differenceContext = when (summary.costDifference.signum()) {
        1 -> "Потенциальная переплата"
        -1 -> "Потенциальная недоплата"
        else -> "Стоимость совпадает"
    }
    val differenceContainer: Color
    val differenceContent: Color
    when (summary.costDifference.signum()) {
        1 -> {
            differenceContainer = MaterialTheme.colorScheme.errorContainer
            differenceContent = MaterialTheme.colorScheme.onErrorContainer
        }
        -1 -> {
            differenceContainer = MaterialTheme.colorScheme.tertiaryContainer
            differenceContent = MaterialTheme.colorScheme.onTertiaryContainer
        }
        else -> {
            differenceContainer = MaterialTheme.colorScheme.primaryContainer
            differenceContent = MaterialTheme.colorScheme.onPrimaryContainer
        }
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val isWide = maxWidth >= 820.dp
        val layoutModifier = Modifier.fillMaxWidth()
        if (isWide) {
            Row(
                modifier = layoutModifier,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MetricCard(
                    label = "Расчёт оператора",
                    value = summary.currentCost.total.formatRubles(),
                    supportingText = "Рассчитано ${summary.currentCost.pricedRows.formatCount()} из ${summary.processedRows.formatCount()} SMS",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                MetricCard(
                    label = "Правильная стоимость",
                    value = summary.correctCost.total.formatRubles(),
                    supportingText = "Рассчитано ${summary.correctCost.pricedRows.formatCount()} из ${summary.processedRows.formatCount()} SMS",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                MetricCard(
                    label = "Разница",
                    value = summary.costDifference.formatRubles(),
                    supportingText = "$differenceContext · сравнимо ${summary.comparableRows.formatCount()} SMS",
                    modifier = Modifier.weight(1f),
                    containerColor = differenceContainer,
                    contentColor = differenceContent,
                )
            }
        } else {
            Column(
                modifier = layoutModifier,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricCard(
                    label = "Расчёт оператора",
                    value = summary.currentCost.total.formatRubles(),
                    supportingText = "Рассчитано ${summary.currentCost.pricedRows.formatCount()} из ${summary.processedRows.formatCount()} SMS",
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                MetricCard(
                    label = "Правильная стоимость",
                    value = summary.correctCost.total.formatRubles(),
                    supportingText = "Рассчитано ${summary.correctCost.pricedRows.formatCount()} из ${summary.processedRows.formatCount()} SMS",
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                MetricCard(
                    label = "Разница",
                    value = summary.costDifference.formatRubles(),
                    supportingText = "$differenceContext · сравнимо ${summary.comparableRows.formatCount()} SMS",
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = differenceContainer,
                    contentColor = differenceContent,
                )
            }
        }
    }
}
