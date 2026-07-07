package org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssue

interface MessageAnalysisContract {

    data class State(
        val isLoadingConfigStatus: Boolean = false,
        val configStatus: ConfigStatus = ConfigStatus.Missing,
        val configPath: String? = null,
        val selectedMessagesFile: AnalyzerFileReference? = null,
        val validationIssues: List<ValidationIssue> = emptyList(),
        val processingStatus: ProcessingStatus = ProcessingStatus.Idle,
        val processedRows: Long = 0,
        val totalRowsHint: Long? = null,
        val progressFraction: Float = 0f,
        val outputCsvPath: String? = null,
        val logPath: String? = null,
        val error: String? = null,
    ) {
        val canStartProcessing: Boolean
            get() = selectedMessagesFile != null &&
                configStatus == ConfigStatus.Valid &&
                processingStatus !is ProcessingStatus.Running
    }

    sealed interface ProcessingStatus {
        data object Idle : ProcessingStatus
        data object Running : ProcessingStatus
        data object Completed : ProcessingStatus
        data object Cancelled : ProcessingStatus
    }

    sealed interface Action {
        data object LoadConfigStatus : Action
        data class ChooseMessagesCsv(val file: AnalyzerFileReference) : Action
        data object StartProcessing : Action
        data object CancelProcessing : Action
    }

    sealed interface Effect {
        data class ShowMessage(val message: String) : Effect
    }
}
