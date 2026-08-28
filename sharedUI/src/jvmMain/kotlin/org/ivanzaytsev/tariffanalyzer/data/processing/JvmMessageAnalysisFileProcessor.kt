package org.ivanzaytsev.tariffanalyzer.domain.repository

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import org.ivanzaytsev.tariffanalyzer.data.csv.SemicolonCsvParser
import org.ivanzaytsev.tariffanalyzer.domain.analyzer.MessageAnalyzer
import org.ivanzaytsev.tariffanalyzer.domain.analyzer.AnalysisSummaryAccumulator
import org.ivanzaytsev.tariffanalyzer.domain.analyzer.MessageCsvRow
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerConfig
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerInputColumns
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerOutputColumns
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessMessagesRequest
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingUpdate
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

actual fun createMessageAnalysisFileProcessor(): MessageAnalysisFileProcessor = JvmMessageAnalysisFileProcessor()

private class JvmMessageAnalysisFileProcessor(
    private val csvParser: SemicolonCsvParser = SemicolonCsvParser(),
) : MessageAnalysisFileProcessor {

    override fun process(
        request: ProcessMessagesRequest,
        config: AnalyzerConfig,
    ): Flow<ProcessingUpdate> = flow {
        val inputFile = File(request.messagesFile.path)
        require(inputFile.isFile) { "Файл сообщений не найден: ${request.messagesFile.path}" }

        val outputFiles = createOutputFiles(request.messagesFile.name)
        val tempCsv = File(outputFiles.outputCsvPath + PARTIAL_SUFFIX)
        val tempUtf8Csv = outputFiles.outputUtf8CsvPath
            .takeIf { request.debugMode }
            ?.let { File(it + PARTIAL_SUFFIX) }
        val tempLog = outputFiles.logPath
            .takeIf { request.debugMode }
            ?.let { File(it + PARTIAL_SUFFIX) }
        var processedRows = 0L
        var processedBytes = 0L
        var processedDataBytes = 0L
        val summaryAccumulator = AnalysisSummaryAccumulator()

        try {
            val analyzer = MessageAnalyzer(config)
            CsvRecordReader(inputFile, WINDOWS_1251).use { reader ->
                BufferedWriter(OutputStreamWriter(FileOutputStream(tempCsv), WINDOWS_1251)).use { csvWriter ->
                    DebugOutputWriters.open(tempUtf8Csv, tempLog).use { debugWriters ->
                        debugWriters?.logWriter?.run {
                            write("Tariff Analyzer processing log")
                            newLine()
                            write("input=${inputFile.absolutePath}")
                            newLine()
                        }

                        val headerRecord = reader.readRecord()
                            ?: error("CSV-файл сообщений пуст.")
                        processedBytes += headerRecord.byteSize
                        val totalDataBytes = (inputFile.length() - headerRecord.byteSize).coerceAtLeast(0L)
                        val header = parseSingleRecord(headerRecord.value)
                        validateRequiredColumns(header)
                        writeCsvRecord(csvWriter, header + AnalyzerOutputColumns.all)
                        debugWriters?.utf8CsvWriter?.let {
                            writeCsvRecord(it, header + AnalyzerOutputColumns.all)
                        }

                        var record = reader.readRecord()
                        while (record != null) {
                            currentCoroutineContext().ensureActive()
                            processedBytes += record.byteSize
                            processedDataBytes += record.byteSize
                            processedRows++

                            val values = parseSingleRecord(record.value)
                            val valuesByColumn = header.mapIndexed { index, column ->
                                column to values.getOrElse(index) { "" }
                            }.toMap()
                            val analysis = analyzer.analyze(
                                MessageCsvRow(
                                    csvLineNumber = processedRows + 1L,
                                    valuesByColumn = valuesByColumn,
                                ),
                            )
                            summaryAccumulator.add(analysis)

                            val outputValues = values + analysis.additionalValues
                            writeCsvRecord(csvWriter, outputValues)
                            debugWriters?.utf8CsvWriter?.let {
                                writeCsvRecord(it, outputValues)
                            }
                            debugWriters?.logWriter?.let { logWriter ->
                                analysis.logEntries.forEach { entry ->
                                    logWriter.write(entry)
                                    logWriter.newLine()
                                }
                            }

                            if (processedRows % PROGRESS_STEP == 0L) {
                                emit(
                                    ProcessingUpdate.Progress(
                                        processedRows = processedRows,
                                        totalRowsHint = estimateTotalRows(
                                            processedRows = processedRows,
                                            processedDataBytes = processedDataBytes,
                                            totalDataBytes = totalDataBytes,
                                        ),
                                        progressFraction = progressFraction(processedBytes, inputFile.length()),
                                    ),
                                )
                            }

                            record = reader.readRecord()
                        }
                    }
                }
            }

            renameTempFile(tempCsv, File(outputFiles.outputCsvPath))
            tempUtf8Csv?.let {
                renameTempFile(it, File(outputFiles.outputUtf8CsvPath))
            }
            tempLog?.let {
                renameTempFile(it, File(outputFiles.logPath))
            }
            emit(
                ProcessingUpdate.Completed(
                    processedRows = processedRows,
                    outputCsvPath = outputFiles.outputCsvPath,
                    logPath = outputFiles.logPath.takeIf { request.debugMode },
                    summary = summaryAccumulator.build(),
                ),
            )
        } catch (throwable: Throwable) {
            tempCsv.delete()
            tempUtf8Csv?.delete()
            tempLog?.delete()
            throw throwable
        }
    }.flowOn(Dispatchers.IO)

    private fun parseSingleRecord(record: String): List<String> =
        csvParser.parse(record).firstOrNull().orEmpty()

    private fun validateRequiredColumns(header: List<String>) {
        val missingColumns = AnalyzerInputColumns.required.filterNot { required ->
            header.any { it.trim() == required }
        }
        require(missingColumns.isEmpty()) {
            "В CSV сообщений отсутствуют обязательные колонки: ${missingColumns.joinToString()}."
        }
    }

    private fun createOutputFiles(inputName: String): OutputFiles {
        val appDir = System.getProperty("compose.application.dir")
            ?.takeIf { it.isNotBlank() }
            ?: System.getProperty("user.dir")
        val outputDir = File(appDir).also { it.mkdirs() }
        val baseName = inputName.substringBeforeLast('.', missingDelimiterValue = inputName)
            .replace(Regex("""[^\p{L}\p{N}._-]+"""), "_")
            .ifBlank { "messages" }
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return OutputFiles(
            outputCsvPath = File(outputDir, "${baseName}_analyzed_$timestamp.csv").absolutePath,
            outputUtf8CsvPath = File(outputDir, "${baseName}_analyzed_${timestamp}_utf8.csv").absolutePath,
            logPath = File(outputDir, "${baseName}_processing_$timestamp.log").absolutePath,
        )
    }
}

