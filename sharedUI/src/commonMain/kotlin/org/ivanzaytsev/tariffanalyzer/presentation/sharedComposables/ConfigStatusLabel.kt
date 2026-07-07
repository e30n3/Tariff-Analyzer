package org.ivanzaytsev.tariffanalyzer.presentation.sharedComposables

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus

fun ConfigStatus.label(): String = when (this) {
    ConfigStatus.Missing -> "Конфигурация отсутствует"
    ConfigStatus.Valid -> "Конфигурация валидна"
    ConfigStatus.Invalid -> "Конфигурация содержит ошибки"
}
