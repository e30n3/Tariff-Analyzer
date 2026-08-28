package org.ivanzaytsev.tariffanalyzer.presentation.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ivanzaytsev.tariffanalyzer.domain.model.ThemeMode
import org.ivanzaytsev.tariffanalyzer.domain.repository.SettingsRepository
import org.ivanzaytsev.tariffanalyzer.presentation.screen.settings.SettingsContract
import org.ivanzaytsev.tariffanalyzer.presentation.screen.settings.SettingsViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun dashboardSettingIsLoadedAndUpdated() = runTest(dispatcher) {
        val repository = FakeSettingsRepository(dashboardEnabled = false)
        val viewModel = SettingsViewModel(repository)

        assertFalse(viewModel.state.value.isDashboardEnabled)

        viewModel.onAction(SettingsContract.Action.SetDashboardEnabled(true))
        advanceUntilIdle()

        assertTrue(repository.dashboardEnabled.value)
        assertTrue(viewModel.state.value.isDashboardEnabled)
    }

    private class FakeSettingsRepository(dashboardEnabled: Boolean) : SettingsRepository {
        private val mutableThemeMode = MutableStateFlow(ThemeMode.System)
        private val mutableDebugMode = MutableStateFlow(false)
        private val mutableDashboardEnabled = MutableStateFlow(dashboardEnabled)
        override val themeMode: StateFlow<ThemeMode> = mutableThemeMode
        override val debugMode: StateFlow<Boolean> = mutableDebugMode
        override val dashboardEnabled: StateFlow<Boolean> = mutableDashboardEnabled

        override fun setThemeMode(mode: ThemeMode) {
            mutableThemeMode.value = mode
        }

        override fun setDebugMode(enabled: Boolean) {
            mutableDebugMode.value = enabled
        }

        override fun setDashboardEnabled(enabled: Boolean) {
            mutableDashboardEnabled.value = enabled
        }
    }
}
