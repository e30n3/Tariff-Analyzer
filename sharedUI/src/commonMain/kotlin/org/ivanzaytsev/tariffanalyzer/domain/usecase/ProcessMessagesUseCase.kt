package org.ivanzaytsev.tariffanalyzer.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessMessagesRequest
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingUpdate
import org.ivanzaytsev.tariffanalyzer.domain.repository.AnalyzerConfigRepository
import org.ivanzaytsev.tariffanalyzer.domain.repository.MessageAnalysisFileProcessor

class ProcessMessagesUseCase(
    private val analyzerConfigRepository: AnalyzerConfigRepository,
    private val fileProcessor: MessageAnalysisFileProcessor,
) {
    operator fun invoke(request: ProcessMessagesRequest): Flow<ProcessingUpdate> = flow {
        val config = analyzerConfigRepository.loadConfig()
        emitAll(fileProcessor.process(request, config))
    }
}
