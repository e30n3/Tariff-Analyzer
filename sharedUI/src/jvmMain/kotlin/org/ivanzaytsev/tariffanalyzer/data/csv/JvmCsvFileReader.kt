package org.ivanzaytsev.tariffanalyzer.data.csv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset

actual fun createCsvFileReader(): CsvFileReader = JvmCsvFileReader()

private class JvmCsvFileReader : CsvFileReader {
    override suspend fun readWindows1251Text(path: String): String = withContext(Dispatchers.IO) {
        File(path).readText(Charset.forName("windows-1251"))
    }
}
