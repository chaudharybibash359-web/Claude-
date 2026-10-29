package com.example.data.model

data class ClaudeModel(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val tag: String,
    val supportsThinking: Boolean,
    val speedRating: String,
    val intelligenceRating: String,
    val contextWindow: String,
    val systemPromptAddition: String
)

object ClaudeModels {
    val OPUS_5_5 = ClaudeModel(
        id = "claude-opus-5-5",
        name = "Claude Opus 5.5",
        subtitle = "Pinnacle Cognitive Intellect & Super-Coding",
        description = "Next-generation flagship cognitive intelligence. Frontier-class multi-step reasoning, quantum-level code architecture, and complex problem decomposition.",
        tag = "Omni-Pinnacle",
        supportsThinking = true,
        speedRating = "Ultra-Fast",
        intelligenceRating = "Pinnacle 5.5",
        contextWindow = "500K tokens",
        systemPromptAddition = """
            You are Claude Opus 5.5, Anthropic's most capable and authoritative intelligence.
            Respond immediately with absolute precision, supreme clarity, and state-of-the-art coding architecture.
            When solving complex engineering, mathematics, or design problems, include deep architectural reasoning.
            Always write complete, production-grade, bug-free implementations without placeholders.
        """.trimIndent()
    )

    val FABLE_5_1 = ClaudeModel(
        id = "fable-5-1-creative",
        name = "Fable 5.1 AI",
        subtitle = "Narrative Architecture & Creative World-Building",
        description = "Specialized frontier creative model engineered for rich world-building, interactive gameplay lore, character dialogue, and evocative literary writing.",
        tag = "Creative Genius",
        supportsThinking = false,
        speedRating = "Instantaneous",
        intelligenceRating = "Ultra-Creative",
        contextWindow = "300K tokens",
        systemPromptAddition = """
            You are Fable 5.1, the master of storytelling, narrative design, game lore, and world architecture.
            Infuse every answer with vibrant creative depth, memorable imagery, compelling dialogue, and stylistic elegance.
        """.trimIndent()
    )

    val SONNET_3_7_THINKING = ClaudeModel(
        id = "claude-3-7-sonnet-thinking",
        name = "Claude 3.7 Sonnet (Thinking)",
        subtitle = "Hybrid reasoning & extended thinking",
        description = "Highest intelligence with visible chain-of-thought reasoning traces. Excels at complex codebases, architecture, algorithms, and deep analysis.",
        tag = "Thinking",
        supportsThinking = true,
        speedRating = "Fast",
        intelligenceRating = "Maximum",
        contextWindow = "200K tokens",
        systemPromptAddition = """
            You are Claude 3.7 Sonnet with Extended Thinking, Anthropic's most intelligent model.
            When reasoning through complex problems, architecture, or code, wrap your internal chain-of-thought steps inside <thinking>...</thinking> tags.
            In your thinking block, carefully analyze edge cases, plan algorithms, and evaluate design patterns.
            After the thinking block, provide the polished, complete, and immaculate answer.
            Always write clean, production-grade, bug-free code. When outputting significant code files, HTML components, or scripts, format them clearly with language tags.
        """.trimIndent()
    )

    val SONNET_3_5_CODING = ClaudeModel(
        id = "claude-3-5-sonnet-coding",
        name = "Claude 3.5 Sonnet",
        subtitle = "The Coding King & Workhorse",
        description = "The industry standard for software engineering, frontend/backend architecture, artifact creation, and precise technical writing.",
        tag = "Coding King",
        supportsThinking = false,
        speedRating = "Very Fast",
        intelligenceRating = "Very High",
        contextWindow = "200K tokens",
        systemPromptAddition = """
            You are Claude 3.5 Sonnet, renowned as the ultimate coding assistant and technical collaborator.
            Deliver direct, elegant, well-architected solutions.
            Write modern code following best practices, DRY principles, and robust error handling.
            When providing code, avoid placeholders or incomplete snippets—provide the full working code.
        """.trimIndent()
    )

    val HAIKU_3_5_FAST = ClaudeModel(
        id = "claude-3-5-haiku",
        name = "Claude 3.5 Haiku",
        subtitle = "Ultra-low latency & rapid answers",
        description = "Lightning-fast generation for quick questions, text transformations, inline bug fixes, and instant brainstorming.",
        tag = "Lightning",
        supportsThinking = false,
        speedRating = "Instant",
        intelligenceRating = "High",
        contextWindow = "200K tokens",
        systemPromptAddition = """
            You are Claude 3.5 Haiku, Anthropic's fastest model.
            Provide swift, succinct, and highly accurate answers with zero fluff.
        """.trimIndent()
    )

    val OPUS_3_RESEARCH = ClaudeModel(
        id = "claude-3-opus",
        name = "Claude 3 Opus",
        subtitle = "Deep scholarly intellect & nuanced writing",
        description = "Top-tier philosophical reasoning, long-form literature analysis, complex mathematics, and multi-faceted problem solving.",
        tag = "Deep Intellect",
        supportsThinking = false,
        speedRating = "Moderate",
        intelligenceRating = "Pinnacle",
        contextWindow = "200K tokens",
        systemPromptAddition = """
            You are Claude 3 Opus, the deeply thoughtful scholar and philosophical intellect.
            Explore nuance, present balanced perspectives, and articulate complex concepts with exceptional literary clarity and intellectual depth.
        """.trimIndent()
    )

    val ALL_MODELS = listOf(
        OPUS_5_5,
        FABLE_5_1,
        SONNET_3_7_THINKING,
        SONNET_3_5_CODING,
        HAIKU_3_5_FAST,
        OPUS_3_RESEARCH
    )

    fun getById(id: String): ClaudeModel {
        return ALL_MODELS.find { it.id == id } ?: OPUS_5_5
    }
}
