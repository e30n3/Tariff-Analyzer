package org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.runComposeUiTest
import org.ivanzaytsev.tariffanalyzer.designsystem.theme.TariffAnalyzerTheme
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalysisSummary
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.CostSummary
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.DecimalAmount
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisContract
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisScreenContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AnalysisDashboardContentTest {

    @Test
    fun completedStateShowsDashboardAndStartsNewAnalysis() = runComposeUiTest {
        var receivedAction: MessageAnalysisContract.Action? = null
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            TariffAnalyzerTheme(isDark = false) {
                MessageAnalysisScreenContent(
                    state = completedState(),
                    snackbarHostState = snackbarHostState,
                    onAction = { receivedAction = it },
                )
            }
        }

        onNodeWithText("Анализ завершён").fetchSemanticsNode()
        onNodeWithText("Расчёт оператора").fetchSemanticsNode()
        onNodeWithText("Правильная стоимость").fetchSemanticsNode()
        onNodeWithText("Разница").fetchSemanticsNode()
        onNode(hasScrollToIndexAction()).performScrollToIndex(6)
        onNodeWithText("Показать в папке").performClick()
        assertEquals(MessageAnalysisContract.Action.OpenOutputFolder, receivedAction)
        onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        onNodeWithText("Новый анализ").performClick()

        assertEquals(MessageAnalysisContract.Action.StartNewAnalysis, receivedAction)
    }

    @Test
    fun completedStateShowsRegularResultWhenDashboardIsDisabled() = runComposeUiTest {
        var receivedAction: MessageAnalysisContract.Action? = null
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            TariffAnalyzerTheme(isDark = false) {
                MessageAnalysisScreenContent(
                    state = completedState().copy(isDashboardEnabled = false),
                    snackbarHostState = snackbarHostState,
                    onAction = { receivedAction = it },
                )
            }
        }

        assertTrue(onAllNodesWithText("Расчёт оператора").fetchSemanticsNodes().isEmpty())
        onNodeWithText("Результат").fetchSemanticsNode()
        onNodeWithText("/tmp/messages_analyzed.csv").fetchSemanticsNode()
        onNodeWithText("Показать в папке").performClick()
        assertEquals(MessageAnalysisContract.Action.OpenOutputFolder, receivedAction)
    }

    private fun completedState(): MessageAnalysisContract.State = MessageAnalysisContract.State(
        selectedMessagesFile = AnalyzerFileReference(
            name = "messages.csv",
            path = "/tmp/messages.csv",
            sizeBytes = 128,
            purpose = AnalyzerFilePurpose.Messages,
        ),
        processingStatus = MessageAnalysisContract.ProcessingStatus.Completed,
        processedRows = 10,
        outputCsvPath = "/tmp/messages_analyzed.csv",
        logPath = "/tmp/messages.log",
        summary = AnalysisSummary(
            processedRows = 10,
            currentCost = CostSummary(decimal("44.30"), 10),
            correctCost = CostSummary(decimal("19.00"), 10),
            comparableRows = 10,
            costDifference = decimal("25.30"),
            matchingTypeRows = 8,
            mismatchRows = 2,
            determinedCorrectTypeRows = 9,
            errorAffectedRows = 0,
            warningAffectedRows = 1,
            issueCounts = emptyMap(),
            operatorSummaries = emptyList(),
            trafficTypeTransitions = emptyList(),
        ),
    )

    private fun decimal(value: String): DecimalAmount = requireNotNull(DecimalAmount.parse(value))
}
