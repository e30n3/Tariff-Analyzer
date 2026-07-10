package org.ivanzaytsev.tariffanalyzer.domain.model.analyzer

import java.math.BigDecimal
import java.math.RoundingMode

actual class DecimalAmount private constructor(
    private val value: BigDecimal,
) : Comparable<DecimalAmount> {

    actual operator fun plus(other: DecimalAmount): DecimalAmount = DecimalAmount(value + other.value)

    actual operator fun minus(other: DecimalAmount): DecimalAmount = DecimalAmount(value - other.value)

    actual fun absoluteValue(): DecimalAmount = DecimalAmount(value.abs())

    actual fun signum(): Int = value.signum()

    actual fun toPlainString(): String = value.toPlainString()

    actual fun toRoundedPlainString(scale: Int): String =
        value.setScale(scale, RoundingMode.HALF_UP).toPlainString()

    override fun compareTo(other: DecimalAmount): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean = other is DecimalAmount && compareTo(other) == 0

    override fun hashCode(): Int = value.stripTrailingZeros().hashCode()

    override fun toString(): String = toPlainString()

    actual companion object {
        actual val Zero: DecimalAmount = DecimalAmount(BigDecimal.ZERO)

        actual fun parse(value: String): DecimalAmount? {
            val normalized = value.trim().replace(',', '.')
            if (normalized.isEmpty()) return null
            return normalized.toBigDecimalOrNull()?.let(::DecimalAmount)
        }
    }
}
