package org.ivanzaytsev.tariffanalyzer.domain.repository

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatusResult

interface AnalyzerConfigRepository {
    suspend fun getConfigStatus(): ConfigStatusResult
    suspend fun saveConfigStatus(result: ConfigStatusResult)
}
