package scripts

import java.io.BufferedReader
import java.io.BufferedWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.system.exitProcess

private val sourceCharset: Charset = Charset.forName("windows-1251")
private val targetCharset: Charset = StandardCharsets.UTF_8

fun main() {
    val sourcePath = readlnOrNull()?.let(Path::of)
        ?: fail("Usage: ./gradlew :scripts:convertCsvWindows1251ToUtf8 --args=\"/absolute/path/to/file.csv\"")

    validateSourcePath(sourcePath)

    val targetPath = sourcePath.resolveUtf8Sibling()
    if (Files.exists(targetPath)) {
        fail("Target file already exists: ${targetPath.absolutePathString()}")
    }

    convertEncoding(
        sourcePath = sourcePath,
        targetPath = targetPath,
    )

    println("Converted: ${sourcePath.absolutePathString()}")
    println("Saved to: ${targetPath.absolutePathString()}")
}

private fun validateSourcePath(sourcePath: Path) {
    when {
        !sourcePath.isAbsolute -> fail("CSV path must be absolute: $sourcePath")
        !sourcePath.isRegularFile() -> fail("CSV file does not exist: ${sourcePath.absolutePathString()}")
        sourcePath.extension.lowercase() != "csv" -> fail("Expected a .csv file: ${sourcePath.absolutePathString()}")
    }
}

private fun Path.resolveUtf8Sibling(): Path {
    val outputName = "${nameWithoutExtension}_UTF-8.$extension"
    return parent?.resolve(outputName)
        ?: fail("CSV file must have a parent directory: $name")
}

private fun convertEncoding(
    sourcePath: Path,
    targetPath: Path,
) {
    Files.newBufferedReader(sourcePath, sourceCharset).use { reader ->
        Files.newBufferedWriter(targetPath, targetCharset).use { writer ->
            reader.copyTo(writer)
        }
    }
}

private fun BufferedReader.copyTo(writer: BufferedWriter) {
    val buffer = CharArray(DEFAULT_BUFFER_SIZE)
    while (true) {
        val readCount = read(buffer)
        if (readCount < 0) break
        writer.write(buffer, 0, readCount)
    }
}

private fun fail(message: String): Nothing {
    System.err.println(message)
    exitProcess(1)
}
