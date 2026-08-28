package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Folder_open

@Composable
fun RevealResultFileButton(onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Icon(MaterialSymbols.Rounded.Folder_open, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Показать в папке")
    }
}
