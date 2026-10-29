package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ArtifactEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.model.PromptTemplates
import com.example.ui.components.*
import com.example.ui.theme.ClaudeTerracotta
import com.example.ui.viewmodel.ChatUiState
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    messages: List<ChatMessageEntity>,
    onSendMessage: (String) -> Unit,
    onToggleThinking: () -> Unit,
    onInspectArtifact: (ArtifactEntity) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive or when streaming
    LaunchedEffect(messages.size, uiState.streamingText) {
        if (messages.isNotEmpty() || uiState.isStreaming) {
            val targetIdx = messages.size + (if (uiState.isStreaming) 1 else 0)
            if (targetIdx > 0) {
                listState.animateScrollToItem(targetIdx - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Error banner
        if (uiState.errorMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onClearError,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss error",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Active Project banner if in project workspace
        if (uiState.activeProject != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Project Workspace: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.activeProject.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ClaudeTerracotta
                    )
                }
            }
        }

        // Main content list
        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty() && !uiState.isStreaming) {
                // Empty state greeting with starter prompt chips
                EmptyChatWelcome(
                    selectedModelName = uiState.selectedModel.name,
                    onSelectPrompt = { prompt ->
                        inputText = prompt
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            onInspectArtifact = onInspectArtifact
                        )
                    }

                    // Streaming assistant bubble
                    if (uiState.isStreaming) {
                        item {
                            StreamingAssistantBubble(
                                text = uiState.streamingText,
                                thinking = uiState.streamingThinking,
                                modelName = uiState.selectedModel.name
                            )
                        }
                    }
                }
            }
        }

        // Bottom Input bar
        ChatInputBar(
            inputText = inputText,
            onInputChange = { inputText = it },
            onSend = {
                onSendMessage(inputText)
                inputText = ""
            },
            isStreaming = uiState.isStreaming,
            selectedModel = uiState.selectedModel,
            thinkingEnabled = uiState.thinkingEnabled,
            onToggleThinking = onToggleThinking
        )
    }
}

@Composable
private fun EmptyChatWelcome(
    selectedModelName: String,
    onSelectPrompt: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Claude Star Logo
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(ClaudeTerracotta),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✳",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Claude AI Premium",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Active: $selectedModelName • Unlimited Queries",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Text(
            text = "Suggested starting points:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 10.dp)
        )

        // Starter Chips Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PromptTemplates.STARTERS.take(4).forEach { template ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { onSelectPrompt(template.prompt) }
                        .testTag("starter_prompt_${template.title.lowercase().replace(" ", "_")}"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = ClaudeTerracotta,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = template.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = template.prompt,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessageEntity,
    onInspectArtifact: (ArtifactEntity) -> Unit
) {
    val isUser = message.sender == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // User Message Bubble
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // Assistant Message
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header with Claude Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(ClaudeTerracotta),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✳",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = message.modelUsed ?: "Claude",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Thinking Box if present
                if (!message.thinkingContent.isNullOrBlank()) {
                    ThinkingBox(thinkingText = message.thinkingContent)
                }

                // Assistant formatted text & code blocks
                FormattedAssistantText(
                    rawContent = message.content,
                    onOpenArtifact = {
                        val mockArt = ArtifactEntity(
                            id = message.id,
                            messageId = message.id,
                            conversationId = message.conversationId,
                            title = "Generated Code Artifact",
                            type = "code",
                            language = "code",
                            content = message.content,
                            timestamp = message.timestamp
                        )
                        onInspectArtifact(mockArt)
                    }
                )
            }
        }
    }
}

@Composable
private fun StreamingAssistantBubble(
    text: String,
    thinking: String?,
    modelName: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(ClaudeTerracotta),
                contentAlignment = Alignment.Center
            ) {
                Text("✳", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "$modelName (Generating...)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ClaudeTerracotta
            )
        }

        if (!thinking.isNullOrBlank()) {
            ThinkingBox(thinkingText = thinking, initialExpanded = true)
        }

        if (text.isNotBlank()) {
            FormattedAssistantText(rawContent = text, onOpenArtifact = null)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = ClaudeTerracotta)
                Text(
                    text = "Claude is thinking...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FormattedAssistantText(
    rawContent: String,
    onOpenArtifact: (() -> Unit)?
) {
    // Splits markdown code blocks and normal paragraphs
    val parts = rawContent.split("```")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Code block
                val firstLineBreak = part.indexOf('\n')
                val language = if (firstLineBreak != -1) part.substring(0, firstLineBreak).trim() else ""
                val code = if (firstLineBreak != -1) part.substring(firstLineBreak + 1) else part
                CodeBlockView(
                    code = code.trim(),
                    language = language,
                    onOpenArtifact = onOpenArtifact
                )
            } else {
                // Regular prose
                val trimmed = part.trim()
                if (trimmed.isNotEmpty()) {
                    Text(
                        text = trimmed,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}
