package com.example.util

data class ParsedResponse(
    val thinking: String?,
    val cleanContent: String,
    val artifacts: List<ExtractedArtifact>
)

data class ExtractedArtifact(
    val title: String,
    val language: String,
    val type: String, // "html", "svg", "code", "markdown"
    val content: String
)

object ClaudeParser {
    private val thinkingRegex = Regex("<thinking>([\\s\\S]*?)</thinking>", RegexOption.IGNORE_CASE)
    private val codeBlockRegex = Regex("```([a-zA-Z0-9_-]+)?\\s*\\n([\\s\\S]*?)```")

    fun parse(rawText: String): ParsedResponse {
        var thinking: String? = null
        var clean = rawText

        // Extract thinking content
        val thinkingMatch = thinkingRegex.find(rawText)
        if (thinkingMatch != null) {
            thinking = thinkingMatch.groupValues[1].trim()
            clean = rawText.replace(thinkingMatch.value, "").trim()
        }

        // Extract code artifacts
        val artifacts = mutableListOf<ExtractedArtifact>()
        val matches = codeBlockRegex.findAll(clean).toList()

        matches.forEachIndexed { index, match ->
            val lang = match.groupValues[1].trim().lowercase().ifEmpty { "code" }
            val code = match.groupValues[2].trim()

            // Only promote significant blocks to Artifacts (> 3 lines or web/code types)
            if (code.lines().size >= 3 || lang in listOf("html", "svg", "js", "javascript", "jsx", "tsx", "vue", "kotlin", "python")) {
                val type = when (lang) {
                    "html", "htm" -> "html"
                    "svg" -> "svg"
                    "md", "markdown" -> "markdown"
                    else -> "code"
                }

                val title = when (type) {
                    "html" -> "Interactive Web Application"
                    "svg" -> "Vector Graphic (SVG)"
                    "markdown" -> "Structured Document"
                    else -> "${lang.replaceFirstChar { it.uppercase() }} Snippet"
                } + if (matches.size > 1) " #${index + 1}" else ""

                artifacts.add(
                    ExtractedArtifact(
                        title = title,
                        language = lang,
                        type = type,
                        content = code
                    )
                )
            }
        }

        return ParsedResponse(
            thinking = thinking,
            cleanContent = clean,
            artifacts = artifacts
        )
    }

    fun prepareHtmlForPreview(content: String, type: String): String {
        return when (type) {
            "html" -> {
                if (content.contains("<html", ignoreCase = true)) {
                    content
                } else {
                    """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="utf-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body {
                                margin: 0;
                                padding: 16px;
                                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                                background-color: #FAF9F5;
                                color: #1F1E1B;
                            }
                        </style>
                    </head>
                    <body>
                        $content
                    </body>
                    </html>
                    """.trimIndent()
                }
            }
            "svg" -> {
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            margin: 0;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            min-height: 100vh;
                            background: #FAF9F5;
                        }
                        svg {
                            max-width: 90vw;
                            max-height: 80vh;
                        }
                    </style>
                </head>
                <body>
                    $content
                </body>
                </html>
                """.trimIndent()
            }
            else -> {
                val escaped = content
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            margin: 0;
                            padding: 16px;
                            background: #181716;
                            color: #EDEBE8;
                            font-family: monospace;
                            white-space: pre-wrap;
                            word-break: break-all;
                        }
                    </style>
                </head>
                <body>$escaped</body>
                </html>
                """.trimIndent()
            }
        }
    }
}
