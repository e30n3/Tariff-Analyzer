package org.ivanzaytsev.tariffanalyzer.domain.analyzer

import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.MessageTemplateRule

class TemplateMatcher private constructor(
    private val templatesBySender: Map<String, SenderTemplateIndex>,
) {

    fun findMatches(senderName: String, smsText: String): List<MessageTemplateRule> {
        val senderIndex = templatesBySender[AnalyzerNormalization.normalizeSender(senderName)] ?: return emptyList()
        val normalizedText = AnalyzerNormalization.normalizeMessageText(smsText)
        if (normalizedText.isEmpty()) return emptyList()

        val candidates = linkedSetOf<CompiledTemplate>()
        normalizedText.split(' ')
            .filter { it.isNotEmpty() }
            .forEach { token ->
                senderIndex.byAnchor[token]?.let(candidates::addAll)
            }
        candidates.addAll(senderIndex.withoutAnchor)

        return candidates
            .asSequence()
            .filter { it.regex.matches(normalizedText) }
            .sortedBy { it.orderIndex }
            .map { it.template }
            .toList()
    }

    companion object {
        fun compile(templates: List<MessageTemplateRule>): TemplateMatcher {
            val compiled = templates.mapIndexed { index, template ->
                TemplatePatternCompiler.compile(template, index)
            }
            val bySender = compiled
                .groupBy { AnalyzerNormalization.normalizeSender(it.template.senderName) }
                .mapValues { (_, senderTemplates) ->
                    SenderTemplateIndex(
                        byAnchor = senderTemplates
                            .filter { it.anchorToken != null }
                            .groupBy { it.anchorToken.orEmpty() },
                        withoutAnchor = senderTemplates.filter { it.anchorToken == null },
                    )
                }
            return TemplateMatcher(bySender)
        }
    }
}

object TemplatePatternCompiler {
    fun compile(template: MessageTemplateRule, orderIndex: Int = 0): CompiledTemplate {
        val parts = mutableListOf<String>()
        val literalTokens = mutableListOf<String>()

        template.text.trim()
            .split(Regex("""\s+"""))
            .filter { it.isNotEmpty() }
            .forEach { token ->
                when {
                    token == "%w+" -> error("Шаблон ${template.id}: операнд %w+ не поддерживается.")
                    token == "%w" -> parts.add("""\S+""")
                    token == "%d" -> parts.add("""\d+""")
                    token == "%d+" -> parts.add("""\d+(?:\s+\d+)*""")
                    token.matches(Regex("""%w\{1,\d+}""")) -> {
                        val limit = token.substringAfter(",").substringBefore("}").toInt()
                        require(limit in 1..20) { "Шаблон ${template.id}: некорректный операнд $token." }
                        parts.add(groupPattern("""\S+""", limit))
                    }
                    token.matches(Regex("""%d\{1,\d+}""")) -> {
                        val limit = token.substringAfter(",").substringBefore("}").toInt()
                        require(limit >= 1) { "Шаблон ${template.id}: некорректный операнд $token." }
                        parts.add(groupPattern("""\d+""", limit))
                    }
                    token.startsWith("%") -> error("Шаблон ${template.id}: неподдерживаемый операнд $token.")
                    else -> {
                        AnalyzerNormalization.normalizeMessageText(token)
                            .split(' ')
                            .filter { it.isNotEmpty() }
                            .forEach { literal ->
                                literalTokens.add(literal)
                                parts.add(Regex.escape(literal))
                            }
                    }
                }
            }

        require(parts.isNotEmpty()) { "Шаблон ${template.id}: пустой текст шаблона." }

        return CompiledTemplate(
            template = template,
            orderIndex = orderIndex,
            regex = Regex("^${parts.joinToString("""\s+""")}$"),
            anchorToken = literalTokens.firstOrNull(),
        )
    }

    private fun groupPattern(itemPattern: String, limit: Int): String =
        if (limit == 1) {
            itemPattern
        } else {
            """$itemPattern(?:\s+$itemPattern){0,${limit - 1}}"""
        }
}

data class CompiledTemplate(
    val template: MessageTemplateRule,
    val orderIndex: Int,
    val regex: Regex,
    val anchorToken: String?,
)

private data class SenderTemplateIndex(
    val byAnchor: Map<String, List<CompiledTemplate>>,
    val withoutAnchor: List<CompiledTemplate>,
)
