package org.ivanzaytsev.tariffanalyzer.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen() {
    val viewModel = koinViewModel<SettingsViewModel>()

    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}
