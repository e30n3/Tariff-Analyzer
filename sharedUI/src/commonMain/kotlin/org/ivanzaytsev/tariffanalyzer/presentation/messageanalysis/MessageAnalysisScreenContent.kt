package org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis.MessageAnalysisContract.Action
import org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis.composables.MessageProcessingSection
import org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis.composables.ResultSection
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.AnalyzerContentScaffold
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.ValidationIssuesSection

@Composable
fun MessageAnalysisScreenContent(
    state: MessageAnalysisContract.State,
    snackbarHostState: SnackbarHostState,
    onAction: (Action) -> Unit,
) {
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
            ResultSection(state)
        }
        item {
            ValidationIssuesSection(state.validationIssues)
        }
    }
}
