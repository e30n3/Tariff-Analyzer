package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisContract.Action
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisContract.ProcessingStatus
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.AnalysisDashboardContent
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.composables.MessageProcessingSection
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.composables.ResultSection
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.AnalyzerContentScaffold
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.ValidationIssuesSection

@Composable
fun MessageAnalysisScreenContent(
    state: MessageAnalysisContract.State,
    snackbarHostState: SnackbarHostState,
    onAction: (Action) -> Unit,
) {
    if (
        state.processingStatus is ProcessingStatus.Completed &&
        state.summary != null &&
        state.isDashboardEnabled
    ) {
        AnalysisDashboardContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onStartNewAnalysis = { onAction(Action.StartNewAnalysis) },
            onOpenOutputFolder = { onAction(Action.OpenOutputFolder) },
        )
        return
    }

    AnalyzerContentScaffold(snackbarHostState) {
        item {
            MessageProcessingSection(
                state = state,
                onMessagesSelected = { onAction(Action.ChooseMessagesCsv(it)) },
                onStart = { onAction(Action.StartProcessing) },
                onCancel = { onAction(Action.CancelProcessing) },
            )
        }
        item {
            ResultSection(
                state = state,
                onOpenOutputFolder = { onAction(Action.OpenOutputFolder) },
            )
        }
        item {
            ValidationIssuesSection(state.validationIssues)
        }
    }
}
