package com.example.data.model

data class PromptTemplate(
    val title: String,
    val prompt: String,
    val icon: String,
    val modelRecommendation: String
)

object PromptTemplates {
    val STARTERS = listOf(
        PromptTemplate(
            title = "Build Compose UI",
            prompt = "Write a complete modern Jetpack Compose screen for a crypto investment dashboard with a balance card, animated line chart preview, and transaction list.",
            icon = "code",
            modelRecommendation = "Claude 3.5 Sonnet"
        ),
        PromptTemplate(
            title = "Interactive HTML Game",
            prompt = "Build a complete single-file HTML5 interactive arcade game (like Retro Space Defender or Pong) with CSS styling and JavaScript game loop in a single artifact.",
            icon = "game",
            modelRecommendation = "Claude 3.7 Sonnet"
        ),
        PromptTemplate(
            title = "Debug & Refactor Code",
            prompt = "I have a complex algorithm that has an off-by-one error and race condition. Review the logic, explain why it happens, and provide the corrected version.",
            icon = "bug",
            modelRecommendation = "Claude 3.7 Sonnet"
        ),
        PromptTemplate(
            title = "System Architecture",
            prompt = "Design a high-throughput real-time notification engine supporting 10 million concurrent active users with message queues, backpressure handling, and failure recovery.",
            icon = "architecture",
            modelRecommendation = "Claude 3 Opus"
        ),
        PromptTemplate(
            title = "Full-Stack API Design",
            prompt = "Design a RESTful OpenAPI spec and Kotlin Ktor backend router for an e-commerce checkout flow with inventory reservation and idempotency keys.",
            icon = "api",
            modelRecommendation = "Claude 3.5 Sonnet"
        ),
        PromptTemplate(
            title = "Explain Complex Concept",
            prompt = "Explain how Transformer Attention mechanisms and KV Cache optimization work under the hood, using intuitive analogies and mathematical rigor.",
            icon = "school",
            modelRecommendation = "Claude 3 Opus"
        )
    )
}
