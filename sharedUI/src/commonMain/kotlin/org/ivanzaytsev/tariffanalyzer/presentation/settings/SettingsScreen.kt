package org.ivanzaytsev.tariffanalyzer.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import org.ivanzaytsev.tariffanalyzer.domain.repository.SettingsRepository
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
) {
    val settingsRepository = koinInject<SettingsRepository>()
    val viewModel: SettingsViewModel = viewModel {
        SettingsViewModel(settingsRepository)
    }

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
