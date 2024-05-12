package com.momo.cardmaker

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.momo.cardmaker.components.ElementState
import com.momo.cardmaker.components.getAvailableSpace
import com.momo.cardmaker.components.getOffset
import org.jetbrains.skia.Bitmap
import kotlin.math.abs

data class Card(
    val cardElements: MutableState<MutableList<CardElement>> = mutableStateOf(mutableListOf()),
    val resolutionHoriz: MutableState<Float> = mutableStateOf(2.5f),
    val resolutionVert: MutableState<Float> = mutableStateOf(3.5f),
    val dpi: MutableState<Int> = mutableStateOf(300),
    val bleedColor: MutableState<Long> = mutableStateOf(0)
) {
    fun toCsv(): String {
        var csv = ""

        return csv
    }

    fun addElement(element: CardElement) {
        val elements = cardElements.value.toMutableList()
        elements.add(element)

        cardElements.value = elements
    }

    fun removeElement(element: CardElement) {
        val elements = cardElements.value.toMutableList()
        elements.remove(element)

        if (ElementState.selectedElement.value?.equals(element) == true) ElementState.selectedElement.value = null

        cardElements.value = elements
    }

    fun moveElementUp(element: CardElement) {
        val cardElements = cardElements.value
        val currentIndex = cardElements.indexOf(element)
        if (currentIndex > 0) {
            val temp = cardElements[currentIndex]
            cardElements[currentIndex] = cardElements[currentIndex - 1]
            cardElements[currentIndex - 1] = temp
        }

        CardState.card.value = CardState.card.value.copy(cardElements = mutableStateOf(cardElements))
    }

    fun moveElementDown(element: CardElement) {
        val cardElements = cardElements.value
        val currentIndex = cardElements.indexOf(element)
        if (currentIndex < cardElements.lastIndex) {
            val temp = cardElements[currentIndex]
            cardElements[currentIndex] = cardElements[currentIndex + 1]
            cardElements[currentIndex + 1] = temp
        }

        CardState.card.value = CardState.card.value.copy(cardElements = mutableStateOf(cardElements))
    }

    fun drawToBitmap(textMeasurer: TextMeasurer): Bitmap {
        val drawScope = CanvasDrawScope()
        val size = Size(
            dpi.value * resolutionHoriz.value,
            dpi.value * resolutionVert.value
        )
        val bitmap = drawScope.asBitmap(size) {
            CardState.card.value.cardElements.value.asReversed().forEach { cardElement ->
                val (maxAvailableWidth, maxAvailableHeight) = getAvailableSpace(cardElement.transformations)
                var elementWidth = 0.0f
                var elementHeight = 0.0f

                when (cardElement) {
                    is RichTextElement -> {
                        val text = cardElement.text.richTextState.annotatedString
                        val style = TextStyle.Default

                        elementWidth = abs(cardElement.transformations.width.get())

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

                        elementHeight = abs(cardElement.transformations.height.get())
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
                        if (cardElement.image.imageBitmap.value != null) {
                            val imageWidth = cardElement.image.imageBitmap.value!!.width.toFloat()
                            val imageHeight = cardElement.image.imageBitmap.value!!.height.toFloat()

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
                                drawImage(
                                    image = cardElement.image.imageBitmap.value!!,
                                    topLeft = topLeft
                                )
                                for (mask in cardElement.masks.value) {
                                    if (mask.imageBitmap.value == null) continue

                                    drawImage(
                                        image = mask.imageBitmap.value!!,
                                        topLeft = topLeft,
                                        colorFilter = ColorFilter.tint(color = Color(mask.color.value)),
                                        blendMode = BlendMode.Color
                                    )
                                }
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
                    size = Size(
                        CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value,
                        CardState.card.value.dpi.value * CardState.card.value.resolutionVert.value
                    ),
                    style = Stroke(width = bleedThickness * 2)
                )
            }
        }

        return bitmap.asSkiaBitmap()
    }

    companion object {
        fun fromCsv(csv: String): Card {
            val card = Card()

            return card
        }

        fun CanvasDrawScope.asBitmap(size: Size, onDraw: DrawScope.() -> Unit): ImageBitmap {
            val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
            draw(Density(1f), LayoutDirection.Ltr, Canvas(bitmap), size) { onDraw() }
            return bitmap
        }
    }
}