package org.ivanzaytsev.tariffanalyzer.domain.model.analyzer

expect class DecimalAmount : Comparable<DecimalAmount> {
    operator fun plus(other: DecimalAmount): DecimalAmount
    operator fun minus(other: DecimalAmount): DecimalAmount
    fun absoluteValue(): DecimalAmount
    fun signum(): Int
    fun toPlainString(): String
    fun toRoundedPlainString(scale: Int): String

    companion object {
        val Zero: DecimalAmount
        fun parse(value: String): DecimalAmount?
    }
}
