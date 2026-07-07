package org.ivanzaytsev.tariffanalyzer.presentation.tariff

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GetTariffsUseCase
import org.ivanzaytsev.tariffanalyzer.presentation.base.BaseViewModel
import org.ivanzaytsev.tariffanalyzer.presentation.tariff.TariffContract.Action
import org.ivanzaytsev.tariffanalyzer.presentation.tariff.TariffContract.Effect
import org.ivanzaytsev.tariffanalyzer.presentation.tariff.TariffContract.State

class TariffViewModel(
    private val getTariffs: GetTariffsUseCase,
) : BaseViewModel<State, Action, Effect>(
    initialState = State(),
    loggerTag = "TariffViewModel",
) {

    init {
        onAction(Action.Load)
    }

    override fun reduce(action: Action) {
        when (action) {
            Action.Load,
            Action.Refresh -> loadTariffs()

            Action.OpenSettings -> sendEffect(Effect.NavigateToSettings)
            is Action.Select -> setState { it.copy(selectedId = action.id) }
        }
    }

    private fun loadTariffs() {
        if (state.value.isLoading) return
        setState { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching { getTariffs() }
                .onSuccess { tariffs ->
                    setState { it.copy(isLoading = false, tariffs = tariffs) }
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Failed to load tariffs"
                    logError(throwable, message)
                    setState { it.copy(isLoading = false, error = message) }
                    sendEffect(Effect.ShowMessage(message))
                }
        }
    }
}
