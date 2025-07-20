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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownRenderer(
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
                        text = parseInlineMarkdown(line.substring(2)),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                line.startsWith("## ") -> {
                    Text(
                        text = parseInlineMarkdown(line.substring(3)),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                line.startsWith("### ") -> {
                    Text(
                        text = parseInlineMarkdown(line.substring(4)),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                line.startsWith("#### ") -> {
                    Text(
                        text = parseInlineMarkdown(line.substring(5)),
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
                        text = buildAnnotatedString {
                            append("• ")
                            append(parseInlineMarkdown(line.substring(2)))
                        },
                        style = style,
                        color = color,
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
                    )
                }

                // Numbered lists
                line.matches(Regex("^\\d+\\. .*")) -> {
                    Text(
                        text = parseInlineMarkdown(line),
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

private fun parseInlineMarkdown(text: String) = buildAnnotatedString {
    val input = text
    var index = 0

    while (index < input.length) {
        // Look for **bold**
        val boldStart = input.indexOf("**", index)
        if (boldStart != -1) {
            // Add text before bold
            append(input.substring(index, boldStart))

            // Find closing **
            val boldEnd = input.indexOf("**", boldStart + 2)
            if (boldEnd != -1) {
                // Add bold text
                val boldText = input.substring(boldStart + 2, boldEnd)
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(boldText)
                }
                index = boldEnd + 2
            } else {
                // No closing **, just add the remaining text
                append(input.substring(boldStart))
                break
            }
        } else {
            // No more bold formatting, add remaining text
            append(input.substring(index))
            break
        }
    }
}