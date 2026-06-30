package org.ivanzaytsev.tariffanalyzer.presentation.tariff

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import java.awt.FileDialog
import java.awt.Frame
import java.awt.datatransfer.DataFlavor
import java.io.File

actual fun pickAndReadFile(): PickedFile? {
    val dialog = FileDialog(null as Frame?, "Выберите файл", FileDialog.LOAD)
    dialog.isVisible = true
    val directory = dialog.directory ?: return null
    val name = dialog.file ?: return null
    val file = File(directory, name)
    return file.readAsPickedFileOrNull()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.fileDropTarget(onFile: (PickedFile) -> Unit): Modifier {
    val currentOnFile = rememberUpdatedState(onFile)
    val target = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val transferable = event.awtTransferable
                if (!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return false
                return runCatching {
                    @Suppress("UNCHECKED_CAST")
                    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                    val picked = files.firstOrNull()?.readAsPickedFileOrNull() ?: return false
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

private fun File.readAsPickedFileOrNull(): PickedFile? =
    runCatching { PickedFile(name = name, content = readText()) }.getOrNull()
