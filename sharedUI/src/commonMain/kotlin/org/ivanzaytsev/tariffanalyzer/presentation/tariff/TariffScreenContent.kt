package org.ivanzaytsev.tariffanalyzer.presentation.tariff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Rotate_right
import com.composables.icons.materialsymbols.rounded.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.domain.model.Tariff
import org.jetbrains.compose.resources.stringResource
import tariff_analyzer.sharedui.generated.resources.Res
import tariff_analyzer.sharedui.generated.resources.data_and_minutes
import tariff_analyzer.sharedui.generated.resources.empty_file
import tariff_analyzer.sharedui.generated.resources.file_selected
import tariff_analyzer.sharedui.generated.resources.input_file_description
import tariff_analyzer.sharedui.generated.resources.input_file_title
import tariff_analyzer.sharedui.generated.resources.price_per_month
import tariff_analyzer.sharedui.generated.resources.pick_file_hint
import tariff_analyzer.sharedui.generated.resources.refresh
import tariff_analyzer.sharedui.generated.resources.retry
import tariff_analyzer.sharedui.generated.resources.settings_title
import tariff_analyzer.sharedui.generated.resources.tariffs_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TariffScreenContent(
    state: TariffContract.State,
    snackbarHostState: SnackbarHostState,
    onAction: (TariffContract.Action) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.tariffs_title)) },
                actions = {
                    IconButton(onClick = { onAction(TariffContract.Action.Refresh) }) {
                        Icon(
                            MaterialSymbols.Rounded.Rotate_right,
                            contentDescription = stringResource(Res.string.refresh),
                        )
                    }
                    IconButton(onClick = { onAction(TariffContract.Action.OpenSettings) }) {
                        Icon(
                            MaterialSymbols.Rounded.Settings,
                            contentDescription = stringResource(Res.string.settings_title),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            var pickedFile by remember { mutableStateOf<PickedFile?>(null) }
            FileDropZone(
                pickedFile = pickedFile,
                onFilePicked = { pickedFile = it },
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    state.isLoading && state.tariffs.isEmpty() ->
                        CircularProgressIndicator()

                    state.error != null && state.tariffs.isEmpty() ->
                        ErrorState(
                            message = state.error,
                            onRetry = { onAction(TariffContract.Action.Load) },
                        )

                    else -> TariffList(
                        tariffs = state.tariffs,
                        selectedId = state.selectedId,
                        onSelect = { onAction(TariffContract.Action.Select(it)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FileDropZone(
    pickedFile: PickedFile?,
    onFilePicked: (PickedFile) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val borderColor = MaterialTheme.colorScheme.outline
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnalyzerSectionHeader(
            title = stringResource(Res.string.input_file_title),
            description = stringResource(Res.string.input_file_description),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .drawBehind {
                    drawRoundRect(
                        color = borderColor,
                        cornerRadius = CornerRadius(8.dp.toPx()),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
                        ),
                    )
                }
                .fileDropTarget(onFilePicked)
                .clickable {
                    scope.launch {
                        val file = withContext(Dispatchers.IO) { pickAndReadFile() }
                        if (file != null) onFilePicked(file)
                    }
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.pick_file_hint),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        if (pickedFile != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = pickedFile.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = {
                                Text(
                                    text = stringResource(Res.string.file_selected),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                        )
                    }
                    Text(
                        text = pickedFile.content.take(100).ifBlank {
                            stringResource(Res.string.empty_file)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

@Composable
private fun TariffList(
    tariffs: List<Tariff>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(tariffs, key = { it.id }) { tariff ->
            TariffCard(
                tariff = tariff,
                selected = tariff.id == selectedId,
                onClick = { onSelect(tariff.id) },
            )
        }
    }
}

@Composable
private fun TariffCard(
    tariff: Tariff,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = if (selected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        CardDefaults.cardColors()
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = colors,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tariff.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = tariff.provider,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(
                        Res.string.data_and_minutes,
                        tariff.dataGb,
                        tariff.callMinutes,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = stringResource(
                    Res.string.price_per_month,
                    "$${tariff.monthlyPrice}",
                ),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp),
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
        )
        ElevatedButton(onClick = onRetry) {
            Text(stringResource(Res.string.retry))
        }
    }
}
