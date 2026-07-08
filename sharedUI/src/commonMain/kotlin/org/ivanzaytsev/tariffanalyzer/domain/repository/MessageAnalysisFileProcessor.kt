package org.ivanzaytsev.tariffanalyzer.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerConfig
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessMessagesRequest
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingUpdate

interface MessageAnalysisFileProcessor {
    fun process(
        request: ProcessMessagesRequest,
        config: AnalyzerConfig,
    ): Flow<ProcessingUpdate>
}

expect fun createMessageAnalysisFileProcessor(): MessageAnalysisFileProcessor
