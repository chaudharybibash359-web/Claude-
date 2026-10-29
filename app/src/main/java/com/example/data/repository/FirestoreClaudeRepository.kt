package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ConversationEntity
import com.example.data.model.FirestoreConversation
import com.example.data.model.FirestoreMessage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRepository"

class FirestoreClaudeRepository(
    private val firestoreProvider: () -> FirebaseFirestore?
) {

    constructor(context: Context) : this({
        try {
            val dbId = context.applicationContext.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank()) {
                FirebaseFirestore.getInstance(dbId)
            } else {
                null
            }
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseFirestore instance unavailable: ${e.message}")
            null
        }
    })

    private val db: FirebaseFirestore?
        get() = try {
            firestoreProvider()
        } catch (e: Throwable) {
            null
        }

    private val auth: FirebaseAuth?
        get() = try {
            Firebase.auth
        } catch (e: Throwable) {
            null
        }

    suspend fun syncUserProfile(userId: String, displayName: String, email: String, photoUrl: String?) {
        val database = db ?: return
        try {
            val userRef = database.collection("users").document(userId)
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing user profile: ${e.message}")
        }
    }

    fun observeCloudConversations(userId: String): Flow<List<FirestoreConversation>> {
        val database = db ?: return emptyFlow()
        return try {
            database.collection("users").document(userId).collection("conversations")
                .snapshots()
                .map { snap ->
                    snap.toObjects(FirestoreConversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt ?: it.createdAt }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error observing cloud conversations: ${e.message}")
            emptyFlow()
        }
    }

    suspend fun saveCloudConversation(conversation: ConversationEntity) {
        val database = db ?: return
        val userId = auth?.currentUser?.uid ?: return
        try {
            val convRef = database.collection("users").document(userId)
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
            Log.w(TAG, "Error saving cloud conversation: ${e.message}")
        }
    }

    suspend fun saveCloudMessage(message: ChatMessageEntity) {
        val database = db ?: return
        val userId = auth?.currentUser?.uid ?: return
        try {
            val msgRef = database.collection("users").document(userId)
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
            Log.w(TAG, "Error saving cloud message: ${e.message}")
        }
    }

    suspend fun deleteCloudConversation(conversationId: String) {
        val database = db ?: return
        val userId = auth?.currentUser?.uid ?: return
        try {
            database.collection("users").document(userId)
                .collection("conversations").document(conversationId)
                .delete()
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Error deleting cloud conversation: ${e.message}")
        }
    }
}
