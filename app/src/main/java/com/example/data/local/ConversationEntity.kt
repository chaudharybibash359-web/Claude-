package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val modelId: String,
    val thinkingEnabled: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val projectId: String? = null,
    val isPinned: Boolean = false
)
