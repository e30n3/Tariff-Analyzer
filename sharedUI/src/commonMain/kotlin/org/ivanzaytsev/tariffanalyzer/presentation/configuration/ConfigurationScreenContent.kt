package org.ivanzaytsev.tariffanalyzer.presentation.configuration

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import org.ivanzaytsev.tariffanalyzer.presentation.configuration.ConfigurationContract.Action
import org.ivanzaytsev.tariffanalyzer.presentation.configuration.composables.ConfigStatusSection
import org.ivanzaytsev.tariffanalyzer.presentation.configuration.composables.InitialImportSection
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.AnalyzerContentScaffold
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.ValidationIssuesSection

@Composable
fun ConfigurationScreenContent(
    state: ConfigurationContract.State,
    snackbarHostState: SnackbarHostState,
    onAction: (Action) -> Unit,
) {
    AnalyzerContentScaffold(snackbarHostState) {
        item {
            ConfigStatusSection(
                state = state,
                onValidate = { onAction(Action.ValidateConfig) },
            )
        }
        item {
            InitialImportSection(
                state = state,
                onTemplatesSelected = { onAction(Action.ChooseTemplatesCsv(it)) },
                onTariffSelected = { onAction(Action.ChooseTariffCsv(it)) },
                onGenerate = { onAction(Action.GenerateConfig) },
            )
        }
        item {
            ValidationIssuesSection(state.validationIssues)
        }
    }
}
