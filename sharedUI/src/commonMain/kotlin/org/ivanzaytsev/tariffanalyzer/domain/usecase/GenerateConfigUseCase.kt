package org.ivanzaytsev.tariffanalyzer.domain.usecase

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.GeneratedConfigResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssue
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssueSeverity

class GenerateConfigUseCase {
    suspend operator fun invoke(
        templatesFile: AnalyzerFileReference,
        tariffFile: AnalyzerFileReference,
    ): GeneratedConfigResult = GeneratedConfigResult(
        configPath = "${templatesFile.path.substringBeforeLast('/', missingDelimiterValue = ".")}/tariff-analyzer-config.json",
        issues = listOf(
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
        ),
    )
}
