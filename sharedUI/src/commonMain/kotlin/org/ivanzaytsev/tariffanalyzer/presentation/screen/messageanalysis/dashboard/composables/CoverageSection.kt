package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalysisSummary
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection

@Composable
fun CoverageSection(
    summary: AnalysisSummary,
    modifier: Modifier = Modifier,
) {
    DashboardSection(modifier) {
        AnalyzerSectionHeader(
            title = "Покрытие расчёта",
            description = "Какая часть файла вошла в каждый показатель.",
        )
        CoverageMetricRow(
            label = "Стоимость по типу оператора",
            coveredRows = summary.currentCost.pricedRows,
            totalRows = summary.processedRows,
        )
        CoverageMetricRow(
            label = "Стоимость по правильному типу",
            coveredRows = summary.correctCost.pricedRows,
            totalRows = summary.processedRows,
        )
        CoverageMetricRow(
            label = "Обе стоимости сравнимы",
            coveredRows = summary.comparableRows,
            totalRows = summary.processedRows,
        )
        CoverageMetricRow(
            label = "Правильный тип определён",
            coveredRows = summary.determinedCorrectTypeRows,
            totalRows = summary.processedRows,
        )
    }
}
