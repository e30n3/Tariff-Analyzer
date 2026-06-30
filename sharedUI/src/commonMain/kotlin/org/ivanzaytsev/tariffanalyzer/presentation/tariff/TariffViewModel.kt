package org.ivanzaytsev.tariffanalyzer.presentation.tariff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GetTariffsUseCase
import org.ivanzaytsev.tariffanalyzer.presentation.tariff.TariffContract.Effect
import org.ivanzaytsev.tariffanalyzer.presentation.tariff.TariffContract.Intent
import org.ivanzaytsev.tariffanalyzer.presentation.tariff.TariffContract.State

class TariffViewModel(
    private val getTariffs: GetTariffsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(Intent.Load)
    }

    fun onIntent(intent: Intent) {
        when (intent) {
            Intent.Load,
            Intent.Refresh -> loadTariffs()

            is Intent.Select -> _state.update { it.copy(selectedId = intent.id) }
        }
    }

    private fun loadTariffs() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching { getTariffs() }
                .onSuccess { tariffs ->
                    _state.update { it.copy(isLoading = false, tariffs = tariffs) }
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Failed to load tariffs"
                    _state.update { it.copy(isLoading = false, error = message) }
                    _effect.send(Effect.ShowMessage(message))
                }
        }
    }
}
