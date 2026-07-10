package org.ivanzaytsev.tariffanalyzer.domain.analyzer

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.DecimalAmount
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingIssue
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingIssueKind
import kotlin.test.Test
import kotlin.test.assertEquals

class AnalysisSummaryAccumulatorTest {

    @Test
    fun aggregatesExactCostsCoverageIssuesOperatorsAndTransitions() {
        val accumulator = AnalysisSummaryAccumulator()

        accumulator.add(
            result(
                operator = "tele2",
                currentType = "Рекламный",
                correctType = "Сервисный",
                currentPrice = "4.430000000000",
                correctPrice = "1.900000000000",
                issues = listOf(
                    issue(ProcessingIssueKind.TemplateConflict),
                    issue(ProcessingIssueKind.OperatorTrafficMappingNotFound),
                ),
            ),
        )
        accumulator.add(
            result(
                operator = "tele2",
                currentType = "Сервисный",
                correctType = "Сервисный",
                currentPrice = null,
                correctPrice = "2.000000000000",
                issues = listOf(
                    issue(ProcessingIssueKind.CurrentTariffNotFound),
                    issue(ProcessingIssueKind.TemplateNotFound),
                ),
            ),
        )

        val summary = accumulator.build()

        assertEquals(2L, summary.processedRows)
        assertEquals(decimal("4.43"), summary.currentCost.total)
        assertEquals(1L, summary.currentCost.pricedRows)
        assertEquals(decimal("3.9"), summary.correctCost.total)
        assertEquals(2L, summary.correctCost.pricedRows)
        assertEquals(1L, summary.comparableRows)
        assertEquals(decimal("2.53"), summary.costDifference)
        assertEquals(1L, summary.matchingTypeRows)
        assertEquals(1L, summary.mismatchRows)
        assertEquals(2L, summary.warningAffectedRows)
        assertEquals(1L, summary.errorAffectedRows)
        assertEquals(2L, summary.issueCounts.getValue(ProcessingIssueKind.TemplateConflict) + summary.issueCounts.getValue(ProcessingIssueKind.OperatorTrafficMappingNotFound))
        assertEquals(1L, summary.trafficTypeTransitions.single().rows)
        assertEquals(decimal("2.53"), summary.operatorSummaries.single().costDifference)
    }

    @Test
    fun emptyAccumulatorProducesZeroSummary() {
        val summary = AnalysisSummaryAccumulator().build()

        assertEquals(0L, summary.processedRows)
        assertEquals(DecimalAmount.Zero, summary.currentCost.total)
        assertEquals(DecimalAmount.Zero, summary.correctCost.total)
        assertEquals(DecimalAmount.Zero, summary.costDifference)
        assertEquals(emptyList(), summary.operatorSummaries)
        assertEquals(emptyList(), summary.trafficTypeTransitions)
    }

    @Test
    fun sortsOperatorsByAbsoluteDifferenceAndTransitionsByCount() {
        val accumulator = AnalysisSummaryAccumulator()
        repeat(2) {
            accumulator.add(result("mts", "A", "B", "10", "7"))
        }
        repeat(3) {
            accumulator.add(result("tele2", "C", "D", "2", "3"))
        }

        val summary = accumulator.build()

        assertEquals(listOf("mts", "tele2"), summary.operatorSummaries.map { it.operator })
        assertEquals(listOf(3L, 2L), summary.trafficTypeTransitions.map { it.rows })
    }

    private fun result(
        operator: String,
        currentType: String,
        correctType: String,
        currentPrice: String?,
        correctPrice: String?,
        issues: List<ProcessingIssue> = emptyList(),
    ): MessageAnalysisResult = MessageAnalysisResult(
        additionalValuesByColumn = emptyMap(),
        additionalValues = emptyList(),
        logEntries = emptyList(),
        operator = operator,
        currentTrafficType = currentType,
        correctTrafficType = correctType,
        currentPrice = currentPrice,
        correctPrice = correctPrice,
        isTypeMismatch = currentType != correctType,
        isCorrectTypeDetermined = true,
        issues = issues,
    )

    private fun issue(kind: ProcessingIssueKind): ProcessingIssue = ProcessingIssue(kind, kind.name)

    private fun decimal(value: String): DecimalAmount = requireNotNull(DecimalAmount.parse(value))
}
