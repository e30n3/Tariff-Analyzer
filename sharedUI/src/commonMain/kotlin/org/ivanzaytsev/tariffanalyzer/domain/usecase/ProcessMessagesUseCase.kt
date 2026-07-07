package org.ivanzaytsev.tariffanalyzer.domain.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessMessagesRequest
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingUpdate
import kotlin.time.Duration.Companion.milliseconds

class ProcessMessagesUseCase {
    operator fun invoke(request: ProcessMessagesRequest): Flow<ProcessingUpdate> = flow {
        val totalRowsHint = 1_000L
        for (step in 1..5) {
            delay(120.milliseconds)
            emit(
                ProcessingUpdate.Progress(
                    processedRows = step * 200L,
                    totalRowsHint = totalRowsHint,
                    progressFraction = step / 5f,
                ),
            )
        }
        val basePath = request.messagesFile.path.substringBeforeLast('.', missingDelimiterValue = request.messagesFile.path)
        emit(
            ProcessingUpdate.Completed(
                processedRows = totalRowsHint,
                outputCsvPath = "${basePath}_analyzed.csv",
                logPath = "${basePath}_processing.log",
            ),
        )
    }
}
