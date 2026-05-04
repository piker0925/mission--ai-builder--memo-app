package com.back.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.back.viewmodel.SearchState

class EditorVisualTransformation(
    private val searchState: SearchState,
    private val colorScheme: ColorScheme
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text.text)
        
        // 1. Markdown Highlighting
        applyMarkdownStyles(builder, text.text)
        
        // 2. Search Highlighting (Overwrites markdown if they overlap)
        applySearchStyles(builder, text.text)

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }

    private fun applyMarkdownStyles(builder: AnnotatedString.Builder, content: String) {
        // Headers (# Title)
        Regex("(?m)^#+.*$").findAll(content).forEach { 
            builder.addStyle(
                SpanStyle(
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp // Slightly larger for headers
                ), 
                it.range.first, it.range.last + 1
            )
        }

        // Bold (**text**)
        Regex("\\*\\*.*?\\*\\*").findAll(content).forEach {
            builder.addStyle(
                SpanStyle(fontWeight = FontWeight.Bold, color = colorScheme.secondary),
                it.range.first, it.range.last + 1
            )
        }

        // Italic (*text*)
        Regex("(?<!\\*)\\*[^\\*]+?\\*(?!\\*)").findAll(content).forEach {
            builder.addStyle(
                SpanStyle(fontStyle = FontStyle.Italic),
                it.range.first, it.range.last + 1
            )
        }

        // Blockquotes (> Quote)
        Regex("(?m)^>.*$").findAll(content).forEach {
            builder.addStyle(
                SpanStyle(color = colorScheme.outline, fontStyle = FontStyle.Italic),
                it.range.first, it.range.last + 1
            )
        }

        // Inline Code (`code`)
        Regex("`.*?`").findAll(content).forEach {
            builder.addStyle(
                SpanStyle(
                    background = colorScheme.surfaceVariant,
                    color = colorScheme.onSurfaceVariant
                ),
                it.range.first, it.range.last + 1
            )
        }
    }

    private fun applySearchStyles(builder: AnnotatedString.Builder, content: String) {
        if (!searchState.isVisible || searchState.query.isEmpty()) return

        searchState.results.forEachIndexed { index, range ->
            val isCurrent = index == searchState.currentIndex
            builder.addStyle(
                SpanStyle(
                    background = if (isCurrent) colorScheme.primary else colorScheme.primaryContainer,
                    color = if (isCurrent) Color.White else colorScheme.onPrimaryContainer
                ),
                range.first.coerceIn(0, content.length),
                (range.last + 1).coerceIn(0, content.length)
            )
        }
    }
}