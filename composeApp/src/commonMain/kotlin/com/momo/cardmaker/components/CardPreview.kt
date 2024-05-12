package com.momo.cardmaker.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import com.momo.cardmaker.*

object ElementState {
    val selectedElement: MutableState<CardElement?> = mutableStateOf(null)

    fun toggleSelect(cardElement: CardElement) {
        if (selectedElement.value == null || selectedElement.value!! != cardElement) selectedElement.value = cardElement
        else selectedElement.value = null
    }
}

/** Build composables for previewing cards. */
@Composable
fun CardPreview(textMeasurer: TextMeasurer) {
    Canvas(
        modifier = Modifier
            .aspectRatio(CardState.card.value.resolutionHoriz.value / CardState.card.value.resolutionVert.value)
            .run {
                if (showBorder.value) {
                    clip(
                        RoundedCornerShape(
                            topStartPercent = 4,
                            topEndPercent = 4,
                            bottomStartPercent = 4,
                            bottomEndPercent = 4
                        )
                    )
                } else {
                    clip(RectangleShape)
                }
            }
            .background(Color.White.copy(alpha = 0.9f))
            .pointerInput(Unit) {
                var moveX = false
                var moveY = false
                var resizeX = 0
                var resizeY = 0

                detectDragGestures(onDragStart = { offset ->
                    val selectedElement = ElementState.selectedElement.value
                    if (selectedElement != null) {
                        val scaleFactor =
                            CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value / size.width

                        val topLeft = getOffset(
                            selectedElement.transformations.anchor.value,
                            selectedElement.transformations.offsetX.get(),
                            selectedElement.realWidth,
                            selectedElement.transformations.offsetY.get(),
                            selectedElement.realHeight
                        )

                        val widthPercent = ((scaleFactor * offset.x) - topLeft.x) / selectedElement.realWidth
                        val heightPercent = ((scaleFactor * offset.y) - topLeft.y) / selectedElement.realHeight

                        moveX = false
                        moveY = false
                        resizeX = 0
                        resizeY = 0

                        if (widthPercent in 0f..1f && heightPercent in 0f..1f) {
                            if (widthPercent in 0.2f..0.8f && heightPercent in 0.2f..0.8f) {
                                moveX = true
                                moveY = true
                            } else {
                                if (widthPercent < 0.2f) {
                                    if (selectedElement.transformations.anchor.value == Anchor.TOP_LEFT || selectedElement.transformations.anchor.value == Anchor.BOTTOM_LEFT) {
                                        moveX = true
                                    }
                                    resizeX = -1
                                    if (selectedElement.transformations.width.get() == 0f) selectedElement.transformations.width.expression.value = selectedElement.realWidth.toString()
                                } else if (widthPercent > 0.8f) {
                                    if (selectedElement.transformations.anchor.value == Anchor.TOP_RIGHT || selectedElement.transformations.anchor.value == Anchor.BOTTOM_RIGHT) {
                                        moveX = true
                                    }
                                    resizeX = 1
                                    if (selectedElement.transformations.width.get() == 0f) selectedElement.transformations.width.expression.value = selectedElement.realWidth.toString()
                                }

                                if (heightPercent < 0.2f) {
                                    if (selectedElement.transformations.anchor.value == Anchor.TOP_LEFT || selectedElement.transformations.anchor.value == Anchor.TOP_RIGHT) {
                                        moveY = true
                                    }
                                    resizeY = 1
                                    if (selectedElement.transformations.height.get() == 0f) selectedElement.transformations.height.expression.value = selectedElement.realHeight.toString()
                                } else if (heightPercent > 0.8f) {
                                    if (selectedElement.transformations.anchor.value == Anchor.BOTTOM_LEFT || selectedElement.transformations.anchor.value == Anchor.BOTTOM_RIGHT) {
                                        moveY = true
                                    }
                                    resizeY = -1
                                    if (selectedElement.transformations.height.get() == 0f) selectedElement.transformations.height.expression.value = selectedElement.realHeight.toString()
                                }

                                if (selectedElement.transformations.anchor.value == Anchor.CENTER) {
                                    resizeX *= 2
                                    resizeY *= 2
                                }
                            }
                        }
                    }

                }, onDrag = { change, dragAmount ->
                    if (ElementState.selectedElement.value == null) return@detectDragGestures
                    change.consume()

                    val scaleFactor =
                        CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value / size.width


                    if (moveX) ElementState.selectedElement.value!!.transformations.offsetX.addToConstant(scaleFactor * dragAmount.x)
                    if (moveY) ElementState.selectedElement.value!!.transformations.offsetY.addToConstant(scaleFactor * -dragAmount.y)

                    if (resizeX != 0) ElementState.selectedElement.value!!.transformations.width.addToConstant(
                        scaleFactor * dragAmount.x * resizeX
                    )
                    if (resizeY != 0) ElementState.selectedElement.value!!.transformations.height.addToConstant(
                        scaleFactor * -dragAmount.y * resizeY
                    )
                })
            }
    )
    {
        scale(
            size.width / (CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value),
            pivot = Offset(0f, 0f)
        ) {
            CardState.card.value.cardElements.value.asReversed().forEach { cardElement ->
                val (maxAvailableWidth, maxAvailableHeight) = getAvailableSpace(cardElement.transformations)
                var elementWidth = 0.0f
                var elementHeight = 0.0f

                when (cardElement) {
                    is RichTextElement -> {
                        val text = cardElement.text.richTextState.annotatedString
                        val style = TextStyle.Default

                        elementWidth = cardElement.transformations.width.get()

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

                        elementHeight = cardElement.transformations.height.get()
                        if (elementHeight == 0f) elementHeight =
                            textMeasurer.measure(wrappedText, style).size.height.toFloat()

                        if (cardElement.transformations.width.get() == 0f) elementWidth =
                            textMeasurer.measure(wrappedText, style).size.width.toFloat()

                        val topLeft = getOffset(
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
                            topLeft = topLeft,
                            size = Size(elementWidth, elementHeight)
                        )
                    }

                    is ImageElement -> {
                        if (cardElement.imageBitmap.value != null) {
                            val imageWidth = cardElement.imageBitmap.value!!.width.toFloat()
                            val imageHeight = cardElement.imageBitmap.value!!.height.toFloat()

                            elementWidth = cardElement.transformations.width.get()
                            elementHeight = cardElement.transformations.height.get()

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

                            val topLeft = getOffset(
                                cardElement.transformations.anchor.value,
                                cardElement.transformations.offsetX.get(),
                                elementWidth,
                                cardElement.transformations.offsetY.get(),
                                elementHeight
                            )

                            scale(
                                scaleX = (elementWidth / imageWidth),
                                scaleY = (elementHeight / imageHeight),
                                pivot = topLeft
                            ) {
                                drawImage(image = cardElement.imageBitmap.value!!, topLeft = topLeft)
                            }
                        }
                    }

                    else -> {}
                }

                cardElement.realWidth = elementWidth
                cardElement.realHeight = elementHeight
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

            // Selection outline
            val selectedElement = ElementState.selectedElement.value
            if (selectedElement != null) {
                val topLeft = getOffset(
                    selectedElement.transformations.anchor.value,
                    selectedElement.transformations.offsetX.get(),
                    selectedElement.realWidth,
                    selectedElement.transformations.offsetY.get(),
                    selectedElement.realHeight
                )

                drawRect(
                    color = Color.Green,
                    topLeft = Offset(x = topLeft.x + (selectedElement.realWidth * 0.2f), topLeft.y),
                    size = Size(selectedElement.realWidth * 0.6f, selectedElement.realHeight),
                    style = Stroke(width = 1f)
                )

                drawRect(
                    color = Color.Green,
                    topLeft = Offset(x = topLeft.x, topLeft.y + (selectedElement.realHeight * 0.2f)),
                    size = Size(selectedElement.realWidth, selectedElement.realHeight * 0.6f),
                    style = Stroke(width = 1f)
                )

                drawRect(
                    color = Color.Green,
                    topLeft = topLeft,
                    size = Size(selectedElement.realWidth, selectedElement.realHeight),
                    style = Stroke(width = 1f)
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