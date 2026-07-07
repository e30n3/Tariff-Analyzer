package org.ivanzaytsev.tariffanalyzer.domain.usecase

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatusResult

class LoadConfigStatusUseCase {
    suspend operator fun invoke(): ConfigStatusResult = ConfigStatusResult(
        status = ConfigStatus.Missing,
        configPath = null,
        issues = emptyList(),
    )
}
