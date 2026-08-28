package org.ivanzaytsev.tariffanalyzer.presentation.messageanalysis

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import org.ivanzaytsev.tariffanalyzer.designsystem.theme.TariffAnalyzerTheme
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisContract
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.composables.ProcessingProgress
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ProcessingProgressTest {

    @Test
    fun runningWithoutEstimateShowsPreparationText() = runComposeUiTest {
        setContent {
            TariffAnalyzerTheme(isDark = false) {
                ProcessingProgress(
                    MessageAnalysisContract.State(
                        processingStatus = MessageAnalysisContract.ProcessingStatus.Running,
                    ),
                )
            }
        }

        onNodeWithText("0% · Подготовка оценки общего количества строк…").fetchSemanticsNode()
    }

    @Test
    fun runningWithEstimateShowsApproximateTotal() = runComposeUiTest {
        setContent {
            TariffAnalyzerTheme(isDark = false) {
                ProcessingProgress(
                    MessageAnalysisContract.State(
                        processingStatus = MessageAnalysisContract.ProcessingStatus.Running,
                        processedRows = 1_000,
                        totalRowsHint = 1_500,
                        progressFraction = 0.67f,
                    ),
                )
            }
        }

        onNodeWithText("67% · Обработано строк: 1000 из примерно 1500").fetchSemanticsNode()
    }

    @Test
    fun completedShowsExactTotal() = runComposeUiTest {
        setContent {
            TariffAnalyzerTheme(isDark = false) {
                ProcessingProgress(
                    MessageAnalysisContract.State(
                        processingStatus = MessageAnalysisContract.ProcessingStatus.Completed,
                        processedRows = 1_500,
                        totalRowsHint = 1_500,
                        progressFraction = 1f,
                    ),
                )
            }
        }

        onNodeWithText("100% · Обработка завершена: 1500 строк").fetchSemanticsNode()
    }
}
