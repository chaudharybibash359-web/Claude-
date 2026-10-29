package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUser(
    @DocumentId val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreConversation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val modelId: String = "",
    val thinkingEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreMessage(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val userId: String = "",
    val sender: String = "user",
    val content: String = "",
    val thinkingContent: String? = null,
    @ServerTimestamp val timestamp: Timestamp? = null,
    val modelUsed: String? = null
)
