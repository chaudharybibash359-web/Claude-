package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.ArtifactEntity
import com.example.ui.components.CodeBlockView
import com.example.ui.theme.ClaudeTerracotta
import com.example.util.ClaudeParser

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtifactViewerScreen(
    artifact: ArtifactEntity,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(if (artifact.type == "html" || artifact.type == "svg") 0 else 1) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = artifact.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Artifact • ${artifact.language.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClaudeTerracotta
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("artifact_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to chat"
                        )
                    }
                },
                actions = {
                    // Copy code
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(artifact.content))
                            Toast.makeText(context, "Copied code artifact", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("artifact_copy_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code"
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, artifact.content)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Artifact"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share artifact"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Selector (Preview vs Code)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ClaudeTerracotta
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Live Preview", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    modifier = Modifier.testTag("artifact_preview_tab")
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Code", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    modifier = Modifier.testTag("artifact_code_tab")
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                if (selectedTab == 0) {
                    var webViewError by remember { mutableStateOf(false) }

                    if (webViewError) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Live preview is not supported on this device. Displaying source code:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            CodeBlockView(
                                code = artifact.content,
                                language = artifact.language,
                                onOpenArtifact = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        // Sandbox Preview in WebView
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            DisposableEffect(artifact.id) {
                                onDispose {
                                    // Clean disposal
                                }
                            }
                            AndroidView(
                                factory = { ctx ->
                                    try {
                                        WebView(ctx).apply {
                                            webViewClient = WebViewClient()
                                            settings.javaScriptEnabled = true
                                            settings.domStorageEnabled = true
                                            settings.useWideViewPort = true
                                            settings.loadWithOverviewMode = true
                                            settings.cacheMode = WebSettings.LOAD_NO_CACHE
                                            settings.allowFileAccess = false
                                            settings.allowContentAccess = false
                                            val html = ClaudeParser.prepareHtmlForPreview(artifact.content, artifact.type)
                                            loadDataWithBaseURL("about:blank", html, "text/html", "UTF-8", null)
                                        }
                                    } catch (e: Throwable) {
                                        webViewError = true
                                        android.view.View(ctx)
                                    }
                                },
                                update = { webView ->
                                    if (webView is WebView) {
                                        try {
                                            val html = ClaudeParser.prepareHtmlForPreview(artifact.content, artifact.type)
                                            webView.loadDataWithBaseURL("about:blank", html, "text/html", "UTF-8", null)
                                        } catch (e: Throwable) {
                                            webViewError = true
                                        }
                                    }
                                },
                                onRelease = { webView ->
                                    if (webView is WebView) {
                                        try {
                                            webView.stopLoading()
                                            webView.destroy()
                                        } catch (e: Throwable) {
                                            // Ignore cleanup error
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("artifact_webview")
                            )
                        }
                    }
                } else {
                    // Code View
                    CodeBlockView(
                        code = artifact.content,
                        language = artifact.language,
                        onOpenArtifact = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
