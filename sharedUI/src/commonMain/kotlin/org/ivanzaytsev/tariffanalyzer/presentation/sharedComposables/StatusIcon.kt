package org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Check_circle
import com.composables.icons.materialsymbols.rounded.Description
import com.composables.icons.materialsymbols.rounded.Error
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus

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
