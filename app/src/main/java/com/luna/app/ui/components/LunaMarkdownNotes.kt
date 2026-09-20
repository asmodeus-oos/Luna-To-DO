package com.luna.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.ui.theme.LunaTheme

@Composable
fun LunaMarkdownNotes(
    notes: String,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val lines = notes.lines()

    Column(modifier = modifier.fillMaxWidth()) {
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### ").trim(),
                        color = palette.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## ").trim(),
                        color = palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# ").trim(),
                        color = palette.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                }
                trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(palette.accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = palette.onAccent,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatInlineMarkdown(trimmed.substring(6)),
                            color = palette.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
                trimmed.startsWith("- [ ] ") -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(palette.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatInlineMarkdown(trimmed.substring(6)),
                            color = palette.textPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(palette.textTertiary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatInlineMarkdown(trimmed.substring(2)),
                            color = palette.textPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
                trimmed.isBlank() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }
                else -> {
                    Text(
                        text = formatInlineMarkdown(trimmed),
                        color = palette.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private fun formatInlineMarkdown(text: String) = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        if (text.startsWith("**", i)) {
            val end = text.indexOf("**", i + 2)
            if (end != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(text.substring(i + 2, end))
                }
                i = end + 2
                continue
            }
        } else if (text.startsWith("`", i)) {
            val end = text.indexOf("`", i + 1)
            if (end != -1) {
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x18008FFD))) {
                    append(text.substring(i + 1, end))
                }
                i = end + 1
                continue
            }
        } else if (text.startsWith("*", i)) {
            val end = text.indexOf("*", i + 1)
            if (end != -1) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(text.substring(i + 1, end))
                }
                i = end + 1
                continue
            }
        }
        append(text[i])
        i++
    }
}
