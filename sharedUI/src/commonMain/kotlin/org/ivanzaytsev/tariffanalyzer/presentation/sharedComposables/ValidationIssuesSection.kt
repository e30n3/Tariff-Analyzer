package org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Description
import com.composables.icons.materialsymbols.rounded.Error
import org.ivanzaytsev.tariffanalyzer.designsystem.components.AnalyzerSectionHeader
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssue
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssueSeverity

@Composable
fun ValidationIssuesSection(issues: List<ValidationIssue>) {
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
