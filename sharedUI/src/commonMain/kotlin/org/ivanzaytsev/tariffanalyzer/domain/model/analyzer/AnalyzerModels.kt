package org.ivanzaytsev.tariffanalyzer.domain.model.analyzer

data class AnalyzerFileReference(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val purpose: AnalyzerFilePurpose,
)

enum class AnalyzerFilePurpose {
    MessageTemplates,
    Tariff,
    Messages,
}

enum class ConfigStatus {
    Missing,
    Valid,
    Invalid,
}

enum class ValidationIssueSeverity {
    Error,
    Warning,
}

data class ValidationIssue(
    val severity: ValidationIssueSeverity,
    val location: String,
    val message: String,
)

data class ConfigStatusResult(
    val status: ConfigStatus,
    val configPath: String?,
    val issues: List<ValidationIssue>,
)

data class GeneratedConfigResult(
    val status: ConfigStatus,
    val configPath: String,
    val issues: List<ValidationIssue>,
)

data class ProcessMessagesRequest(
    val messagesFile: AnalyzerFileReference,
)

sealed interface ProcessingUpdate {
    data class Progress(
        val processedRows: Long,
        val totalRowsHint: Long?,
        val progressFraction: Float,
    ) : ProcessingUpdate

    data class Completed(
        val processedRows: Long,
        val outputCsvPath: String,
        val logPath: String,
    ) : ProcessingUpdate
}
