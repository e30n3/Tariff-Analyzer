package org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Fact_check
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationContract
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.DashboardSection
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.StatusIcon
import org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables.label

@Composable
fun ConfigStatusSection(
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
