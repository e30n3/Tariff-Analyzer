package org.ivanzaytsev.tariffanalyzer.domain.model

data class Tariff(
    val id: String,
    val name: String,
    val provider: String,
    val monthlyPrice: Double,
    val dataGb: Int,
    val callMinutes: Int,
)
