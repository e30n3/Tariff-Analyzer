package org.ivanzaytsev.tariffanalyzer.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<State : Any, Action : Any, Effect : Any>(
    initialState: State,
    loggerTag: String,
) : ViewModel() {

    private val logger = Logger.withTag(loggerTag)
    private val _state = MutableStateFlow(initialState)
    private val _effect = Channel<Effect>(Channel.BUFFERED)

    val state: StateFlow<State> = _state.asStateFlow()
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    fun onAction(action: Action) {
        logger.d { "Action received: $action" }
        reduce(action)
    }

    protected abstract fun reduce(action: Action)

    protected fun setState(reducer: (State) -> State) {
        _state.update(reducer)
        logger.v { "State updated: ${_state.value}" }
    }

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            logger.d { "Effect emitted: $effect" }
            _effect.send(effect)
        }
    }

    protected fun logError(throwable: Throwable, operation: String) {
        val stackFrames = throwable.stackTrace.joinToString(separator = "\n") { "\tat $it" }
        logger.e {
            "$operation failed with ${throwable::class.simpleName}\n$stackFrames"
        }
    }
}
