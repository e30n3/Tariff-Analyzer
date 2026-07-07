package org.ivanzaytsev.tariffanalyzer.domain.usecase

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.GeneratedConfigResult
import org.ivanzaytsev.tariffanalyzer.domain.repository.AnalyzerConfigRepository

class GenerateConfigUseCase(
    private val analyzerConfigRepository: AnalyzerConfigRepository,
) {
    suspend operator fun invoke(
        templatesFile: AnalyzerFileReference,
        tariffFile: AnalyzerFileReference,
    ): GeneratedConfigResult = analyzerConfigRepository.generateConfig(templatesFile, tariffFile)
}
