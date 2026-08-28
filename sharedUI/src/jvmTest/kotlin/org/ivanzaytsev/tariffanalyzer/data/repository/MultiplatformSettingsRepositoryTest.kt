package org.ivanzaytsev.tariffanalyzer.data.repository

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.PreferencesSettings
import org.ivanzaytsev.tariffanalyzer.data.repository.settings.MultiplatformSettingsRepository
import java.util.UUID
import java.util.prefs.Preferences
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalSettingsImplementation::class)
class MultiplatformSettingsRepositoryTest {

    @Test
    fun debugModeIsDisabledByDefault() = withTestSettings { settings ->
        val repository = MultiplatformSettingsRepository(settings)

        assertFalse(repository.debugMode.value)
    }

    @Test
    fun debugModeIsPersisted() = withTestSettings { settings ->
        val repository = MultiplatformSettingsRepository(settings)

        repository.setDebugMode(true)

        assertTrue(repository.debugMode.value)
        assertTrue(MultiplatformSettingsRepository(settings).debugMode.value)
    }

    @Test
    fun dashboardIsEnabledByDefault() = withTestSettings { settings ->
        val repository = MultiplatformSettingsRepository(settings)

        assertTrue(repository.dashboardEnabled.value)
    }

    @Test
    fun dashboardSettingIsPublishedAndPersisted() = withTestSettings { settings ->
        val repository = MultiplatformSettingsRepository(settings)

        repository.setDashboardEnabled(false)

        assertFalse(repository.dashboardEnabled.value)
        assertFalse(MultiplatformSettingsRepository(settings).dashboardEnabled.value)

        repository.setDashboardEnabled(true)

        assertTrue(repository.dashboardEnabled.value)
        assertTrue(MultiplatformSettingsRepository(settings).dashboardEnabled.value)
    }

    private fun withTestSettings(block: (PreferencesSettings) -> Unit) {
        val preferences = Preferences.userRoot().node("tariff-analyzer-tests/${UUID.randomUUID()}")
        try {
            block(PreferencesSettings(preferences))
        } finally {
            preferences.removeNode()
        }
    }
}
