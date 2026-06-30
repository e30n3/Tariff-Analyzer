package org.ivanzaytsev.tariffanalyzer.presentation.tariff

import org.ivanzaytsev.tariffanalyzer.domain.model.Tariff

interface TariffContract {

    data class State(
        val isLoading: Boolean = false,
        val tariffs: List<Tariff> = emptyList(),
        val selectedId: String? = null,
        val error: String? = null,
    )

    sealed interface Intent {
        data object Load : Intent
        data object Refresh : Intent
        data class Select(val id: String) : Intent
    }

    sealed interface Effect {
        data class ShowMessage(val message: String) : Effect
    }
}
