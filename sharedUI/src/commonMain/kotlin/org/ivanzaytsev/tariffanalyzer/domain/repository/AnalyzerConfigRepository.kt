package org.ivanzaytsev.tariffanalyzer.domain.repository

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatusResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.GeneratedConfigResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference

interface AnalyzerConfigRepository {
    suspend fun getConfigStatus(): ConfigStatusResult
    suspend fun generateConfig(
        templatesFile: AnalyzerFileReference,
        tariffFile: AnalyzerFileReference,
    ): GeneratedConfigResult
    suspend fun validateConfig(): ConfigStatusResult
}
