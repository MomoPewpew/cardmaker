package com.momo.cardmaker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatAlignLeft
import androidx.compose.material.icons.automirrored.outlined.FormatAlignRight
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momo.cardmaker.RichTextParameter

/** A composable object to render the buttons for formatting a Rich Text parameter. */
@Composable
fun RichTextStyleRow(
    modifier: Modifier = Modifier,
    parameter: RichTextParameter
) {
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        item {
            RichTextStyleButton(
                onClick = {
                    parameter.richTextState.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Left,
                        )
                    )
                },
                isSelected = parameter.richTextState.currentParagraphStyle.textAlign == TextAlign.Left,
                icon = Icons.AutoMirrored.Outlined.FormatAlignLeft
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    parameter.richTextState.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Center
                        )
                    )
                },
                isSelected = parameter.richTextState.currentParagraphStyle.textAlign == TextAlign.Center,
                icon = Icons.Outlined.FormatAlignCenter
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    parameter.richTextState.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Right
                        )
                    )
                },
                isSelected = parameter.richTextState.currentParagraphStyle.textAlign == TextAlign.Right,
                icon = Icons.AutoMirrored.Outlined.FormatAlignRight
            )
        }

        item {
            Box(
                Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color(0xFF393B3D))
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    parameter.richTextState.toggleSpanStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                isSelected = parameter.richTextState.currentSpanStyle.fontWeight == FontWeight.Bold,
                icon = Icons.Outlined.FormatBold
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    parameter.richTextState.toggleSpanStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic
                        )
                    )
                },
                isSelected = parameter.richTextState.currentSpanStyle.fontStyle == FontStyle.Italic,
                icon = Icons.Outlined.FormatItalic
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    parameter.richTextState.toggleSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.Underline
                        )
                    )
                },
                isSelected = parameter.richTextState.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true,
                icon = Icons.Outlined.FormatUnderlined
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    parameter.richTextState.toggleSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                },
                isSelected = parameter.richTextState.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true,
                icon = Icons.Outlined.FormatStrikethrough
            )
        }

        item {
            Box(
                Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color(0xFF393B3D))
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    parameter.richTextState.toggleSpanStyle(
                        SpanStyle(
                            color = Color(parameter.color.value)
                        )
                    )
                },
                isSelected = parameter.richTextState.currentSpanStyle.color == Color(parameter.color.value),
                icon = Icons.Filled.Circle,
                tint = Color(parameter.color.value)
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    ColorPickerState.pick(parameter.color)
                },
                isSelected = false,
                icon = Icons.Outlined.Palette
            )
        }

        item {
            Box(
                Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color(0xFF393B3D))
            )
        }

        item {
            FontSizeDropdownMenu(
                onSizeSelected = { size ->
                    parameter.richTextState.addSpanStyle(SpanStyle(fontSize = size.sp))
                },
                selectedSize = if (parameter.richTextState.currentSpanStyle.fontSize == TextUnit.Unspecified) 14f else parameter.richTextState.currentSpanStyle.fontSize.value
            )
        }

        item {
            Box(
                Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color(0xFF393B3D))
            )
        }

        item {
            FontDropdownMenu(
                onFontSelected = { familyName ->
                    parameter.applyFontFamily(familyName)
                },
                selectedFont = FontDropdownState.fontFamilyMap.entries.find { it.value == parameter.richTextState.currentSpanStyle.fontFamily }?.key
                    ?: "Font Selection"
            )
        }

        item {
            Box(
                Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color(0xFF393B3D))
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    parameter.richTextState.toggleUnorderedList()
                },
                isSelected = parameter.richTextState.isUnorderedList,
                icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    parameter.richTextState.toggleOrderedList()
                },
                isSelected = parameter.richTextState.isOrderedList,
                icon = Icons.Outlined.FormatListNumbered,
            )
        }
    }
}