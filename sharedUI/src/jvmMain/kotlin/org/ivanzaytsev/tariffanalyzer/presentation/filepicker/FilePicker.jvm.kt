package org.ivanzaytsev.tariffanalyzer.presentation.filepicker

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import java.awt.FileDialog
import java.awt.Frame
import java.awt.datatransfer.DataFlavor
import java.io.File

actual fun pickFileReference(purpose: AnalyzerFilePurpose): AnalyzerFileReference? {
    val dialog = FileDialog(null as Frame?, "Выберите файл", FileDialog.LOAD)
    dialog.isVisible = true
    val directory = dialog.directory ?: return null
    val name = dialog.file ?: return null
    val file = File(directory, name)
    return file.toFileReference(purpose)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.fileDropTarget(
    purpose: AnalyzerFilePurpose,
    onFile: (AnalyzerFileReference) -> Unit,
): Modifier {
    val currentOnFile = rememberUpdatedState(onFile)
    val target = remember(purpose) {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val transferable = event.awtTransferable
                if (!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return false
                return runCatching {
                    @Suppress("UNCHECKED_CAST")
                    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                    val picked = files.firstOrNull()?.toFileReference(purpose) ?: return false
                    currentOnFile.value(picked)
                    true
                }.getOrDefault(false)
            }
        }
    }
    return dragAndDropTarget(
        shouldStartDragAndDrop = { true },
        target = target,
    )
}

private fun File.toFileReference(purpose: AnalyzerFilePurpose): AnalyzerFileReference? =
    runCatching {
        AnalyzerFileReference(
            name = name,
            path = absolutePath,
            sizeBytes = length(),
            purpose = purpose,
        )
    }.getOrNull()
