package org.ivanzaytsev.tariffanalyzer.data.repository

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import org.ivanzaytsev.tariffanalyzer.data.config.AnalyzerConfigFileStorage
import org.ivanzaytsev.tariffanalyzer.data.csv.CsvFileReader
import org.ivanzaytsev.tariffanalyzer.data.csv.SemicolonCsvParser
import org.ivanzaytsev.tariffanalyzer.domain.analyzer.AnalyzerNormalization
import org.ivanzaytsev.tariffanalyzer.domain.analyzer.TemplatePatternCompiler
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerConfig
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerFileReference
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.AnalyzerSource
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatusResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.DecimalAmount
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.GeneratedConfigResult
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.MessageTemplateRule
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TariffRange
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TariffRule
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.TrafficMapping
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

    override suspend fun loadConfig(): AnalyzerConfig {
        if (!storage.exists()) {
            error("Конфигурация не найдена: ${storage.configPath}")
        }
        val root = json.parseToJsonElement(storage.readText()).jsonObject
        val issues = validateRoot(root)
        if (issues.any { it.severity == ValidationIssueSeverity.Error }) {
            error("Конфигурация некорректна: ${issues.joinToString { "${it.location}: ${it.message}" }}")
        }
        return parseConfig(root)
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
                        put("operator", AnalyzerNormalization.normalizeOperator(parts[1]))
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

        val lastRangeEndByKey = mutableMapOf<TariffBuildKey, Long>()

        return buildJsonArray {
            rows.drop(1).forEachIndexed { rowIndex, row ->
                val csvLine = rowIndex + 2
                val description = row.valueAt(columnIndexes.getValue("Описание")).trim()
                val parsed = parseTrafficTariffDescription(description) ?: return@forEachIndexed
                val quantity = row.valueAt(columnIndexes.getValue("Количество")).parseLong()
                val price = row.valueAt(columnIndexes.getValue("Цена (руб. с НДС)")).trim()
                val totalCost = row.valueAt(columnIndexes.getValue("Стоимость (руб. с НДС)")).trim()
                val tariffKey = TariffBuildKey(
                    operator = AnalyzerNormalization.normalizeOperator(parsed.operator),
                    trafficType = parsed.trafficType.trim().lowercase(),
                )
                val rangeStart = parsed.rangeStart ?: (lastRangeEndByKey[tariffKey]?.plus(1L) ?: 1L)
                val rangeEnd = quantity?.let { rangeStart + it - 1L }
                if (rangeEnd != null) {
                    lastRangeEndByKey[tariffKey] = rangeEnd
                }

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
                        put("operator", AnalyzerNormalization.normalizeOperator(parsed.operator))
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
        } else {
            tariffs.forEachIndexed { index, element ->
                val tariff = element as? JsonObject ?: return@forEachIndexed
                val price = tariff["priceWithVat"]?.jsonPrimitive?.contentOrNull
                if (!price.isNullOrBlank() && DecimalAmount.parse(price) == null) {
                    add(
                        ValidationIssue(
                            severity = ValidationIssueSeverity.Error,
                            location = "\$.tariffs[$index].priceWithVat",
                            message = "Цена должна быть корректным десятичным числом.",
                        ),
                    )
                }
            }
        }

        if (none { it.severity == ValidationIssueSeverity.Error }) {
            runCatching {
                val config = parseConfig(root)
                config.templates.forEach { template ->
                    TemplatePatternCompiler.compile(template)
                }
            }.onFailure { throwable ->
                add(
                    ValidationIssue(
                        severity = ValidationIssueSeverity.Error,
                        location = "\$",
                        message = throwable.message ?: "Некорректная конфигурация.",
                    ),
                )
            }
        }
    }

    private fun parseConfig(root: JsonObject): AnalyzerConfig = AnalyzerConfig(
        templates = root.getValue("templates").jsonArray.mapIndexed { index, element ->
            val template = element.jsonObject
            MessageTemplateRule(
                id = template.requiredString("id", "\$.templates[$index].id"),
                text = template.requiredString("text", "\$.templates[$index].text"),
                senderName = template.requiredString("senderName", "\$.templates[$index].senderName"),
                trafficMappings = template.getValue("trafficMappings").jsonArray.mapIndexed { mappingIndex, mappingElement ->
                    val mapping = mappingElement.jsonObject
                    TrafficMapping(
                        channel = mapping.requiredString(
                            name = "channel",
                            location = "\$.templates[$index].trafficMappings[$mappingIndex].channel",
                        ),
                        operator = AnalyzerNormalization.normalizeOperator(
                            mapping.requiredString(
                                name = "operator",
                                location = "\$.templates[$index].trafficMappings[$mappingIndex].operator",
                            ),
                        ),
                        trafficType = mapping.requiredString(
                            name = "trafficType",
                            location = "\$.templates[$index].trafficMappings[$mappingIndex].trafficType",
                        ),
                        sourceValue = mapping["sourceValue"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                    )
                },
                source = template["source"]?.jsonObject?.toAnalyzerSource(),
            )
        },
        tariffs = root.getValue("tariffs").jsonArray.mapIndexed { index, element ->
            val tariff = element.jsonObject
            val range = tariff.getValue("range").jsonObject
            TariffRule(
                operator = AnalyzerNormalization.normalizeOperator(
                    tariff.requiredString("operator", "\$.tariffs[$index].operator"),
                ),
                trafficType = tariff.requiredString("trafficType", "\$.tariffs[$index].trafficType"),
                priceWithVat = tariff.requiredString("priceWithVat", "\$.tariffs[$index].priceWithVat"),
                quantity = tariff["quantity"]?.jsonPrimitive?.longOrNull ?: 0L,
                range = TariffRange(
                    from = range["from"]?.jsonPrimitive?.longOrNull
                        ?: error("Отсутствует обязательное поле \$.tariffs[$index].range.from."),
                    to = range["to"]?.jsonPrimitive?.longOrNull,
                ),
                source = tariff["source"]?.jsonObject?.toAnalyzerSource(),
            )
        },
    )

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

    private fun JsonObject.requiredString(name: String, location: String): String =
        this[name]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: error("Отсутствует обязательное поле $location.")

    private fun JsonObject.toAnalyzerSource(): AnalyzerSource = AnalyzerSource(
        csvLine = this["csvLine"]?.jsonPrimitive?.contentOrNull?.toIntOrNull(),
        description = this["description"]?.jsonPrimitive?.contentOrNull,
        rawValue = this["trafficTypesRaw"]?.jsonPrimitive?.contentOrNull,
    )

    private data class ParsedTrafficTariff(
        val operator: String,
        val trafficType: String,
        val rangeStart: Long?,
    )

    private data class TariffBuildKey(
        val operator: String,
        val trafficType: String,
    )
}
