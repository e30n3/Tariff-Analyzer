package org.ivanzaytsev.tariffanalyzer.presentation.configuration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Fact_check
import com.composables.icons.materialsymbols.rounded.Upload_file
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.AnalyzerContentScaffold
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.FilePickerTile
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.StatusIcon
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.ValidationIssuesSection
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.label
import org.ivanzaytsev.tariffanalyzer.presentation.configuration.ConfigurationContract.Action
import org.ivanzaytsev.tariffanalyzer.presentation.configuration.ConfigurationContract.OperationStatus

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

@Composable
private fun ConfigStatusSection(
    state: ConfigurationContract.State,
    onValidate: () -> Unit,
) {
    DashboardSection {
        AnalyzerSectionHeader(
            title = "JSON-конфигурация",
            description = "Статус загрузки и валидации конфигурации для обработки сообщений.",
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusIcon(state.configStatus, state.isLoadingConfigStatus)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.configStatus.label(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = state.configPath ?: "Файл конфигурации пока не найден",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OutlinedButton(
                enabled = state.canValidateConfig,
                onClick = onValidate,
            ) {
                Icon(MaterialSymbols.Rounded.Fact_check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Проверить")
            }
        }
    }
}

@Composable
private fun InitialImportSection(
    state: ConfigurationContract.State,
    onTemplatesSelected: (AnalyzerFileReference) -> Unit,
    onTariffSelected: (AnalyzerFileReference) -> Unit,
    onGenerate: () -> Unit,
) {
    DashboardSection {
        AnalyzerSectionHeader(
            title = "Первый запуск",
            description = "Выберите исходные CSV. На этом инкременте приложение сохраняет только ссылки на файлы.",
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilePickerTile(
                title = "message_templates.csv",
                file = state.selectedTemplatesFile,
                purpose = AnalyzerFilePurpose.MessageTemplates,
                onFilePicked = onTemplatesSelected,
                modifier = Modifier.weight(1f),
            )
            FilePickerTile(
                title = "tariff.csv",
                file = state.selectedTariffFile,
                purpose = AnalyzerFilePurpose.Tariff,
                onFilePicked = onTariffSelected,
                modifier = Modifier.weight(1f),
            )
        }
        Button(
            enabled = state.canGenerateConfig,
            onClick = onGenerate,
        ) {
            Icon(MaterialSymbols.Rounded.Upload_file, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (state.operationStatus is OperationStatus.GeneratingConfig) {
                    "Генерация..."
                } else {
                    "Сгенерировать конфигурацию"
                },
            )
        }
        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
