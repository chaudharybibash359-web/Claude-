package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.ArtifactEntity
import com.example.ui.components.ClaudeDrawer
import com.example.ui.components.ClaudeTopBar
import com.example.ui.components.ModelSelectorSheet
import com.example.ui.screens.ArtifactViewerScreen
import com.example.ui.screens.ArtifactsListScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ClaudeTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.configureAppCheck
import kotlinx.coroutines.launch

enum class AppDestination {
    CHAT,
    ARTIFACT_VIEWER,
    ARTIFACTS_LIST,
    PROJECTS,
    SETTINGS,
    AUTH
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration before UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            ClaudeTheme {
                ClaudeAppRoot()
            }
        }
    }
}

@Composable
fun ClaudeAppRoot(
    viewModel: ChatViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val messages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val allArtifacts by viewModel.allArtifacts.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var currentDestination by remember { mutableStateOf(AppDestination.CHAT) }
    var showModelSelector by remember { mutableStateOf(false) }

    // Intercept back button when not on main chat screen
    if (currentDestination != AppDestination.CHAT) {
        BackHandler {
            if (currentDestination == AppDestination.ARTIFACT_VIEWER) {
                viewModel.inspectArtifact(null)
            }
            currentDestination = AppDestination.CHAT
        }
    }

    // Auto navigate to artifact viewer if an artifact is inspected
    LaunchedEffect(uiState.inspectingArtifact) {
        if (uiState.inspectingArtifact != null) {
            currentDestination = AppDestination.ARTIFACT_VIEWER
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentDestination == AppDestination.CHAT,
        drawerContent = {
            ClaudeDrawer(
                conversations = conversations,
                currentConversationId = uiState.currentConversation?.id,
                currentUser = authUiState.user,
                onSelectConversation = { conv ->
                    viewModel.selectConversation(conv)
                    currentDestination = AppDestination.CHAT
                },
                onNewChat = {
                    viewModel.startNewChat()
                    currentDestination = AppDestination.CHAT
                },
                onNavigateArtifacts = {
                    currentDestination = AppDestination.ARTIFACTS_LIST
                },
                onNavigateProjects = {
                    currentDestination = AppDestination.PROJECTS
                },
                onNavigateSettings = {
                    currentDestination = AppDestination.SETTINGS
                },
                onNavigateAuth = {
                    currentDestination = AppDestination.AUTH
                },
                onDeleteConversation = { id ->
                    viewModel.deleteConversation(id)
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (currentDestination == AppDestination.CHAT) {
                    ClaudeTopBar(
                        selectedModel = uiState.selectedModel,
                        thinkingEnabled = uiState.thinkingEnabled,
                        onMenuClick = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        onModelClick = {
                            showModelSelector = true
                        },
                        onToggleThinking = {
                            viewModel.toggleThinking(!uiState.thinkingEnabled)
                        },
                        onNewChatClick = {
                            viewModel.startNewChat()
                        }
                    )
                }
            }
        ) { innerPadding ->
            when (currentDestination) {
                AppDestination.CHAT -> {
                    ChatScreen(
                        uiState = uiState,
                        messages = messages,
                        onSendMessage = { text -> viewModel.sendMessage(text) },
                        onToggleThinking = { viewModel.toggleThinking(!uiState.thinkingEnabled) },
                        onInspectArtifact = { art ->
                            viewModel.inspectArtifact(art)
                            currentDestination = AppDestination.ARTIFACT_VIEWER
                        },
                        onClearError = { viewModel.clearError() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppDestination.ARTIFACT_VIEWER -> {
                    val artifact = uiState.inspectingArtifact ?: ArtifactEntity(
                        id = "default",
                        messageId = "default",
                        conversationId = "default",
                        title = "Code Artifact",
                        type = "code",
                        language = "kotlin",
                        content = "// No artifact loaded",
                        timestamp = System.currentTimeMillis()
                    )
                    ArtifactViewerScreen(
                        artifact = artifact,
                        onBack = {
                            viewModel.inspectArtifact(null)
                            currentDestination = AppDestination.CHAT
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppDestination.ARTIFACTS_LIST -> {
                    ArtifactsListScreen(
                        artifacts = allArtifacts,
                        onSelectArtifact = { art ->
                            viewModel.inspectArtifact(art)
                            currentDestination = AppDestination.ARTIFACT_VIEWER
                        },
                        onBack = { currentDestination = AppDestination.CHAT },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppDestination.PROJECTS -> {
                    ProjectsScreen(
                        projects = projects,
                        onSelectProject = { proj ->
                            viewModel.startNewChat(projectId = proj.id)
                            currentDestination = AppDestination.CHAT
                        },
                        onCreateProject = { name, desc, instructions ->
                            viewModel.createProject(name, desc, instructions)
                            currentDestination = AppDestination.CHAT
                        },
                        onDeleteProject = { id -> viewModel.deleteProject(id) },
                        onBack = { currentDestination = AppDestination.CHAT },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppDestination.SETTINGS -> {
                    SettingsScreen(
                        currentUser = authUiState.user,
                        onNavigateAuth = { currentDestination = AppDestination.AUTH },
                        onBack = { currentDestination = AppDestination.CHAT },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppDestination.AUTH -> {
                    AuthScreen(
                        authUiState = authUiState,
                        onSignIn = { authViewModel.signInWithGoogle(context) },
                        onSignOut = { authViewModel.signOut() },
                        onBack = { currentDestination = AppDestination.CHAT },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    if (showModelSelector) {
        ModelSelectorSheet(
            selectedModel = uiState.selectedModel,
            onModelSelected = { model ->
                viewModel.setModel(model)
            },
            onDismiss = {
                showModelSelector = false
            }
        )
    }
}
