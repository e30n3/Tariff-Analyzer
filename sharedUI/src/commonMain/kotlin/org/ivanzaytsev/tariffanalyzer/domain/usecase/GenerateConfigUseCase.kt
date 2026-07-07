package org.ivanzaytsev.tariffanalyzer.domain.usecase

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatusResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.GeneratedConfigResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssue
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssueSeverity
import org.ivanzaytsev.tariffanalyzer.domain.repository.AnalyzerConfigRepository

class GenerateConfigUseCase(
    private val analyzerConfigRepository: AnalyzerConfigRepository,
) {
    suspend operator fun invoke(
        templatesFile: AnalyzerFileReference,
        tariffFile: AnalyzerFileReference,
    ): GeneratedConfigResult {
        val configPath = "${templatesFile.path.substringBeforeLast('/', missingDelimiterValue = ".")}/tariff-analyzer-config.json"
        val issues = listOf(
            ValidationIssue(
                severity = ValidationIssueSeverity.Warning,
                location = "config.templates",
                message = "Конфигурация сгенерирована в режиме skeleton без разбора CSV.",
            ),
            ValidationIssue(
                severity = ValidationIssueSeverity.Warning,
                location = "config.tariffs",
                message = "Файл тарифов '${tariffFile.name}' пока не преобразуется в реальные тарифные правила.",
            ),
        )
        analyzerConfigRepository.saveConfigStatus(
            ConfigStatusResult(
                status = ConfigStatus.Valid,
                configPath = configPath,
                issues = issues,
            ),
        )
        return GeneratedConfigResult(
            configPath = configPath,
            issues = issues,
        )
    }
}
