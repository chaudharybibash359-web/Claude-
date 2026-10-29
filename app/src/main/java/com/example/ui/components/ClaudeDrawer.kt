package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.ui.theme.ClaudeGreen
import com.example.ui.theme.ClaudeTerracotta
import com.example.ui.theme.ClaudeTerracottaContainer
import com.google.firebase.auth.FirebaseUser

@Composable
fun ClaudeDrawer(
    conversations: List<ConversationEntity>,
    currentConversationId: String?,
    currentUser: FirebaseUser?,
    onSelectConversation: (ConversationEntity) -> Unit,
    onNewChat: () -> Unit,
    onNavigateArtifacts: () -> Unit,
    onNavigateProjects: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateAuth: () -> Unit,
    onDeleteConversation: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(310.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ClaudeTerracotta),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✳",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Claude AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Premium Edition • All Models Free",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClaudeTerracotta,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // User / Account Card in Drawer
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onNavigateAuth()
                        onCloseDrawer()
                    }
                    .testTag("drawer_account_card"),
                shape = RoundedCornerShape(12.dp),
                color = if (currentUser != null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) else ClaudeTerracottaContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (currentUser != null) ClaudeGreen else ClaudeTerracotta),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentUser != null) {
                            Text(
                                text = (currentUser.displayName?.take(1) ?: "U").uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.displayName ?: "Sign In to Sync History",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (currentUser != null) "Cloud Backup Active" else "Tap to connect Google account",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = if (currentUser != null) ClaudeGreen else ClaudeTerracotta
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // New Chat Primary Button
            Button(
                onClick = {
                    onNewChat()
                    onCloseDrawer()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("drawer_new_chat_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start New Chat", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Navigation Items
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Code, contentDescription = null, tint = ClaudeTerracotta) },
                label = { Text("Artifacts Studio", fontWeight = FontWeight.Medium) },
                selected = false,
                onClick = {
                    onNavigateArtifacts()
                    onCloseDrawer()
                },
                modifier = Modifier.padding(vertical = 2.dp),
                shape = RoundedCornerShape(10.dp)
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.FolderOpen, contentDescription = null, tint = ClaudeTerracotta) },
                label = { Text("Project Workspaces", fontWeight = FontWeight.Medium) },
                selected = false,
                onClick = {
                    onNavigateProjects()
                    onCloseDrawer()
                },
                modifier = Modifier.padding(vertical = 2.dp),
                shape = RoundedCornerShape(10.dp)
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                label = { Text("App Settings", fontWeight = FontWeight.Medium) },
                selected = false,
                onClick = {
                    onNavigateSettings()
                    onCloseDrawer()
                },
                modifier = Modifier.padding(vertical = 2.dp),
                shape = RoundedCornerShape(10.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )

            Text(
                text = "Recent Chats",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            // Conversations List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(conversations, key = { it.id }) { conv ->
                    val isSelected = conv.id == currentConversationId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) ClaudeTerracottaContainer.copy(alpha = 0.35f)
                                else Color.Transparent
                            )
                            .clickable {
                                onSelectConversation(conv)
                                onCloseDrawer()
                            }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = if (isSelected) ClaudeTerracotta else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = conv.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) ClaudeTerracotta else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { onDeleteConversation(conv.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete conversation",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
