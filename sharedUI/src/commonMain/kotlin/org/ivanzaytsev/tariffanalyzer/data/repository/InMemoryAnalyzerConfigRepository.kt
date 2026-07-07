package org.ivanzaytsev.tariffanalyzer.data.repository

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatusResult
import org.ivanzaytsev.tariffanalyzer.domain.repository.AnalyzerConfigRepository

class InMemoryAnalyzerConfigRepository : AnalyzerConfigRepository {

    private var currentStatus = ConfigStatusResult(
        status = ConfigStatus.Missing,
        configPath = null,
        issues = emptyList(),
    )

    override suspend fun getConfigStatus(): ConfigStatusResult = currentStatus

    override suspend fun saveConfigStatus(result: ConfigStatusResult) {
        currentStatus = result
    }
}
