package org.ivanzaytsev.tariffanalyzer.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.domain.model.ThemeMode
import org.jetbrains.compose.resources.stringResource
import tariff_analyzer.sharedui.generated.resources.Res
import tariff_analyzer.sharedui.generated.resources.back
import tariff_analyzer.sharedui.generated.resources.settings_title
import tariff_analyzer.sharedui.generated.resources.theme_dark
import tariff_analyzer.sharedui.generated.resources.theme_light
import tariff_analyzer.sharedui.generated.resources.theme_section_description
import tariff_analyzer.sharedui.generated.resources.theme_section_title
import tariff_analyzer.sharedui.generated.resources.theme_system

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    state: SettingsContract.State,
    onAction: (SettingsContract.Action) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
                navigationIcon = {
                    TextButton(onClick = { onAction(SettingsContract.Action.BackClick) }) {
                        Text(stringResource(Res.string.back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
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
}

@Composable
private fun ThemeModeItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        headlineContent = { Text(title) },
        leadingContent = {
            RadioButton(
                selected = selected,
                onClick = null,
            )
        },
    )
}
