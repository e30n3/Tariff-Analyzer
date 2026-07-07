package org.ivanzaytsev.tariffanalyzer.presentation.analyzer

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnalyzerScreen(
    onNavigateToSettings: () -> Unit,
) {
    val viewModel = koinViewModel<AnalyzerViewModel>()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AnalyzerContract.Effect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)

                AnalyzerContract.Effect.NavigateToSettings -> onNavigateToSettings()
            }
        }
    }

    AnalyzerScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
    )
}
