package org.ivanzaytsev.tariffanalyzer.domain.model.analyzer

enum class ProcessingIssueSeverity {
    Error,
    Warning,
}

enum class ProcessingIssueKind(
    val severity: ProcessingIssueSeverity,
) {
    TemplateNotFound(ProcessingIssueSeverity.Warning),
    OperatorTrafficMappingNotFound(ProcessingIssueSeverity.Warning),
    TemplateConflict(ProcessingIssueSeverity.Warning),
    CurrentTariffNotFound(ProcessingIssueSeverity.Error),
    CorrectTariffNotFound(ProcessingIssueSeverity.Error),
}

data class ProcessingIssue(
    val kind: ProcessingIssueKind,
    val message: String,
)

data class CostSummary(
    val total: DecimalAmount,
    val pricedRows: Long,
)

data class TrafficTypeTransitionSummary(
    val currentType: String,
    val correctType: String,
    val rows: Long,
)

data class OperatorAnalysisSummary(
    val operator: String,
    val processedRows: Long,
    val mismatchRows: Long,
    val currentCost: CostSummary,
    val correctCost: CostSummary,
    val comparableRows: Long,
    val costDifference: DecimalAmount,
    val errorAffectedRows: Long,
    val warningAffectedRows: Long,
)

data class AnalysisSummary(
    val processedRows: Long,
    val currentCost: CostSummary,
    val correctCost: CostSummary,
    val comparableRows: Long,
    val costDifference: DecimalAmount,
    val matchingTypeRows: Long,
    val mismatchRows: Long,
    val determinedCorrectTypeRows: Long,
    val errorAffectedRows: Long,
    val warningAffectedRows: Long,
    val issueCounts: Map<ProcessingIssueKind, Long>,
    val operatorSummaries: List<OperatorAnalysisSummary>,
    val trafficTypeTransitions: List<TrafficTypeTransitionSummary>,
)
