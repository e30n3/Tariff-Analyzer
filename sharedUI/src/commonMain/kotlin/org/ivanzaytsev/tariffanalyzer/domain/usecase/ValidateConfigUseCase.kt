package org.ivanzaytsev.tariffanalyzer.domain.usecase

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatusResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssue
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssueSeverity
import org.ivanzaytsev.tariffanalyzer.domain.repository.AnalyzerConfigRepository

class ValidateConfigUseCase(
    private val analyzerConfigRepository: AnalyzerConfigRepository,
) {
    suspend operator fun invoke(): List<ValidationIssue> {
        val currentStatus = analyzerConfigRepository.getConfigStatus()
        val issues = listOf(
            ValidationIssue(
                severity = ValidationIssueSeverity.Warning,
                location = "config",
                message = "Skeleton-валидация: синтаксис JSON и бизнес-правила будут подключены в следующем инкременте.",
            ),
        )
        analyzerConfigRepository.saveConfigStatus(
            ConfigStatusResult(
                status = ConfigStatus.Valid,
                configPath = currentStatus.configPath,
                issues = issues,
            ),
        )
        return issues
    }
}
