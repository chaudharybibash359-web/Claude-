package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artifacts",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"]), Index(value = ["messageId"])]
)
data class ArtifactEntity(
    @PrimaryKey
    val id: String,
    val messageId: String,
    val conversationId: String,
    val title: String,
    val type: String, // "html", "code", "markdown", "svg"
    val language: String,
    val content: String,
    val timestamp: Long
)
