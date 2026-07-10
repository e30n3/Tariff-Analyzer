package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.dashboard.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OperatorTableHeader() {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text("Оператор", modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.labelMedium)
        Text("SMS", modifier = Modifier.weight(0.7f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelMedium)
        Text("Расхождения", modifier = Modifier.weight(0.9f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelMedium)
        Text("Доля", modifier = Modifier.weight(0.6f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelMedium)
        Text("Разница", modifier = Modifier.weight(1f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelMedium)
    }
}
