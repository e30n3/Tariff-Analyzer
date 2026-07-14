import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.sun.jna.Library
import com.sun.jna.Native
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
import org.ivanzaytsev.tariffanalyzer.domain.repository.SettingsRepository
import java.nio.charset.Charset

/** Writes bytes to both the original console stream and the debug log file. */
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
 * Creates a startup debug log under %LOCALAPPDATA% and mirrors stdout/stderr to it.
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
        System.err.println("[FATAL] Unhandled exception in thread '${thread.name}'")
        printThrowableWithoutLocalizedMessages(throwable, System.err)
    }

    println("=== Tariff Analyzer startup diagnostics ===")
    println("Time: ${Date()}")
    println("Log file: ${logFile.absolutePath}")
    println("OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")} (${System.getProperty("os.arch")})")
    println("Java: ${System.getProperty("java.version")} / ${System.getProperty("java.vendor")}")
    println("java.home: ${System.getProperty("java.home")}")
    println("User: ${System.getProperty("user.name")}")
    println("Working directory: ${System.getProperty("user.dir")}")
    println("Application directory (compose.application.dir): ${System.getProperty("compose.application.dir")}")
    println("Path and argument encoding (sun.jnu.encoding): ${System.getProperty("sun.jnu.encoding")}")
    println("File encoding (file.encoding): ${Charset.defaultCharset().displayName()}")
    println("Locale: ${System.getProperty("user.language")}_${System.getProperty("user.country")}")

    val nonAsciiPathParts = listOfNotNull(
        System.getProperty("user.dir"),
        System.getProperty("user.name"),
        System.getProperty("compose.application.dir"),
    ).filter { it.any { c -> c.code > 127 } }
    if (nonAsciiPathParts.isNotEmpty()) {
        println(
            "WARNING: A path or user name contains non-ASCII characters: " +
                nonAsciiPathParts.joinToString(", "),
        )
        println(
            "If the application fails to start on this machine, copy it to an ASCII-only path " +
                "such as C:\\Temp\\TariffAnalyzer and run it from there. Native Skia/Compose libraries " +
                "may fail to load from a non-ASCII path when the system code page cannot represent it.",
        )
    }
    println("=======================================================")

    return logFile
}

private fun showWindowsDebugConsole() {
    if (!System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) return
    if (!WindowsKernel32.INSTANCE.AllocConsole()) return

    System.setOut(PrintStream(FileOutputStream("CONOUT$"), true, Charsets.UTF_8))
    System.setErr(PrintStream(FileOutputStream("CONOUT$"), true, Charsets.UTF_8))
}

private fun printThrowableWithoutLocalizedMessages(
    throwable: Throwable,
    output: PrintStream,
) {
    var current: Throwable? = throwable
    var causeIndex = 0
    while (current != null) {
        val prefix = if (causeIndex == 0) "Exception" else "Caused by"
        output.println("$prefix: ${current::class.qualifiedName}")
        current.stackTrace.forEach { output.println("\tat $it") }
        current = current.cause
        causeIndex++
    }
}

private interface WindowsKernel32 : Library {
    fun AllocConsole(): Boolean

    companion object {
        val INSTANCE: WindowsKernel32 = Native.load("kernel32", WindowsKernel32::class.java)
    }
}

fun main() {
    val koinApplication = initKoin()
    val debugMode = koinApplication.koin.get<SettingsRepository>().debugMode.value
    val logFile = if (debugMode) {
        showWindowsDebugConsole()
        setupCrashLogging()
    } else {
        null
    }
    try {
        if (debugMode) println("Starting Compose application...")
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
        if (debugMode) println("Application exited normally")
    } catch (t: Throwable) {
        if (debugMode) {
            System.err.println("[FATAL] Application startup failed. Details are available above and in: ${logFile?.absolutePath}")
            printThrowableWithoutLocalizedMessages(t, System.err)
            System.err.println("Press Enter to close this window...")
            readlnOrNull()
        }
        exitProcess(1)
    }
}
