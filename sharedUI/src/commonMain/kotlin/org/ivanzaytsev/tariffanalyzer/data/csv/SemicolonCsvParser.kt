package org.ivanzaytsev.tariffanalyzer.data.csv

class SemicolonCsvParser {

    fun parse(text: String): List<List<String>> {
        if (text.isEmpty()) return emptyList()

        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val cell = StringBuilder()
        var index = 0
        var inQuotes = false

        while (index < text.length) {
            val char = text[index]
            when {
                char == '"' && inQuotes && text.getOrNull(index + 1) == '"' -> {
                    cell.append('"')
                    index++
                }

                char == '"' -> {
                    inQuotes = !inQuotes
                }

                char == ';' && !inQuotes -> {
                    row.add(cell.toString())
                    cell.clear()
                }

                char == '\n' && !inQuotes -> {
                    row.add(cell.toString())
                    cell.clear()
                    rows.add(row.toList())
                    row.clear()
                }

                char == '\r' && !inQuotes -> {
                    if (text.getOrNull(index + 1) == '\n') {
                        index++
                    }
                    row.add(cell.toString())
                    cell.clear()
                    rows.add(row.toList())
                    row.clear()
                }

                else -> cell.append(char)
            }
            index++
        }

        if (cell.isNotEmpty() || row.isNotEmpty()) {
            row.add(cell.toString())
            rows.add(row.toList())
        }

        return rows
    }
}
