package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ConversationEntity
import com.example.data.model.FirestoreConversation
import com.example.data.model.FirestoreMessage
import com.example.data.model.FirestoreUser
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRepository"

class FirestoreClaudeRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: error("User must be authenticated to perform Firestore operations")
    }

    suspend fun syncUserProfile(userId: String, displayName: String, email: String, photoUrl: String?) {
        val userRef = db.collection("users").document(userId)
        val snapshot = userRef.get().await()

        val data = mutableMapOf<String, Any>(
            "id" to userId,
            "displayName" to displayName,
            "email" to email,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (photoUrl != null) {
            data["photoUrl"] = photoUrl
        }

        if (!snapshot.exists()) {
            data["createdAt"] = FieldValue.serverTimestamp()
            userRef.set(data).await()
        } else {
            userRef.update(data).await()
        }
    }

    fun observeCloudConversations(userId: String): Flow<List<FirestoreConversation>> {
        return db.collection("users").document(userId).collection("conversations")
            .snapshots()
            .map { snap ->
                snap.toObjects(FirestoreConversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.updatedAt ?: it.createdAt }
            }
    }

    suspend fun saveCloudConversation(conversation: ConversationEntity) {
        val userId = auth.currentUser?.uid ?: return
        try {
            val convRef = db.collection("users").document(userId)
                .collection("conversations").document(conversation.id)

            val data = mapOf(
                "id" to conversation.id,
                "userId" to userId,
                "title" to conversation.title,
                "modelId" to conversation.modelId,
                "thinkingEnabled" to conversation.thinkingEnabled,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            convRef.set(data).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cloud conversation", e)
        }
    }

    suspend fun saveCloudMessage(message: ChatMessageEntity) {
        val userId = auth.currentUser?.uid ?: return
        try {
            val msgRef = db.collection("users").document(userId)
                .collection("conversations").document(message.conversationId)
                .collection("messages").document(message.id)

            val data = mutableMapOf<String, Any>(
                "id" to message.id,
                "conversationId" to message.conversationId,
                "userId" to userId,
                "sender" to message.sender,
                "content" to message.content,
                "timestamp" to FieldValue.serverTimestamp()
            )
            if (!message.thinkingContent.isNullOrBlank()) {
                data["thinkingContent"] = message.thinkingContent
            }
            if (!message.modelUsed.isNullOrBlank()) {
                data["modelUsed"] = message.modelUsed
            }

            msgRef.set(data).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cloud message", e)
        }
    }

    suspend fun deleteCloudConversation(conversationId: String) {
        val userId = auth.currentUser?.uid ?: return
        try {
            db.collection("users").document(userId)
                .collection("conversations").document(conversationId)
                .delete()
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting cloud conversation", e)
        }
    }
}
