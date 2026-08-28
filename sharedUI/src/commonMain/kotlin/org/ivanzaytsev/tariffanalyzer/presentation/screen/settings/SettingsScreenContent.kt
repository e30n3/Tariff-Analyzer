package org.ivanzaytsev.tariffanalyzer.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.domain.model.ThemeMode
import org.ivanzaytsev.tariffanalyzer.presentation.screen.settings.composables.ThemeModeItem
import org.ivanzaytsev.tariffanalyzer.presentation.screen.settings.composables.ToggleSettingItem
import org.jetbrains.compose.resources.stringResource
import tariff_analyzer.sharedui.generated.resources.Res
import tariff_analyzer.sharedui.generated.resources.dashboard_mode_description
import tariff_analyzer.sharedui.generated.resources.dashboard_mode_title
import tariff_analyzer.sharedui.generated.resources.dashboard_section_description
import tariff_analyzer.sharedui.generated.resources.dashboard_section_title
import tariff_analyzer.sharedui.generated.resources.debug_mode_description
import tariff_analyzer.sharedui.generated.resources.debug_mode_title
import tariff_analyzer.sharedui.generated.resources.debug_section_description
import tariff_analyzer.sharedui.generated.resources.debug_section_title
import tariff_analyzer.sharedui.generated.resources.theme_dark
import tariff_analyzer.sharedui.generated.resources.theme_light
import tariff_analyzer.sharedui.generated.resources.theme_section_description
import tariff_analyzer.sharedui.generated.resources.theme_section_title
import tariff_analyzer.sharedui.generated.resources.theme_system

@Composable
fun SettingsScreenContent(
    state: SettingsContract.State,
    onAction: (SettingsContract.Action) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnalyzerSectionHeader(
            title = stringResource(Res.string.theme_section_title),
            description = stringResource(Res.string.theme_section_description),
        )
        ThemeModeItem(
            title = stringResource(Res.string.theme_system),
            selected = state.selectedThemeMode == ThemeMode.System,
            onClick = {
                onAction(SettingsContract.Action.SelectThemeMode(ThemeMode.System))
            },
        )
        ThemeModeItem(
            title = stringResource(Res.string.theme_dark),
            selected = state.selectedThemeMode == ThemeMode.Dark,
            onClick = {
                onAction(SettingsContract.Action.SelectThemeMode(ThemeMode.Dark))
            },
        )
        ThemeModeItem(
            title = stringResource(Res.string.theme_light),
            selected = state.selectedThemeMode == ThemeMode.Light,
            onClick = {
                onAction(SettingsContract.Action.SelectThemeMode(ThemeMode.Light))
            },
        )
        AnalyzerSectionHeader(
            title = stringResource(Res.string.dashboard_section_title),
            description = stringResource(Res.string.dashboard_section_description),
        )
        ToggleSettingItem(
            title = stringResource(Res.string.dashboard_mode_title),
            description = stringResource(Res.string.dashboard_mode_description),
            enabled = state.isDashboardEnabled,
            onEnabledChange = {
                onAction(SettingsContract.Action.SetDashboardEnabled(it))
            },
        )
        AnalyzerSectionHeader(
            title = stringResource(Res.string.debug_section_title),
            description = stringResource(Res.string.debug_section_description),
        )
        ToggleSettingItem(
            title = stringResource(Res.string.debug_mode_title),
            description = stringResource(Res.string.debug_mode_description),
            enabled = state.isDebugModeEnabled,
            onEnabledChange = {
                onAction(SettingsContract.Action.SetDebugMode(it))
            },
        )
    }
}
