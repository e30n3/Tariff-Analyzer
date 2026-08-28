package org.ivanzaytsev.tariffanalyzer.presentation.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import org.ivanzaytsev.tariffanalyzer.designsystem.theme.TariffAnalyzerTheme
import org.ivanzaytsev.tariffanalyzer.presentation.screen.settings.SettingsContract
import org.ivanzaytsev.tariffanalyzer.presentation.screen.settings.SettingsScreenContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SettingsScreenContentTest {

    @Test
    fun dashboardToggleReflectsStateAndDispatchesAction() = runComposeUiTest {
        var receivedAction: SettingsContract.Action? = null
        setContent {
            TariffAnalyzerTheme(isDark = false) {
                SettingsScreenContent(
                    state = SettingsContract.State(isDashboardEnabled = false),
                    onAction = { receivedAction = it },
                )
            }
        }

        onNodeWithText("Показывать dashboard").performClick()

        assertEquals(SettingsContract.Action.SetDashboardEnabled(true), receivedAction)
    }
}
