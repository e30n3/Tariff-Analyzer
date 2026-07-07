package org.ivanzaytsev.tariffanalyzer.presentation.tariff

import org.ivanzaytsev.tariffanalyzer.domain.model.Tariff

interface TariffContract {

    data class State(
        val isLoading: Boolean = false,
        val tariffs: List<Tariff> = emptyList(),
        val selectedId: String? = null,
        val error: String? = null,
    )

    sealed interface Action {
        data object Load : Action
        data object Refresh : Action
        data object OpenSettings : Action
        data class Select(val id: String) : Action
    }

    sealed interface Effect {
        data class ShowMessage(val message: String) : Effect
        data object NavigateToSettings : Effect
    }
}
