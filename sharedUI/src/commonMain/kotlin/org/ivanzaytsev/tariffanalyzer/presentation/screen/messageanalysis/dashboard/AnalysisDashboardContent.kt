package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisContract
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables.ClassificationAndCoverage
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables.DashboardHeader
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables.FinancialMetrics
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables.IssuesOverview
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables.OperatorBreakdownSection
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables.OutputFilesSection
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables.TypeTransitionsSection
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.AnalyzerContentScaffold

@Composable
fun AnalysisDashboardContent(
    state: MessageAnalysisContract.State,
    snackbarHostState: SnackbarHostState,
    onStartNewAnalysis: () -> Unit,
    onOpenOutputFolder: () -> Unit,
) {
    val summary = requireNotNull(state.summary)
    AnalyzerContentScaffold(snackbarHostState) {
        item {
            DashboardHeader(
                fileName = state.selectedMessagesFile?.name.orEmpty(),
                processedRows = summary.processedRows,
                onStartNewAnalysis = onStartNewAnalysis,
            )
        }
        item { FinancialMetrics(summary) }
        item { ClassificationAndCoverage(summary) }
        item { IssuesOverview(summary) }
        item { TypeTransitionsSection(summary.trafficTypeTransitions) }
        item { OperatorBreakdownSection(summary.operatorSummaries) }
        item {
            OutputFilesSection(
                outputCsvPath = state.outputCsvPath,
                logPath = state.logPath,
                onOpenOutputFolder = onOpenOutputFolder,
            )
        }
    }
}
