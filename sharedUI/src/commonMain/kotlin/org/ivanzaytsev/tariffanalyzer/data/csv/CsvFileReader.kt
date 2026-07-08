package org.ivanzaytsev.tariffanalyzer.data.csv

interface CsvFileReader {
    suspend fun readWindows1251Text(path: String): String
}

expect fun createCsvFileReader(): CsvFileReader
