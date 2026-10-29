package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CodeBlockView(
    code: String,
    language: String,
    onOpenArtifact: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CodeBgLight)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1D1B))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = language.ifEmpty { "code" },
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ClaudeTerracotta
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onOpenArtifact != null) {
                    TextButton(
                        onClick = onOpenArtifact,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("open_artifact_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in Artifacts",
                            tint = Color(0xFFDDD9D3),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Artifact",
                            fontSize = 11.sp,
                            color = Color(0xFFDDD9D3)
                        )
                    }
                }

                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        isCopied = true
                        Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("copy_code_btn")
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = if (isCopied) ClaudeGreen else Color(0xFFDDD9D3),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCopied) "Copied!" else "Copy",
                        fontSize = 11.sp,
                        color = if (isCopied) ClaudeGreen else Color(0xFFDDD9D3)
                    )
                }
            }
        }

        // Code Content with syntax highlighting
        val horizontalScroll = rememberScrollState()
        val highlightedCode = remember(code, language) {
            buildSyntaxHighlightedString(code, language)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
                .padding(14.dp)
        ) {
            Text(
                text = highlightedCode,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

private fun buildSyntaxHighlightedString(code: String, language: String): AnnotatedString {
    val keywords = setOf(
        "val", "var", "fun", "class", "object", "interface", "import", "package",
        "return", "if", "else", "when", "for", "while", "true", "false", "null",
        "def", "import", "from", "as", "class", "return", "lambda", "async", "await",
        "function", "const", "let", "var", "export", "default", "typeof",
        "public", "private", "protected", "override", "data", "sealed"
    )

    return buildAnnotatedString {
        val lines = code.lines()
        lines.forEachIndexed { lineIdx, line ->
            // Line number
            append("${(lineIdx + 1).toString().padStart(3, ' ')}  ")
            val lineNumberEnd = length
            addStyle(SpanStyle(color = Color(0xFF5C5855)), lineNumberEnd - 5, lineNumberEnd)

            val words = line.split(Regex("(?<=[^a-zA-Z0-9_])|(?=[^a-zA-Z0-9_])"))
            for (word in words) {
                val start = length
                append(word)
                when {
                    word in keywords -> addStyle(SpanStyle(color = CodeKeyword, fontWeight = FontWeight.SemiBold), start, length)
                    word.startsWith("\"") || word.endsWith("\"") || word.startsWith("'") || word.endsWith("'") ->
                        addStyle(SpanStyle(color = CodeString), start, length)
                    word.all { it.isDigit() } -> addStyle(SpanStyle(color = CodeNumber), start, length)
                    word.startsWith("//") || word.startsWith("#") ->
                        addStyle(SpanStyle(color = CodeComment), start, length)
                    word.isNotEmpty() && word[0].isUpperCase() ->
                        addStyle(SpanStyle(color = CodeFunction), start, length)
                    else -> addStyle(SpanStyle(color = CodeText), start, length)
                }
            }
            if (lineIdx < lines.size - 1) {
                append("\n")
            }
        }
    }
}
