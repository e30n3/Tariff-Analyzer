package org.ivanzaytsev.tariffanalyzer.domain.repository

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import org.ivanzaytsev.tariffanalyzer.data.csv.SemicolonCsvParser
import org.ivanzaytsev.tariffanalyzer.domain.analyzer.MessageAnalyzer
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
        val tempUtf8Csv = File(outputFiles.outputUtf8CsvPath + PARTIAL_SUFFIX)
        val tempLog = File(outputFiles.logPath + PARTIAL_SUFFIX)
        var processedRows = 0L
        var processedBytes = 0L

        try {
            val analyzer = MessageAnalyzer(config)
            CsvRecordReader(inputFile, WINDOWS_1251).use { reader ->
                BufferedWriter(OutputStreamWriter(FileOutputStream(tempCsv), WINDOWS_1251)).use { csvWriter ->
                    BufferedWriter(OutputStreamWriter(FileOutputStream(tempUtf8Csv), Charsets.UTF_8)).use { utf8CsvWriter ->
                        BufferedWriter(OutputStreamWriter(FileOutputStream(tempLog), Charsets.UTF_8)).use { logWriter ->
                            logWriter.write("Tariff Analyzer processing log")
                            logWriter.newLine()
                            logWriter.write("input=${inputFile.absolutePath}")
                            logWriter.newLine()

                            val headerRecord = reader.readRecord()
                                ?: error("CSV-файл сообщений пуст.")
                            processedBytes += headerRecord.byteSizeForProgress()
                            val header = parseSingleRecord(headerRecord)
                            validateRequiredColumns(header)
                            writeCsvRecord(csvWriter, header + AnalyzerOutputColumns.all)
                            writeCsvRecord(utf8CsvWriter, header + AnalyzerOutputColumns.all)

                            var record = reader.readRecord()
                            while (record != null) {
                                currentCoroutineContext().ensureActive()
                                processedBytes += record.byteSizeForProgress()
                                processedRows++

                                val values = parseSingleRecord(record)
                                val valuesByColumn = header.mapIndexed { index, column ->
                                    column to values.getOrElse(index) { "" }
                                }.toMap()
                                val analysis = analyzer.analyze(
                                    MessageCsvRow(
                                        csvLineNumber = processedRows + 1L,
                                        valuesByColumn = valuesByColumn,
                                    ),
                                )

                                val outputValues = values + analysis.additionalValues
                                writeCsvRecord(csvWriter, outputValues)
                                writeCsvRecord(utf8CsvWriter, outputValues)
                                analysis.logEntries.forEach { entry ->
                                    logWriter.write(entry)
                                    logWriter.newLine()
                                }

                                if (processedRows % PROGRESS_STEP == 0L) {
                                    emit(
                                        ProcessingUpdate.Progress(
                                            processedRows = processedRows,
                                            totalRowsHint = null,
                                            progressFraction = progressFraction(processedBytes, inputFile.length()),
                                        ),
                                    )
                                }

                                record = reader.readRecord()
                            }
                        }
                    }
                }
            }

            renameTempFile(tempCsv, File(outputFiles.outputCsvPath))
            renameTempFile(tempUtf8Csv, File(outputFiles.outputUtf8CsvPath))
            renameTempFile(tempLog, File(outputFiles.logPath))
            emit(
                ProcessingUpdate.Completed(
                    processedRows = processedRows,
                    outputCsvPath = outputFiles.outputCsvPath,
                    logPath = outputFiles.logPath,
                ),
            )
        } catch (throwable: Throwable) {
            tempCsv.delete()
            tempUtf8Csv.delete()
            tempLog.delete()
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

    fun readRecord(): String? {
        val record = StringBuilder()
        var inQuotes = false
        var hasContent = false

        while (true) {
            val value = reader.read()
            if (value < 0) {
                return if (hasContent) record.toString() else null
            }

            hasContent = true
            val char = value.toChar()
            when {
                char == '"' -> {
                    reader.mark(1)
                    val next = reader.read()
                    if (inQuotes && next == '"'.code) {
                        record.append("\"\"")
                    } else {
                        if (next >= 0) {
                            reader.reset()
                        }
                        inQuotes = !inQuotes
                        record.append(char)
                    }
                }

                char == '\n' && !inQuotes -> return record.toString()

                char == '\r' && !inQuotes -> {
                    reader.mark(1)
                    val next = reader.read()
                    if (next != '\n'.code && next >= 0) {
                        reader.reset()
                    }
                    return record.toString()
                }

                else -> record.append(char)
            }
        }
    }

    override fun close() {
        reader.close()
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

private fun String.byteSizeForProgress(): Long = toByteArray(WINDOWS_1251).size + 1L

private fun progressFraction(processedBytes: Long, totalBytes: Long): Float =
    if (totalBytes <= 0L) {
        0f
    } else {
        (processedBytes.toDouble() / totalBytes.toDouble()).toFloat().coerceIn(0f, 1f)
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
