package org.ivanzaytsev.tariffanalyzer.domain.usecase

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssue
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssueSeverity

class ValidateConfigUseCase {
    suspend operator fun invoke(): List<ValidationIssue> = listOf(
        ValidationIssue(
            severity = ValidationIssueSeverity.Warning,
            location = "config",
            message = "Skeleton-валидация: синтаксис JSON и бизнес-правила будут подключены в следующем инкременте.",
        ),
    )
}
