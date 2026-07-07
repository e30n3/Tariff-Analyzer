package org.ivanzaytsev.tariffanalyzer.presentation.analyzer

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Cancel
import com.composables.icons.materialsymbols.rounded.Check_circle
import com.composables.icons.materialsymbols.rounded.Description
import com.composables.icons.materialsymbols.rounded.Error
import com.composables.icons.materialsymbols.rounded.Fact_check
import com.composables.icons.materialsymbols.rounded.Folder_open
import com.composables.icons.materialsymbols.rounded.Play_arrow
import com.composables.icons.materialsymbols.rounded.Settings
import com.composables.icons.materialsymbols.rounded.Upload_file
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssue
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssueSeverity
import org.ivanzaytsev.tariffanalyzer.presentation.analyzer.AnalyzerContract.Action
import org.ivanzaytsev.tariffanalyzer.presentation.analyzer.AnalyzerContract.ProcessingStatus
import org.ivanzaytsev.tariffanalyzer.presentation.filepicker.fileDropTarget
import org.ivanzaytsev.tariffanalyzer.presentation.filepicker.pickFileReference
import org.jetbrains.compose.resources.stringResource
import tariff_analyzer.sharedui.generated.resources.Res
import tariff_analyzer.sharedui.generated.resources.settings_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerScreenContent(
    state: AnalyzerContract.State,
    snackbarHostState: SnackbarHostState,
    onAction: (Action) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tariff Analyzer") },
                actions = {
                    IconButton(onClick = { onAction(Action.OpenSettings) }) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Settings,
                            contentDescription = stringResource(Res.string.settings_title),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val listState = rememberLazyListState()
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
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
                    MessageProcessingSection(
                        state = state,
                        onMessagesSelected = { onAction(Action.ChooseMessagesCsv(it)) },
                        onStart = { onAction(Action.StartProcessing) },
                        onCancel = { onAction(Action.CancelProcessing) },
                    )
                }
                item {
                    ResultSection(state)
                }
                item {
                    ValidationIssuesSection(state.validationIssues)
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp, horizontal = 4.dp),

            )
        }
    }
}

@Composable
private fun ConfigStatusSection(
    state: AnalyzerContract.State,
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
    state: AnalyzerContract.State,
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
                text = if (state.processingStatus is ProcessingStatus.GeneratingConfig) {
                    "Генерация..."
                } else {
                    "Сгенерировать конфигурацию"
                },
            )
        }
    }
}

@Composable
private fun MessageProcessingSection(
    state: AnalyzerContract.State,
    onMessagesSelected: (AnalyzerFileReference) -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit,
) {
    DashboardSection {
        AnalyzerSectionHeader(
            title = "Обработка сообщений",
            description = "Большой CSV выбирается как файл на диске; содержимое не читается в UI.",
        )
        FilePickerTile(
            title = "Файл сообщений CSV",
            file = state.selectedMessagesFile,
            purpose = AnalyzerFilePurpose.Messages,
            onFilePicked = onMessagesSelected,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ElevatedButton(
                enabled = state.canStartProcessing,
                onClick = onStart,
            ) {
                Icon(MaterialSymbols.Rounded.Play_arrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Запустить")
            }
            OutlinedButton(
                enabled = state.processingStatus is ProcessingStatus.Running,
                onClick = onCancel,
            ) {
                Icon(MaterialSymbols.Rounded.Cancel, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Отменить")
            }
        }
        ProcessingProgress(state)
    }
}

@Composable
private fun ProcessingProgress(state: AnalyzerContract.State) {
    val isRunning = state.processingStatus is ProcessingStatus.Running
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LinearProgressIndicator(
            progress = { state.progressFraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = when (state.processingStatus) {
                ProcessingStatus.Idle -> "Обработка еще не запускалась"
                ProcessingStatus.GeneratingConfig -> "Генерируется skeleton-конфигурация"
                ProcessingStatus.ValidatingConfig -> "Выполняется skeleton-валидация"
                ProcessingStatus.Running -> "Обработано строк: ${state.processedRows} из ${state.totalRowsHint ?: "?"}"
                ProcessingStatus.Completed -> "Обработка завершена: ${state.processedRows} строк"
                ProcessingStatus.Cancelled -> "Обработка отменена"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResultSection(state: AnalyzerContract.State) {
    DashboardSection {
        AnalyzerSectionHeader(
            title = "Результат",
            description = "Пути будущих выходных файлов после обработки.",
        )
        ResultRow(
            label = "CSV",
            value = state.outputCsvPath ?: "Пока не создан",
        )
        ResultRow(
            label = "Лог",
            value = state.logPath ?: "Пока не создан",
        )
        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ValidationIssuesSection(issues: List<ValidationIssue>) {
    DashboardSection {
        AnalyzerSectionHeader(
            title = "Ошибки и предупреждения",
            description = "Список validation issues, который позже будет связан с JSON path и CSV-строками.",
        )
        if (issues.isEmpty()) {
            Text(
                text = "Нет сообщений валидации",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                issues.forEach { issue ->
                    ListItem(
                        headlineContent = { Text(issue.message) },
                        supportingContent = { Text(issue.location) },
                        leadingContent = {
                            Icon(
                                imageVector = if (issue.severity == ValidationIssueSeverity.Error) {
                                    MaterialSymbols.Rounded.Error
                                } else {
                                    MaterialSymbols.Rounded.Description
                                },
                                contentDescription = null,
                                tint = if (issue.severity == ValidationIssueSeverity.Error) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FilePickerTile(
    title: String,
    file: AnalyzerFileReference?,
    purpose: AnalyzerFilePurpose,
    onFilePicked: (AnalyzerFileReference) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val borderColor = MaterialTheme.colorScheme.outline
    Box(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)),
                    ),
                )
            }
            .fileDropTarget(purpose, onFilePicked)
            .clickable {
                scope.launch {
                    val pickedFile = withContext(Dispatchers.IO) { pickFileReference(purpose) }
                    if (pickedFile != null) onFilePicked(pickedFile)
                }
            }
            .padding(14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Folder_open,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (file == null) {
                Text(
                    text = "Перетащите файл или нажмите для выбора",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${formatBytes(file.sizeBytes)} · ${file.path}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSection(content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

@Composable
private fun StatusIcon(
    status: ConfigStatus,
    isLoading: Boolean,
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        } else {
            Icon(
                imageVector = when (status) {
                    ConfigStatus.Missing -> MaterialSymbols.Rounded.Description
                    ConfigStatus.Valid -> MaterialSymbols.Rounded.Check_circle
                    ConfigStatus.Invalid -> MaterialSymbols.Rounded.Error
                },
                contentDescription = null,
                tint = when (status) {
                    ConfigStatus.Missing -> MaterialTheme.colorScheme.onSurfaceVariant
                    ConfigStatus.Valid -> MaterialTheme.colorScheme.primary
                    ConfigStatus.Invalid -> MaterialTheme.colorScheme.error
                },
            )
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssistChip(
            onClick = {},
            enabled = false,
            label = { Text(label) },
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun ConfigStatus.label(): String = when (this) {
    ConfigStatus.Missing -> "Конфигурация отсутствует"
    ConfigStatus.Valid -> "Конфигурация валидна"
    ConfigStatus.Invalid -> "Конфигурация содержит ошибки"
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1_024 -> "$bytes B"
    bytes < 1_048_576 -> "${bytes / 1_024} KB"
    else -> "${bytes / 1_048_576} MB"
}