private class CsvRecordReader(
    file: File,
    charset: Charset,
) : Closeable {
    private val reader = BufferedReader(InputStreamReader(FileInputStream(file), charset))

    fun readRecord(): CsvRecord? {
        val record = StringBuilder()
        var inQuotes = false
        var hasContent = false
        var byteSize = 0L

        while (true) {
            val value = reader.read()
            if (value < 0) {
                return if (hasContent) CsvRecord(record.toString(), byteSize) else null
            }

            hasContent = true
            byteSize++
            when (val char = value.toChar()) {
                '"' -> {
                    reader.mark(1)
                    val next = reader.read()
                    if (inQuotes && next == '"'.code) {
                        byteSize++
                        record.append("\"\"")
                    } else {
                        if (next >= 0) {
                            reader.reset()
                        }
                        inQuotes = !inQuotes
                        record.append(char)
                    }
                }
                '\n' if !inQuotes -> return CsvRecord(record.toString(), byteSize)
                '\r' if !inQuotes -> {
                    reader.mark(1)
                    val next = reader.read()
                    if (next == '\n'.code) {
                        byteSize++
                    } else if (next >= 0) {
                        reader.reset()
                    }
                    return CsvRecord(record.toString(), byteSize)
                }
                else -> record.append(char)
            }
        }
    }

    override fun close() {
        reader.close()
    }
}

private data class CsvRecord(
    val value: String,
    val byteSize: Long,
)

private class DebugOutputWriters private constructor(
    val utf8CsvWriter: BufferedWriter,
    val logWriter: BufferedWriter,
) : Closeable {

    override fun close() {
        var failure: Throwable? = null
        runCatching { logWriter.close() }
            .onFailure { failure = it }
        runCatching { utf8CsvWriter.close() }
            .onFailure { throwable ->
                failure?.addSuppressed(throwable) ?: run { failure = throwable }
            }
        failure?.let { throw it }
    }

    companion object {
        fun open(
            utf8File: File?,
            logFile: File?,
        ): DebugOutputWriters? {
            if (utf8File == null || logFile == null) return null

            val utf8Writer = BufferedWriter(OutputStreamWriter(FileOutputStream(utf8File), Charsets.UTF_8))
            return try {
                DebugOutputWriters(
                    utf8CsvWriter = utf8Writer,
                    logWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(logFile), Charsets.UTF_8)),
                )
            } catch (throwable: Throwable) {
                utf8Writer.close()
                throw throwable
            }
        }
    }
}

private fun writeCsvRecord(writer: BufferedWriter, values: List<String>) {
    writer.write(values.joinToString(";") { it.escapeCsvValue() })
    writer.newLine()
}

private fun String.escapeCsvValue(): String {
    val escaped = replace("\"", "\"\"")
    return if (any { it == ';' || it == '"' || it == '\r' || it == '\n' }) {
        "\"$escaped\""
    } else {
        escaped
    }
}

private fun progressFraction(processedBytes: Long, totalBytes: Long): Float =
    if (totalBytes <= 0L) {
        0f
    } else {
        (processedBytes.toDouble() / totalBytes.toDouble()).toFloat().coerceIn(0f, 1f)
    }

private fun estimateTotalRows(
    processedRows: Long,
    processedDataBytes: Long,
    totalDataBytes: Long,
): Long? = if (processedRows <= 0L || processedDataBytes <= 0L || totalDataBytes <= 0L) {
    null
} else {
    ceil(processedRows.toDouble() * totalDataBytes.toDouble() / processedDataBytes.toDouble())
        .toLong()
        .coerceAtLeast(processedRows)
}

private fun renameTempFile(tempFile: File, targetFile: File) {
    if (!tempFile.renameTo(targetFile)) {
        error("Не удалось сохранить файл результата: ${targetFile.absolutePath}")
    }
}

private data class OutputFiles(
    val outputCsvPath: String,
    val outputUtf8CsvPath: String,
    val logPath: String,
)

private val WINDOWS_1251: Charset = Charset.forName("windows-1251")
private const val PROGRESS_STEP = 1_000L
private const val PARTIAL_SUFFIX = ".partial"
