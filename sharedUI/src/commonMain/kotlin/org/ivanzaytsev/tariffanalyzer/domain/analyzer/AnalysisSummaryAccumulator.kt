package org.ivanzaytsev.tariffanalyzer.domain.analyzer

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalysisSummary
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.CostSummary
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.DecimalAmount
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.OperatorAnalysisSummary
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingIssueKind
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingIssueSeverity
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TrafficTypeTransitionSummary

class AnalysisSummaryAccumulator {
    private var processedRows = 0L
    private var currentCost = DecimalAmount.Zero
    private var currentPricedRows = 0L
    private var correctCost = DecimalAmount.Zero
    private var correctPricedRows = 0L
    private var comparableRows = 0L
    private var costDifference = DecimalAmount.Zero
    private var matchingTypeRows = 0L
    private var mismatchRows = 0L
    private var determinedCorrectTypeRows = 0L
    private var errorAffectedRows = 0L
    private var warningAffectedRows = 0L
    private val issueCounts = mutableMapOf<ProcessingIssueKind, Long>()
    private val operators = mutableMapOf<String, MutableOperatorSummary>()
    private val transitions = mutableMapOf<TrafficTypeTransition, Long>()

    fun add(result: MessageAnalysisResult) {
        processedRows++
        val currentAmount = result.currentPrice?.let(::parsePrice)
        val correctAmount = result.correctPrice?.let(::parsePrice)
        val hasError = result.issues.any { it.kind.severity == ProcessingIssueSeverity.Error }
        val hasWarning = result.issues.any { it.kind.severity == ProcessingIssueSeverity.Warning }

        if (currentAmount != null) {
            currentCost += currentAmount
            currentPricedRows++
        }
        if (correctAmount != null) {
            correctCost += correctAmount
            correctPricedRows++
        }
        if (currentAmount != null && correctAmount != null) {
            comparableRows++
            costDifference += currentAmount - correctAmount
        }

        if (result.isTypeMismatch) {
            mismatchRows++
            val transition = TrafficTypeTransition(result.currentTrafficType, result.correctTrafficType)
            transitions[transition] = transitions.getOrElse(transition) { 0L } + 1L
        } else {
            matchingTypeRows++
        }
        if (result.isCorrectTypeDetermined) determinedCorrectTypeRows++
        if (hasError) errorAffectedRows++
        if (hasWarning) warningAffectedRows++
        result.issues.forEach { issue ->
            issueCounts[issue.kind] = issueCounts.getOrElse(issue.kind) { 0L } + 1L
        }

        operators.getOrPut(result.operator.ifBlank { "Не указан" }) { MutableOperatorSummary() }
            .add(result, currentAmount, correctAmount, hasError, hasWarning)
    }

    fun build(): AnalysisSummary = AnalysisSummary(
        processedRows = processedRows,
        currentCost = CostSummary(currentCost, currentPricedRows),
        correctCost = CostSummary(correctCost, correctPricedRows),
        comparableRows = comparableRows,
        costDifference = costDifference,
        matchingTypeRows = matchingTypeRows,
        mismatchRows = mismatchRows,
        determinedCorrectTypeRows = determinedCorrectTypeRows,
        errorAffectedRows = errorAffectedRows,
        warningAffectedRows = warningAffectedRows,
        issueCounts = ProcessingIssueKind.entries.associateWith { issueCounts[it] ?: 0L },
        operatorSummaries = operators.map { (operator, summary) -> summary.toImmutable(operator) }
            .sortedWith(
                compareByDescending<OperatorAnalysisSummary> { it.costDifference.absoluteValue() }
                    .thenBy { it.operator },
            ),
        trafficTypeTransitions = transitions.map { (transition, rows) ->
            TrafficTypeTransitionSummary(
                currentType = transition.currentType,
                correctType = transition.correctType,
                rows = rows,
            )
        }.sortedWith(
            compareByDescending<TrafficTypeTransitionSummary> { it.rows }
                .thenBy { it.currentType }
                .thenBy { it.correctType },
        ),
    )

    private fun parsePrice(value: String): DecimalAmount =
        requireNotNull(DecimalAmount.parse(value)) { "Некорректная цена тарифа: '$value'." }
}

private data class TrafficTypeTransition(
    val currentType: String,
    val correctType: String,
)

private class MutableOperatorSummary {
    private var processedRows = 0L
    private var mismatchRows = 0L
    private var currentCost = DecimalAmount.Zero
    private var currentPricedRows = 0L
    private var correctCost = DecimalAmount.Zero
    private var correctPricedRows = 0L
    private var comparableRows = 0L
    private var costDifference = DecimalAmount.Zero
    private var errorAffectedRows = 0L
    private var warningAffectedRows = 0L

    fun add(
        result: MessageAnalysisResult,
        currentAmount: DecimalAmount?,
        correctAmount: DecimalAmount?,
        hasError: Boolean,
        hasWarning: Boolean,
    ) {
        processedRows++
        if (result.isTypeMismatch) mismatchRows++
        if (currentAmount != null) {
            currentCost += currentAmount
            currentPricedRows++
        }
        if (correctAmount != null) {
            correctCost += correctAmount
            correctPricedRows++
        }
        if (currentAmount != null && correctAmount != null) {
            comparableRows++
            costDifference += currentAmount - correctAmount
        }
        if (hasError) errorAffectedRows++
        if (hasWarning) warningAffectedRows++
    }

    fun toImmutable(operator: String): OperatorAnalysisSummary = OperatorAnalysisSummary(
        operator = operator,
        processedRows = processedRows,
        mismatchRows = mismatchRows,
        currentCost = CostSummary(currentCost, currentPricedRows),
        correctCost = CostSummary(correctCost, correctPricedRows),
        comparableRows = comparableRows,
        costDifference = costDifference,
        errorAffectedRows = errorAffectedRows,
        warningAffectedRows = warningAffectedRows,
    )
}
