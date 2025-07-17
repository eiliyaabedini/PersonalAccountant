package ir.act.personalAccountant.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val lines = text.split('\n')

    Column(modifier = modifier) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            when {
                // Headers
                line.startsWith("# ") -> {
                    Text(
                        text = line.substring(2),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                line.startsWith("## ") -> {
                    Text(
                        text = line.substring(3),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                line.startsWith("### ") -> {
                    Text(
                        text = line.substring(4),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                line.startsWith("#### ") -> {
                    Text(
                        text = line.substring(5),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Bullet points
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Text(
                        text = "• ${line.substring(2)}",
                        style = style,
                        color = color,
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
                    )
                }

                // Numbered lists
                line.matches(Regex("^\\d+\\. .*")) -> {
                    Text(
                        text = line,
                        style = style,
                        color = color,
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
                    )
                }

                // Code blocks
                line.startsWith("```") -> {
                    val codeLines = mutableListOf<String>()
                    i++ // Skip opening ```

                    while (i < lines.size && !lines[i].startsWith("```")) {
                        codeLines.add(lines[i])
                        i++
                    }

                    if (codeLines.isNotEmpty()) {
                        Text(
                            text = codeLines.joinToString("\\n"),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }
                }

                // Regular text with inline formatting
                line.isNotBlank() -> {
                    Text(
                        text = parseInlineMarkdown(line),
                        style = style,
                        color = color,
                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                    )
                }

                // Empty lines for spacing
                line.isBlank() -> {
                    // Add small spacing for empty lines
                    if (i > 0 && i < lines.size - 1) {
                        Text(
                            text = "",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                        )
                    }
                }
            }

            i++
        }
    }
}

@Composable
private fun parseInlineMarkdown(text: String) = buildAnnotatedString {
    var currentPos = 0
    val length = text.length

    while (currentPos < length) {
        var foundMatch = false

        // Check for **bold** (must be checked first to avoid conflicts)
        if (currentPos <= length - 2 && text[currentPos] == '*' && text[currentPos + 1] == '*') {
            val endPos = text.indexOf("**", currentPos + 2)
            if (endPos != -1 && endPos > currentPos + 2) {
                val content = text.substring(currentPos + 2, endPos)
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(content)
                }
                currentPos = endPos + 2
                foundMatch = true
            }
        }

        // Check for `code`
        if (!foundMatch && currentPos < length && text[currentPos] == '`') {
            val endPos = text.indexOf('`', currentPos + 1)
            if (endPos != -1 && endPos > currentPos) {
                val content = text.substring(currentPos + 1, endPos)
                withStyle(
                    style = SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                ) {
                    append(content)
                }
                currentPos = endPos + 1
                foundMatch = true
            }
        }

        // Check for [link](url)
        if (!foundMatch && currentPos < length && text[currentPos] == '[') {
            val endBracket = text.indexOf(']', currentPos + 1)
            if (endBracket != -1 && endBracket + 1 < length && text[endBracket + 1] == '(') {
                val endParen = text.indexOf(')', endBracket + 2)
                if (endParen != -1) {
                    val linkText = text.substring(currentPos + 1, endBracket)
                    withStyle(
                        style = SpanStyle(
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(linkText)
                    }
                    currentPos = endParen + 1
                    foundMatch = true
                }
            }
        }

        // Check for *italic* (only single asterisks, not part of **)
        if (!foundMatch && currentPos < length && text[currentPos] == '*') {
            // Make sure it's not part of **
            val isPartOfBold = (currentPos > 0 && text[currentPos - 1] == '*') ||
                    (currentPos + 1 < length && text[currentPos + 1] == '*')

            if (!isPartOfBold) {
                val endPos = text.indexOf('*', currentPos + 1)
                if (endPos != -1 && endPos > currentPos + 1) {
                    // Make sure the closing * is not part of **
                    val isClosingPartOfBold = (endPos + 1 < length && text[endPos + 1] == '*') ||
                            (endPos > 0 && text[endPos - 1] == '*')

                    if (!isClosingPartOfBold) {
                        val content = text.substring(currentPos + 1, endPos)
                        if (content.isNotEmpty() && !content.contains('\n')) {
                            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(content)
                            }
                            currentPos = endPos + 1
                            foundMatch = true
                        }
                    }
                }
            }
        }

        // If no match found, add the current character and move forward
        if (!foundMatch) {
            append(text[currentPos])
            currentPos++
        }
    }
}