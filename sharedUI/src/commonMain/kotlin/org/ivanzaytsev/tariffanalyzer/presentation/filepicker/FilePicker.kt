package org.ivanzaytsev.tariffanalyzer.presentation.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference

/**
 * Opens a native file chooser and returns only file metadata.
 * Returns `null` if the user cancels. Must be called off the main thread
 * because the dialog blocks until the user makes a choice.
 */
expect fun pickFileReference(purpose: AnalyzerFilePurpose): AnalyzerFileReference?

/**
 * Makes the receiver a drop target that accepts a single file dragged from the
 * OS. The dropped file is delivered as metadata only.
 */
@Composable
expect fun Modifier.fileDropTarget(
    purpose: AnalyzerFilePurpose,
    onFile: (AnalyzerFileReference) -> Unit,
): Modifier
