package org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssue

interface ConfigurationContract {

    data class State(
        val isLoadingConfigStatus: Boolean = false,
        val configStatus: ConfigStatus = ConfigStatus.Missing,
        val configPath: String? = null,
        val selectedTemplatesFile: AnalyzerFileReference? = null,
        val selectedTariffFile: AnalyzerFileReference? = null,
        val validationIssues: List<ValidationIssue> = emptyList(),
        val operationStatus: OperationStatus = OperationStatus.Idle,
        val error: String? = null,
    ) {
        val canGenerateConfig: Boolean
            get() = selectedTemplatesFile != null &&
                selectedTariffFile != null &&
                operationStatus == OperationStatus.Idle

        val canValidateConfig: Boolean
            get() = configPath != null && operationStatus == OperationStatus.Idle
    }

    sealed interface OperationStatus {
        data object Idle : OperationStatus
        data object GeneratingConfig : OperationStatus
        data object ValidatingConfig : OperationStatus
    }

    sealed interface Action {
        data object LoadConfigStatus : Action
        data class ChooseTemplatesCsv(val file: AnalyzerFileReference) : Action
        data class ChooseTariffCsv(val file: AnalyzerFileReference) : Action
        data object GenerateConfig : Action
        data object ValidateConfig : Action
    }

    sealed interface Effect {
        data class ShowMessage(val message: String) : Effect
    }
}
