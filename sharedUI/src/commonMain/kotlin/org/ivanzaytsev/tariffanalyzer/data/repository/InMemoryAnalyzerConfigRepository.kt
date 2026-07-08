package org.ivanzaytsev.tariffanalyzer.data.repository

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerConfig
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatusResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.GeneratedConfigResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.MessageTemplateRule
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TariffRange
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TariffRule
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TrafficMapping
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssue
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssueSeverity
import org.ivanzaytsev.tariffanalyzer.domain.repository.AnalyzerConfigRepository

class InMemoryAnalyzerConfigRepository : AnalyzerConfigRepository {

    private var currentStatus = ConfigStatusResult(
        status = ConfigStatus.Missing,
        configPath = null,
        issues = emptyList(),
    )
    private var currentConfig = AnalyzerConfig(
        templates = emptyList(),
        tariffs = emptyList(),
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
        currentConfig = AnalyzerConfig(
            templates = listOf(
                MessageTemplateRule(
                    id = "test-template",
                    text = "код %d",
                    senderName = "OTP_Bank",
                    trafficMappings = listOf(
                        TrafficMapping(
                            channel = "СМС",
                            operator = "mts",
                            trafficType = "Сервисный",
                            sourceValue = "СМС:mts:Сервисный",
                        ),
                    ),
                ),
            ),
            tariffs = listOf(
                TariffRule(
                    operator = "mts",
                    trafficType = "Сервисный",
                    priceWithVat = "3.000000000000",
                    quantity = 5000,
                    range = TariffRange(from = 1, to = 5000),
                ),
            ),
        )
        return GeneratedConfigResult(
            status = currentStatus.status,
            configPath = currentStatus.configPath.orEmpty(),
            issues = issues,
        )
    }

    override suspend fun validateConfig(): ConfigStatusResult = currentStatus

    override suspend fun loadConfig(): AnalyzerConfig = currentConfig

}
