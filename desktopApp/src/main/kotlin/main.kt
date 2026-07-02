import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess
import org.ivanzaytsev.tariffanalyzer.App
import org.ivanzaytsev.tariffanalyzer.di.initKoin
import java.nio.charset.Charset

/** Пишет байты сразу в оба потока — оригинальный (консоль) и файл лога. */
private class TeeOutputStream(
    private val original: OutputStream,
    private val file: OutputStream,
) : OutputStream() {
    override fun write(b: Int) {
        original.write(b)
        file.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        original.write(b, off, len)
        file.write(b, off, len)
    }

    override fun flush() {
        original.flush()
        file.flush()
    }
}

/**
 * Настраивает лог-файл в %LOCALAPPDATA% (гарантированно доступен на запись
 * даже без прав администратора, в отличие от папки самого приложения,
 * которая может лежать на read-only/сетевом ресурсе) и дублирует туда
 * весь stdout/stderr, чтобы после падения можно было прислать лог,
 * даже если консоль уже закрылась.
 */
private fun setupCrashLogging(): File {
    val baseDir = System.getenv("LOCALAPPDATA")?.let(::File)
        ?: File(System.getProperty("user.home"), "AppData/Local")
    val logDir = File(baseDir, "TariffAnalyzer/logs")
    logDir.mkdirs()

    val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
    val logFile = File(logDir, "startup_$timestamp.log")
    val fileStream = FileOutputStream(logFile, true)

    System.setOut(PrintStream(TeeOutputStream(System.out, fileStream), true, "UTF-8"))
    System.setErr(PrintStream(TeeOutputStream(System.err, fileStream), true, "UTF-8"))

    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        System.err.println("[FATAL] Необработанное исключение в потоке '${thread.name}'")
        throwable.printStackTrace(System.err)
    }

    println("=== Tariff Analyzer — диагностический лог запуска ===")
    println("Время: ${Date()}")
    println("Лог-файл: ${logFile.absolutePath}")
    println("ОС: ${System.getProperty("os.name")} ${System.getProperty("os.version")} (${System.getProperty("os.arch")})")
    println("Java: ${System.getProperty("java.version")} / ${System.getProperty("java.vendor")}")
    println("java.home: ${System.getProperty("java.home")}")
    println("Пользователь: ${System.getProperty("user.name")}")
    println("Рабочая директория: ${System.getProperty("user.dir")}")
    println("Каталог приложения (compose.application.dir): ${System.getProperty("compose.application.dir")}")
    println("Кодировка путей/аргументов (sun.jnu.encoding): ${System.getProperty("sun.jnu.encoding")}")
    println("Кодировка файлов (file.encoding): ${Charset.defaultCharset().displayName()}")
    println("Локаль: ${System.getProperty("user.language")}_${System.getProperty("user.country")}")

    val nonAsciiPathParts = listOfNotNull(
        System.getProperty("user.dir"),
        System.getProperty("user.name"),
        System.getProperty("compose.application.dir"),
    ).filter { it.any { c -> c.code > 127 } }
    if (nonAsciiPathParts.isNotEmpty()) {
        println(
            "ВНИМАНИЕ: путь/имя пользователя содержит не-ASCII символы (например, кириллицу): " +
                nonAsciiPathParts.joinToString(", "),
        )
        println(
            "Если приложение не запускается именно на этой машине — попробуйте скопировать " +
                "папку в путь без кириллицы (например, C:\\Temp\\TariffAnalyzer) и запустить оттуда. " +
                "Известная проблема: при системной кодовой странице, не поддерживающей кириллицу " +
                "(Language for non-Unicode programs != Russian), загрузка нативных библиотек Skia/Compose " +
                "по такому пути может тихо падать.",
        )
    }
    println("=======================================================")

    return logFile
}

fun main() {
    val logFile = setupCrashLogging()
    try {
        println("[1/2] Инициализация Koin...")
        initKoin()
        println("[1/2] Koin инициализирован")

        println("[2/2] Запуск Compose-приложения...")
        application {
            Window(
                title = "Tariff Analyzer",
                state = rememberWindowState(width = 800.dp, height = 600.dp),
                onCloseRequest = ::exitApplication,
            ) {
                window.minimumSize = Dimension(350, 600)
                App()
            }
        }
        println("Приложение завершилось штатно")
    } catch (t: Throwable) {
        System.err.println("[FATAL] Ошибка при старте приложения. Подробности выше и в файле: ${logFile.absolutePath}")
        t.printStackTrace(System.err)
        System.err.println("Нажмите Enter, чтобы закрыть окно...")
        readlnOrNull()
        exitProcess(1)
    }
}
