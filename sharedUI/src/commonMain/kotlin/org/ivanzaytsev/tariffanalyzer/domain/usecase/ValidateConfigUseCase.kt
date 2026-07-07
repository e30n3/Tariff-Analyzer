package org.ivanzaytsev.tariffanalyzer.domain.usecase

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatusResult
import org.ivanzaytsev.tariffanalyzer.domain.repository.AnalyzerConfigRepository

class ValidateConfigUseCase(
    private val analyzerConfigRepository: AnalyzerConfigRepository,
) {
    suspend operator fun invoke(): ConfigStatusResult = analyzerConfigRepository.validateConfig()
}
