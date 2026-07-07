package org.ivanzaytsev.tariffanalyzer.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<SettingsViewModel>()

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SettingsContract.Effect.NavigateBack -> onNavigateBack()
            }
        }
    }

    SettingsScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}
