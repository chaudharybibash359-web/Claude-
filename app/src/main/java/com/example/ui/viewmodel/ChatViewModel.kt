package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ArtifactEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ClaudeDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.ProjectEntity
import com.example.data.model.ClaudeModel
import com.example.data.model.ClaudeModels
import com.example.data.repository.ClaudeRepository
import com.example.data.repository.FirestoreClaudeRepository
import com.example.data.repository.StreamChunk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatUiState(
    val currentConversation: ConversationEntity? = null,
    val selectedModel: ClaudeModel = ClaudeModels.OPUS_5_5,
    val thinkingEnabled: Boolean = true,
    val activeProject: ProjectEntity? = null,
    val isStreaming: Boolean = false,
    val streamingText: String = "",
    val streamingThinking: String? = null,
    val errorMessage: String? = null,
    val inspectingArtifact: ArtifactEntity? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ClaudeDatabase.getInstance(application)
    private val repository = ClaudeRepository(db.claudeDao())
    private val firestoreRepo = FirestoreClaudeRepository(application)

    val conversations: StateFlow<List<ConversationEntity>> = repository.getConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArtifacts: StateFlow<List<ArtifactEntity>> = repository.getAllArtifacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<ProjectEntity>> = repository.getProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<ChatMessageEntity>> = _activeMessages.asStateFlow()

    init {
        // Create initial conversation or pick latest once on startup
        viewModelScope.launch {
            try {
                val list = repository.getConversations().first()
                if (list.isNotEmpty()) {
                    selectConversation(list.first())
                } else {
                    startNewChat()
                }
            } catch (e: Exception) {
                startNewChat()
            }
        }
    }

    fun startNewChat(projectId: String? = null) {
        viewModelScope.launch {
            val model = _uiState.value.selectedModel
            val conv = repository.createConversation(
                title = "New Conversation",
                modelId = model.id,
                thinkingEnabled = _uiState.value.thinkingEnabled,
                projectId = projectId
            )
            firestoreRepo.saveCloudConversation(conv)
            val project = projectId?.let { repository.getProject(it) }
            _uiState.value = _uiState.value.copy(
                currentConversation = conv,
                activeProject = project,
                streamingText = "",
                streamingThinking = null,
                errorMessage = null,
                inspectingArtifact = null
            )
            observeMessages(conv.id)
        }
    }

    fun selectConversation(conversation: ConversationEntity) {
        viewModelScope.launch {
            val model = ClaudeModels.getById(conversation.modelId)
            val project = conversation.projectId?.let { repository.getProject(it) }
            _uiState.value = _uiState.value.copy(
                currentConversation = conversation,
                selectedModel = model,
                thinkingEnabled = conversation.thinkingEnabled,
                activeProject = project,
                streamingText = "",
                streamingThinking = null,
                errorMessage = null,
                inspectingArtifact = null
            )
            observeMessages(conversation.id)
        }
    }

    private fun observeMessages(conversationId: String) {
        viewModelScope.launch {
            repository.getMessages(conversationId).collect { msgs ->
                _activeMessages.value = msgs
            }
        }
    }

    fun setModel(model: ClaudeModel) {
        _uiState.value = _uiState.value.copy(
            selectedModel = model,
            thinkingEnabled = if (model.supportsThinking) _uiState.value.thinkingEnabled else false
        )
        val conv = _uiState.value.currentConversation ?: return
        viewModelScope.launch {
            repository.updateConversationModel(conv.id, model.id, _uiState.value.thinkingEnabled)
        }
    }

    fun toggleThinking(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(thinkingEnabled = enabled)
        val conv = _uiState.value.currentConversation ?: return
        viewModelScope.launch {
            repository.updateConversationModel(conv.id, _uiState.value.selectedModel.id, enabled)
        }
    }

    fun sendMessage(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isEmpty() || _uiState.value.isStreaming) return

        val conv = _uiState.value.currentConversation ?: return
        val model = _uiState.value.selectedModel
        val thinking = _uiState.value.thinkingEnabled
        val projectPrompt = _uiState.value.activeProject?.systemPrompt

        viewModelScope.launch {
            // Save user message locally and to cloud
            val userMsg = repository.saveUserMessage(conv.id, trimmed)
            firestoreRepo.saveCloudMessage(userMsg)

            _uiState.value = _uiState.value.copy(
                isStreaming = true,
                streamingText = "",
                streamingThinking = null,
                errorMessage = null
            )

            val history = _activeMessages.value
            repository.streamAssistantResponse(
                conversationId = conv.id,
                userPrompt = trimmed,
                model = model,
                thinkingEnabled = thinking,
                previousMessages = history,
                projectSystemPrompt = projectPrompt
            ).collect { chunk ->
                when (chunk) {
                    is StreamChunk.Content -> {
                        _uiState.value = _uiState.value.copy(streamingText = chunk.partialText)
                    }
                    is StreamChunk.Thinking -> {
                        _uiState.value = _uiState.value.copy(streamingThinking = chunk.text)
                    }
                    is StreamChunk.Completed -> {
                        _uiState.value = _uiState.value.copy(
                            isStreaming = false,
                            streamingText = "",
                            streamingThinking = null,
                            inspectingArtifact = chunk.artifacts.firstOrNull() ?: _uiState.value.inspectingArtifact
                        )
                    }
                    is StreamChunk.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isStreaming = false,
                            errorMessage = chunk.message
                        )
                    }
                }
            }
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            firestoreRepo.deleteCloudConversation(id)
            if (_uiState.value.currentConversation?.id == id) {
                val remaining = conversations.value.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    selectConversation(remaining.first())
                } else {
                    startNewChat()
                }
            }
        }
    }

    fun createProject(name: String, description: String, instructions: String) {
        viewModelScope.launch {
            val proj = repository.createProject(name, description, instructions)
            startNewChat(projectId = proj.id)
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    fun inspectArtifact(artifact: ArtifactEntity?) {
        _uiState.value = _uiState.value.copy(inspectingArtifact = artifact)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
