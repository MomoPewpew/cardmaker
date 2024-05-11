package com.momo.cardmaker.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import com.momo.cardmaker.*

/** Build composables for previewing cards. */
@Composable
fun CardPreview(modifier: Modifier = Modifier, textMeasurer: TextMeasurer) {
    var modified = modifier

    modified =
        modified.aspectRatio(CardState.card.value.resolutionHoriz.value / CardState.card.value.resolutionVert.value)

    modified = if (showBorder.value) {
        modified.clip(
            RoundedCornerShape(
                topStartPercent = 4,
                topEndPercent = 4,
                bottomStartPercent = 4,
                bottomEndPercent = 4
            )
        )
    } else {
        modified.clip(RectangleShape)
    }

    modified = modified.background(Color.White.copy(alpha = 0.9f))

    Canvas(
        modifier = modified
    )
    {
        scale(
            size.width / (CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value),
            pivot = Offset(0f, 0f)
        ) {
            CardState.card.value.cardElements.value.asReversed().forEach { cardElement ->
                val (maxAvailableWidth, maxAvailableHeight) = getAvailableSpace(cardElement.transformations)

                when (cardElement) {
                    is RichTextElement -> {
                        val text = cardElement.text.richTextState.annotatedString
                        val style = TextStyle.Default

                        var elementWidth = cardElement.transformations.width.get()

                        if (elementWidth == 0f) elementWidth = maxAvailableWidth

                        // Find line break spots for wrapping
                        val lineBreaks = mutableListOf<Int>()

                        var currentStartIndex = 0
                        while (currentStartIndex < text.length) {
                            var lastIndex = text.length
                            var spacesOnly = true

                            while (textMeasurer.measure(
                                    text.subSequence(currentStartIndex, lastIndex),
                                    style
                                ).size.width > elementWidth
                            ) {
                                if (spacesOnly) {
                                    val lastSpaceIndex = text.subSequence(currentStartIndex, lastIndex)
                                        .lastIndexOf(" ") + currentStartIndex
                                    if (lastSpaceIndex > currentStartIndex) {
                                        lastIndex = lastSpaceIndex
                                    } else {
                                        lastIndex = text.length
                                        spacesOnly = false
                                    }
                                } else {
                                    if (lastIndex > currentStartIndex + 1) lastIndex--
                                    else break
                                }
                            }

                            lineBreaks.add(lastIndex)
                            currentStartIndex = lastIndex
                        }

                        // Apply the found line breaks
                        val wrappedText = buildAnnotatedString {
                            var startIndex = 0
                            for (breakIndex in lineBreaks) {
                                if (text.subSequence(startIndex, breakIndex).text.startsWith(" ")) startIndex++

                                append(text.subSequence(startIndex, breakIndex))
                                startIndex = breakIndex
                            }
                            if (startIndex < text.length) {
                                if (text.subSequence(startIndex, text.length).text.startsWith(" ")) startIndex++

                                append(text.subSequence(startIndex, text.length))
                            }
                        }

                        var elementHeight = cardElement.transformations.height.get()
                        if (elementHeight == 0f) elementHeight = textMeasurer.measure(wrappedText, style).size.height.toFloat()

                        if (cardElement.transformations.width.get() == 0f) elementWidth = textMeasurer.measure(wrappedText, style).size.width.toFloat()

                        val offset = getOffset(
                            cardElement.transformations.anchor.value,
                            cardElement.transformations.offsetX.get(),
                            elementWidth,
                            cardElement.transformations.offsetY.get(),
                            elementHeight
                        )

                        drawText(
                            textMeasurer = textMeasurer,
                            text = wrappedText,
                            style = style,
                            topLeft = offset,
                            size = Size(elementWidth, elementHeight)
                        )
                    }

                    is ImageElement -> {
                        if (cardElement.imageBitmap.value != null) {
                            val imageWidth = cardElement.imageBitmap.value!!.width.toFloat()
                            val imageHeight = cardElement.imageBitmap.value!!.height.toFloat()

                            var elementWidth = cardElement.transformations.width.get()
                            var elementHeight = cardElement.transformations.height.get()

                            if (elementWidth == 0f) {
                                if (elementHeight == 0f) {
                                    val widthRatio = maxAvailableWidth / imageWidth
                                    val heightRatio = maxAvailableHeight / imageHeight

                                    if (widthRatio > heightRatio) {
                                        elementWidth = imageWidth * heightRatio
                                        elementHeight = imageHeight * heightRatio
                                    } else {
                                        elementWidth = imageWidth * widthRatio
                                        elementHeight = imageHeight * widthRatio
                                    }
                                } else {
                                    elementWidth = imageWidth * (elementHeight / imageHeight)
                                }
                            } else if (elementHeight == 0f) {
                                elementHeight = imageHeight * (elementWidth / imageWidth)
                            }

                            val offset = getOffset(
                                cardElement.transformations.anchor.value,
                                cardElement.transformations.offsetX.get(),
                                elementWidth,
                                cardElement.transformations.offsetY.get(),
                                elementHeight
                            )

                            scale(
                                scaleX = (elementWidth / imageWidth),
                                scaleY = (elementHeight / imageHeight),
                                pivot = offset
                            ) {
                                drawImage(image = cardElement.imageBitmap.value!!, topLeft = offset)
                            }
                        }
                    }

                    else -> {}
                }
            }

            // Bleed border
            if (CardState.card.value.bleedColor.value > 0) {
                val bleedThickness: Float = CardState.card.value.dpi.value / 12.7f
                val color = Color(CardState.card.value.bleedColor.value)

                drawRect(
                    color = color,
                    topLeft = Offset.Zero,
                    size = Size(
                        width = CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value,
                        height = bleedThickness
                    )
                )
                drawRect(
                    color = color,
                    topLeft = Offset.Zero,
                    size = Size(
                        width = bleedThickness,
                        height = CardState.card.value.dpi.value * CardState.card.value.resolutionVert.value
                    )
                )
                drawRect(
                    color = color,
                    topLeft = Offset(
                        0f,
                        CardState.card.value.dpi.value * CardState.card.value.resolutionVert.value - bleedThickness
                    ),
                    size = Size(
                        width = CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value,
                        height = bleedThickness
                    )
                )
                drawRect(
                    color = color,
                    topLeft = Offset(
                        CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value - bleedThickness,
                        0f
                    ),
                    size = Size(
                        width = bleedThickness,
                        height = CardState.card.value.dpi.value * CardState.card.value.resolutionVert.value
                    )
                )
            }
        }
    }
}

