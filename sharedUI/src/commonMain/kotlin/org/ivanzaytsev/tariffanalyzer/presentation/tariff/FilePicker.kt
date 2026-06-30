package org.ivanzaytsev.tariffanalyzer.presentation.tariff

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** A file selected by the user, with its textual content already read. */
data class PickedFile(
    val name: String,
    val content: String,
)

/**
 * Opens a native file chooser, reads the selected file as text and returns it.
 * Returns `null` if the user cancels. Must be called off the main thread
 * because the dialog blocks until the user makes a choice.
 */
expect fun pickAndReadFile(): PickedFile?

/**
 * Makes the receiver a drop target that accepts a single file dragged from the
 * OS. The dropped file is read as text and delivered through [onFile].
 */
@Composable
expect fun Modifier.fileDropTarget(onFile: (PickedFile) -> Unit): Modifier
