package org.ivanzaytsev.tariffanalyzer.data.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual fun createAnalyzerConfigFileStorage(): AnalyzerConfigFileStorage {
    val appDir = System.getProperty("compose.application.dir")
        ?.takeIf { it.isNotBlank() }
        ?: System.getProperty("user.dir")
    return JvmAnalyzerConfigFileStorage(File(appDir, CONFIG_FILE_NAME))
}

private const val CONFIG_FILE_NAME = "tariff-analyzer-config.json"

private class JvmAnalyzerConfigFileStorage(
    private val configFile: File,
) : AnalyzerConfigFileStorage {

    override val configPath: String
        get() = configFile.absolutePath

    override suspend fun exists(): Boolean = withContext(Dispatchers.IO) {
        configFile.isFile
    }

    override suspend fun readText(): String = withContext(Dispatchers.IO) {
        configFile.readText(Charsets.UTF_8)
    }

    override suspend fun writeText(text: String) = withContext(Dispatchers.IO) {
        configFile.parentFile?.mkdirs()
        configFile.writeText(text, Charsets.UTF_8)
    }
}
