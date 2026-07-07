package org.ivanzaytsev.tariffanalyzer.presentation.configuration.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Upload_file
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.presentation.configuration.ConfigurationContract
import org.ivanzaytsev.tariffanalyzer.presentation.configuration.ConfigurationContract.OperationStatus
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.FilePickerTile

@Composable
fun InitialImportSection(
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
