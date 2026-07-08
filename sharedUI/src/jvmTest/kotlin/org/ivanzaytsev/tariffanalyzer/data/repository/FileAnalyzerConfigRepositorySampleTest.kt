package org.ivanzaytsev.tariffanalyzer.data.repository

import kotlinx.coroutines.test.runTest
import org.ivanzaytsev.tariffanalyzer.data.config.AnalyzerConfigFileStorage
import org.ivanzaytsev.tariffanalyzer.data.csv.createCsvFileReader
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFilePurpose
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileAnalyzerConfigRepositorySampleTest {

    @Test
    fun generateConfigFromSampleFilesWritesTemplatesAndTariffs() = runTest {
        val storage = FakeConfigFileStorage()
        val repository = FileAnalyzerConfigRepository(storage, createCsvFileReader())

        val result = repository.generateConfig(
            templatesFile = sampleFile("message_templates.csv", AnalyzerFilePurpose.MessageTemplates),
            tariffFile = sampleFile("tariff.csv", AnalyzerFilePurpose.Tariff),
        )

        assertEquals(ConfigStatus.Valid, result.status, result.issues.joinToString { it.message })
        assertTrue(storage.text.orEmpty().contains("\"templates\": ["))
        assertTrue(storage.text.orEmpty().contains("\"tariffs\": ["))
        assertTrue(storage.text.orEmpty().contains("\"id\": \"39324\""))
        assertTrue(storage.text.orEmpty().contains("\"trafficType\": \"Сервисный\""))
        assertTrue(storage.text.orEmpty().contains("\"priceWithVat\": \"2.710000000000\""))
    }

    private fun sampleFile(
        name: String,
        purpose: AnalyzerFilePurpose,
    ): AnalyzerFileReference {
        val file = sequenceOf(
            File("sample_files/$name"),
            File("../sample_files/$name"),
        ).first { it.isFile }
        return AnalyzerFileReference(
            name = name,
            path = file.absolutePath,
            sizeBytes = file.length(),
            purpose = purpose,
        )
    }

    private class FakeConfigFileStorage : AnalyzerConfigFileStorage {
        var text: String? = null

        override val configPath: String = "/tmp/tariff-analyzer-config.json"

        override suspend fun exists(): Boolean = text != null

        override suspend fun readText(): String = text.orEmpty()

        override suspend fun writeText(text: String) {
            this.text = text
        }
    }
}
