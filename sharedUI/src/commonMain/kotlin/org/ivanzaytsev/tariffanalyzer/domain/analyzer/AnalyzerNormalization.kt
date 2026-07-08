package org.ivanzaytsev.tariffanalyzer.domain.analyzer

object AnalyzerNormalization {

    fun normalizeOperator(value: String): String = when (value.trim().lowercase()) {
        "t2" -> "tele2"
        "мтс" -> "mts"
        "мегафон" -> "megafon"
        "билайн" -> "beeline"
        "мотив" -> "motiw"
        "ростелеком" -> "rostelecom"
        else -> value.trim().lowercase()
    }

    fun normalizeTrafficType(value: String): String = value.trim().lowercase()

    fun normalizeSender(value: String): String = value.trim().lowercase()

    fun normalizeMessageText(value: String): String = value
        .lowercase()
        .map { char ->
            when {
                char.isLetterOrDigit() -> char
                char.isWhitespace() -> ' '
                else -> ' '
            }
        }
        .joinToString(separator = "")
        .replace(Regex("""\s+"""), " ")
        .trim()
}
