package org.ivanzaytsev.tariffanalyzer.data.config

interface AnalyzerConfigFileStorage {
    val configPath: String
    suspend fun exists(): Boolean
    suspend fun readText(): String
    suspend fun writeText(text: String)
}

expect fun createAnalyzerConfigFileStorage(): AnalyzerConfigFileStorage
