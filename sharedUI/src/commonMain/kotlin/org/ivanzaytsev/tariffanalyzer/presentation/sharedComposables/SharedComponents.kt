package org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables

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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.composables.icons.materialsymbols.rounded.Check_circle
import com.composables.icons.materialsymbols.rounded.Description
import com.composables.icons.materialsymbols.rounded.Error
import com.composables.icons.materialsymbols.rounded.Folder_open
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssue
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssueSeverity
import org.ivanzaytsev.tariffanalyzer.presentation.filepicker.fileDropTarget
import org.ivanzaytsev.tariffanalyzer.presentation.filepicker.pickFileReference

@Composable
fun AnalyzerContentScaffold(
    snackbarHostState: SnackbarHostState,
    content: LazyListScope.() -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                content = content,
            )
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
fun DashboardSection(content: @Composable ColumnScope.() -> Unit) {
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
fun FilePickerTile(
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
fun ValidationIssuesSection(issues: List<ValidationIssue>) {
    DashboardSection {
        org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader(
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
fun StatusIcon(
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
fun ResultRow(
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

fun ConfigStatus.label(): String = when (this) {
    ConfigStatus.Missing -> "Конфигурация отсутствует"
    ConfigStatus.Valid -> "Конфигурация валидна"
    ConfigStatus.Invalid -> "Конфигурация содержит ошибки"
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1_024 -> "$bytes B"
    bytes < 1_048_576 -> "${bytes / 1_024} KB"
    else -> "${bytes / 1_048_576} MB"
}