fun getAvailableSpace(transformations: CardElementTransformations): Pair<Float, Float> {
    val cardWidth = CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value
    val cardHeight = CardState.card.value.dpi.value * CardState.card.value.resolutionVert.value

    return when (transformations.anchor.value) {
        Anchor.TOP_LEFT -> {
            Pair(cardWidth - transformations.offsetX.get(), cardHeight + transformations.offsetY.get())
        }

        Anchor.TOP_RIGHT -> {
            Pair(transformations.offsetX.get(), cardHeight + transformations.offsetY.get())
        }

        Anchor.BOTTOM_LEFT -> {
            Pair(cardWidth - transformations.offsetX.get(), -transformations.offsetY.get())
        }

        Anchor.BOTTOM_RIGHT -> {
            Pair(transformations.offsetX.get(), -transformations.offsetY.get())
        }

        Anchor.CENTER -> {
            Pair(
                if (transformations.offsetX.get() * 2 > cardWidth) ((cardWidth - transformations.offsetX.get()) * 2) else (transformations.offsetX.get() * 2),
                if (-transformations.offsetY.get() * 2 > cardHeight) ((cardHeight + transformations.offsetY.get()) * 2) else (-transformations.offsetY.get() * 2)
            )
        }
    }
}

fun getOffset(anchor: Anchor, offsetX: Float, width: Float, offsetY: Float, height: Float): Offset {
    val (anchorOffsetX, anchorOffsetY) = when (anchor) {
        Anchor.TOP_LEFT -> {
            Pair(0f, 0f)
        }

        Anchor.TOP_RIGHT -> {
            Pair(-width, 0f)
        }

        Anchor.BOTTOM_LEFT -> {
            Pair(0f, -height)
        }

        Anchor.BOTTOM_RIGHT -> {
            Pair(-width, -height)
        }

        Anchor.CENTER -> {
            Pair(-width / 2, -height / 2)
        }
    }

    return Offset(
        x = offsetX + anchorOffsetX,
        y = -offsetY + anchorOffsetY
    )
}