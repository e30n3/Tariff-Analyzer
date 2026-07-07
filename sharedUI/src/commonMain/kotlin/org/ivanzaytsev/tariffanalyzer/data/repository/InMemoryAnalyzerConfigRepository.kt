package org.ivanzaytsev.tariffanalyzer.data.repository

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatusResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.GeneratedConfigResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssue
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssueSeverity
import org.ivanzaytsev.tariffanalyzer.domain.repository.AnalyzerConfigRepository

class InMemoryAnalyzerConfigRepository : AnalyzerConfigRepository {

    private var currentStatus = ConfigStatusResult(
        status = ConfigStatus.Missing,
        configPath = null,
        issues = emptyList(),
    )

    override suspend fun getConfigStatus(): ConfigStatusResult = currentStatus

    override suspend fun generateConfig(
        templatesFile: AnalyzerFileReference,
        tariffFile: AnalyzerFileReference,
    ): GeneratedConfigResult {
        val issues = listOf(
            ValidationIssue(
                severity = ValidationIssueSeverity.Warning,
                location = "config",
                message = "In-memory конфигурация создана без записи JSON-файла.",
            ),
        )
        currentStatus = ConfigStatusResult(
            status = ConfigStatus.Valid,
            configPath = "/tmp/tariff-analyzer-config.json",
            issues = issues,
        )
        return GeneratedConfigResult(
            status = currentStatus.status,
            configPath = currentStatus.configPath.orEmpty(),
            issues = issues,
        )
    }

    override suspend fun validateConfig(): ConfigStatusResult = currentStatus

}
