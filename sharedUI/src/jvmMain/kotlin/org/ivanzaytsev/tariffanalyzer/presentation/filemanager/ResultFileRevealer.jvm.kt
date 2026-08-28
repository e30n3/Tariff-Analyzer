package org.ivanzaytsev.tariffanalyzer.presentation.filemanager

import java.awt.Desktop
import java.io.File

actual fun revealResultFile(path: String): Result<Unit> = runCatching {
    val file = File(path)
    require(file.isFile) { "Итоговый CSV-файл не найден: $path" }

    val parentDirectory = requireNotNull(file.parentFile) {
        "Не удалось определить папку итогового CSV-файла."
    }
    check(Desktop.isDesktopSupported()) {
        "Открытие файлового менеджера не поддерживается системой."
    }

    val desktop = Desktop.getDesktop()
    val revealed = if (desktop.isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
        runCatching { desktop.browseFileDirectory(file) }.isSuccess
    } else {
        false
    }

    if (!revealed) {
        check(desktop.isSupported(Desktop.Action.OPEN)) {
            "Открытие папки результата не поддерживается системой."
        }
        desktop.open(parentDirectory)
    }
}
