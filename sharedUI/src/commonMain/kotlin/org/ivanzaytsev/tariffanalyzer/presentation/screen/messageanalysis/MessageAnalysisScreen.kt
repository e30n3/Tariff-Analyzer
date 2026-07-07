package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MessageAnalysisScreen() {
    val viewModel = koinViewModel<MessageAnalysisViewModel>()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MessageAnalysisContract.Effect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    MessageAnalysisScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
    )
}
