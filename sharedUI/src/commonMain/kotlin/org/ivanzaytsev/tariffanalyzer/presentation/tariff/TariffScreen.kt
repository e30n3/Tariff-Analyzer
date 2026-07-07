package org.ivanzaytsev.tariffanalyzer.presentation.tariff

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GetTariffsUseCase
import org.koin.compose.koinInject

@Composable
fun TariffScreen(
    onNavigateToSettings: () -> Unit,
) {
    val getTariffs = koinInject<GetTariffsUseCase>()
    val viewModel: TariffViewModel = viewModel { TariffViewModel(getTariffs) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TariffContract.Effect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)

                TariffContract.Effect.NavigateToSettings -> onNavigateToSettings()
            }
        }
    }

    TariffScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
    )
}
