package org.ivanzaytsev.tariffanalyzer.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.domain.model.ThemeMode
import org.ivanzaytsev.tariffanalyzer.presentation.settings.composables.ThemeModeItem
import org.jetbrains.compose.resources.stringResource
import tariff_analyzer.sharedui.generated.resources.Res
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
        modifier = Modifier.fillMaxSize().padding(16.dp),
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
    }
}
