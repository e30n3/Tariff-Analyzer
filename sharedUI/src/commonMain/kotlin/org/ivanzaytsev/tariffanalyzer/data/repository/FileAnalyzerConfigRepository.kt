package org.ivanzaytsev.tariffanalyzer.data.repository

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import org.ivanzaytsev.tariffanalyzer.data.config.AnalyzerConfigFileStorage
import org.ivanzaytsev.tariffanalyzer.data.csv.CsvFileReader
import org.ivanzaytsev.tariffanalyzer.data.csv.SemicolonCsvParser
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatusResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.GeneratedConfigResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssue
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ValidationIssueSeverity
import org.ivanzaytsev.tariffanalyzer.domain.repository.AnalyzerConfigRepository

class FileAnalyzerConfigRepository(
    private val storage: AnalyzerConfigFileStorage,
    private val csvFileReader: CsvFileReader,
    private val csvParser: SemicolonCsvParser = SemicolonCsvParser(),
    private val json: Json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    },
) : AnalyzerConfigRepository {

    override suspend fun getConfigStatus(): ConfigStatusResult {
        if (!storage.exists()) {
            return ConfigStatusResult(
                status = ConfigStatus.Missing,
                configPath = null,
                issues = emptyList(),
            )
        }
        return validateConfig()
    }

    override suspend fun generateConfig(
        templatesFile: AnalyzerFileReference,
        tariffFile: AnalyzerFileReference,
    ): GeneratedConfigResult {
        val generationIssues = mutableListOf<ValidationIssue>()
        val templates = parseTemplates(
            rows = csvParser.parse(csvFileReader.readWindows1251Text(templatesFile.path)),
            issues = generationIssues,
        )
        val tariffs = parseTariffs(
            rows = csvParser.parse(csvFileReader.readWindows1251Text(tariffFile.path)),
            issues = generationIssues,
        )
        val config = buildJsonObject {
            put(
                "meta",
                buildJsonObject {
                    put("formatVersion", 1)
                    put("generatedBy", "Tariff Analyzer")
                    put(
                        "sourceFiles",
                        buildJsonObject {
                            put("templatesCsv", templatesFile.path)
                            put("tariffCsv", tariffFile.path)
                        },
                    )
                },
            )
            put("templates", templates)
            put("tariffs", tariffs)
        }
        storage.writeText(json.encodeToString(JsonObject.serializer(), config))

        val issues = generationIssues + validateRoot(config)
        return GeneratedConfigResult(
            status = statusFor(issues),
            configPath = storage.configPath,
            issues = issues,
        )
    }

    override suspend fun validateConfig(): ConfigStatusResult {
        if (!storage.exists()) {
            return ConfigStatusResult(
                status = ConfigStatus.Missing,
                configPath = null,
                issues = emptyList(),
            )
        }

        val issues = runCatching {
            val root = json.parseToJsonElement(storage.readText()).jsonObject
            validateRoot(root)
        }.getOrElse { throwable ->
            listOf(
                ValidationIssue(
                    severity = ValidationIssueSeverity.Error,
                    location = storage.configPath,
                    message = throwable.message ?: "Некорректный JSON конфигурации.",
                ),
            )
        }

        return ConfigStatusResult(
            status = statusFor(issues),
            configPath = storage.configPath,
            issues = issues,
        )
    }

    private fun parseTemplates(
        rows: List<List<String>>,
        issues: MutableList<ValidationIssue>,
    ): JsonArray {
        val columnIndexes = rows.firstOrNull()?.columnIndexes(
            "ID",
            "Текст шаблона",
            "Имя отправителя",
            "Типы трафика",
        )
        if (columnIndexes == null) {
            issues.add(
                ValidationIssue(
                    severity = ValidationIssueSeverity.Error,
                    location = "message_templates.csv:1",
                    message = "Не найдены обязательные колонки ID, Текст шаблона, Имя отправителя, Типы трафика.",
                ),
            )
            return buildJsonArray { }
        }

        return buildJsonArray {
            rows.drop(1).forEachIndexed { rowIndex, row ->
                val csvLine = rowIndex + 2
                val id = row.valueAt(columnIndexes.getValue("ID")).trim()
                val text = row.valueAt(columnIndexes.getValue("Текст шаблона")).trim()
                val senderName = row.valueAt(columnIndexes.getValue("Имя отправителя")).trim()
                val trafficTypes = row.valueAt(columnIndexes.getValue("Типы трафика"))
                val mappings = parseTrafficMappings(trafficTypes, csvLine, issues)

                if (id.isEmpty() || text.isEmpty() || senderName.isEmpty() || mappings.isEmpty()) {
                    issues.add(
                        ValidationIssue(
                            severity = ValidationIssueSeverity.Warning,
                            location = "message_templates.csv:$csvLine",
                            message = "Строка шаблона пропущена: отсутствуют обязательные значения.",
                        ),
                    )
                    return@forEachIndexed
                }

                add(
                    buildJsonObject {
                        put("id", id)
                        put("text", text)
                        put("senderName", senderName)
                        put("trafficMappings", mappings)
                        put(
                            "source",
                            buildJsonObject {
                                put("csvLine", csvLine)
                                put("trafficTypesRaw", trafficTypes)
                            },
                        )
                    },
                )
            }
        }
    }

    private fun parseTrafficMappings(
        value: String,
        csvLine: Int,
        issues: MutableList<ValidationIssue>,
    ): JsonArray = buildJsonArray {
        value.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { mapping ->
                val parts = mapping.split(":").map { it.trim() }
                if (parts.size != 3 || parts.any { it.isEmpty() }) {
                    issues.add(
                        ValidationIssue(
                            severity = ValidationIssueSeverity.Error,
                            location = "message_templates.csv:$csvLine",
                            message = "Некорректное соответствие типа трафика: '$mapping'.",
                        ),
                    )
                    return@forEach
                }
                add(
                    buildJsonObject {
                        put("channel", parts[0])
                        put("operator", normalizeOperator(parts[1]))
                        put("trafficType", parts[2])
                        put("sourceValue", mapping)
                    },
                )
            }
    }

    private fun parseTariffs(
        rows: List<List<String>>,
        issues: MutableList<ValidationIssue>,
    ): JsonArray {
        val columnIndexes = rows.firstOrNull()?.columnIndexes(
            "Описание",
            "Количество",
            "Цена (руб. с НДС)",
            "Стоимость (руб. с НДС)",
        )
        if (columnIndexes == null) {
            issues.add(
                ValidationIssue(
                    severity = ValidationIssueSeverity.Error,
                    location = "tariff.csv:1",
                    message = "Не найдены обязательные колонки Описание, Количество, Цена (руб. с НДС), Стоимость (руб. с НДС).",
                ),
            )
            return buildJsonArray { }
        }

        return buildJsonArray {
            rows.drop(1).forEachIndexed { rowIndex, row ->
                val csvLine = rowIndex + 2
                val description = row.valueAt(columnIndexes.getValue("Описание")).trim()
                val parsed = parseTrafficTariffDescription(description) ?: return@forEachIndexed
                val quantity = row.valueAt(columnIndexes.getValue("Количество")).parseLong()
                val price = row.valueAt(columnIndexes.getValue("Цена (руб. с НДС)")).trim()
                val totalCost = row.valueAt(columnIndexes.getValue("Стоимость (руб. с НДС)")).trim()
                val rangeStart = parsed.rangeStart ?: 1L
                val rangeEnd = quantity?.let { rangeStart + it - 1L }

                if (price.isEmpty()) {
                    issues.add(
                        ValidationIssue(
                            severity = ValidationIssueSeverity.Error,
                            location = "tariff.csv:$csvLine",
                            message = "У тарифного правила отсутствует цена.",
                        ),
                    )
                }

                add(
                    buildJsonObject {
                        put("operator", normalizeOperator(parsed.operator))
                        put("trafficType", parsed.trafficType)
                        put("priceWithVat", price)
                        put("quantity", quantity ?: 0L)
                        put(
                            "range",
                            buildJsonObject {
                                put("from", rangeStart)
                                if (rangeEnd != null) {
                                    put("to", rangeEnd)
                                }
                            },
                        )
                        put(
                            "source",
                            buildJsonObject {
                                put("csvLine", csvLine)
                                put("description", description)
                                put("totalCostWithVat", totalCost)
                            },
                        )
                    },
                )
            }
        }
    }

    private fun parseTrafficTariffDescription(description: String): ParsedTrafficTariff? {
        val marker = "Пропуск sms-трафика\""
        val markerIndex = description.indexOf(marker)
        if (markerIndex < 0) return null

        val tail = description.substring(markerIndex + marker.length).trim()
        if (tail.isEmpty()) return null

        val rangeStart = Regex("""\(по шкале от ([\d\s]+)\)""")
            .find(tail)
            ?.groupValues
            ?.getOrNull(1)
            ?.filter { it.isDigit() }
            ?.toLongOrNull()
        val withoutRange = tail.replace(Regex("""\s*\(по шкале от [\d\s]+\)\s*$"""), "").trim()
        val firstSpace = withoutRange.indexOf(' ')
        if (firstSpace < 0) return null

        return ParsedTrafficTariff(
            operator = withoutRange.substring(0, firstSpace),
            trafficType = withoutRange.substring(firstSpace + 1).trim(),
            rangeStart = rangeStart,
        )
    }

    private fun validateRoot(root: JsonObject): List<ValidationIssue> = buildList {
        val templates = root["templates"]
        val tariffs = root["tariffs"]

        if (templates == null) {
            add(
                ValidationIssue(
                    severity = ValidationIssueSeverity.Error,
                    location = "\$.templates",
                    message = "Отсутствует обязательная секция templates.",
                ),
            )
        } else if (templates !is JsonArray) {
            add(
                ValidationIssue(
                    severity = ValidationIssueSeverity.Error,
                    location = "\$.templates",
                    message = "Секция templates должна быть массивом.",
                ),
            )
        }

        if (tariffs == null) {
            add(
                ValidationIssue(
                    severity = ValidationIssueSeverity.Error,
                    location = "\$.tariffs",
                    message = "Отсутствует обязательная секция tariffs.",
                ),
            )
        } else if (tariffs !is JsonArray) {
            add(
                ValidationIssue(
                    severity = ValidationIssueSeverity.Error,
                    location = "\$.tariffs",
                    message = "Секция tariffs должна быть массивом.",
                ),
            )
        }
    }

    private fun statusFor(issues: List<ValidationIssue>): ConfigStatus =
        if (issues.any { it.severity == ValidationIssueSeverity.Error }) {
            ConfigStatus.Invalid
        } else {
            ConfigStatus.Valid
        }

    private fun List<String>.columnIndexes(vararg requiredColumns: String): Map<String, Int>? {
        val indexes = requiredColumns.associateWith { requiredColumn ->
            indexOfFirst { it.trim() == requiredColumn }
        }
        return indexes.takeIf { columns -> columns.values.all { it >= 0 } }
    }

    private fun List<String>.valueAt(index: Int): String = getOrNull(index).orEmpty()

    private fun String.parseLong(): Long? = trim()
        .filter { it.isDigit() }
        .takeIf { it.isNotEmpty() }
        ?.toLongOrNull()

    private fun normalizeOperator(value: String): String = when (value.trim().lowercase()) {
        "t2" -> "tele2"
        "мтс" -> "mts"
        "мегафон" -> "megafon"
        "билайн" -> "beeline"
        "мотив" -> "motiw"
        "ростелеком" -> "rostelecom"
        else -> value.trim().lowercase()
    }

    private data class ParsedTrafficTariff(
        val operator: String,
        val trafficType: String,
        val rangeStart: Long?,
    )
}
