package com.momo.cardmaker

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.skia.Bitmap
import kotlin.math.max

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
        val cardElements = cardElements.value
        cardElements.add(element)

        CardState.card.value = CardState.card.value.copy(cardElements = mutableStateOf(cardElements))
    }

    fun removeElement(element: CardElement) {
        val cardElements = cardElements.value
        cardElements.remove(element)

        CardState.card.value = CardState.card.value.copy(cardElements = mutableStateOf(cardElements))
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
            CardState.card.value.cardElements.value.forEach { cardElement ->
                when (cardElement) {
                    is TextElement -> {
                        val text = cardElement.text.richTextState.annotatedString
                        val style = TextStyle.Default

                        val textWidth = textMeasurer.measure(text, style).size.width
                        val textHeight = textMeasurer.measure(text, style).size.height

                        val topLeft = Offset(
                            cardElement.transformations.offsetX.get().toFloat(),
                            -cardElement.transformations.offsetY.get().toFloat()
                        )

                        var anchorOffsetX = 0f
                        var anchorOffsetY = 0f

                        when (cardElement.transformations.anchor.value) {
                            Anchor.TOP_RIGHT -> {
                                anchorOffsetX -= textWidth
                            }

                            Anchor.BOTTOM_LEFT -> {
                                anchorOffsetY -= textHeight
                            }

                            Anchor.BOTTOM_RIGHT -> {
                                anchorOffsetX -= textWidth
                                anchorOffsetY -= textHeight
                            }

                            Anchor.CENTER -> {
                                anchorOffsetX -= textWidth / 2
                                anchorOffsetY -= textHeight / 2
                            }

                            else -> {
                                anchorOffsetX = 0f
                                anchorOffsetY = 0f
                            }
                        }

                        val finalTopLeft = Offset(
                            x = topLeft.x + anchorOffsetX,
                            y = topLeft.y + anchorOffsetY
                        )

                        drawText(
                            textMeasurer = textMeasurer,
                            text = text,
                            style = style,
                            topLeft = finalTopLeft,
                            size = Size(
                                if (cardElement.transformations.width.get() > 0) (cardElement.transformations).width.get()
                                    .toFloat() else max(
                                    (CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value) - cardElement.transformations.offsetX.get(),
                                    0f
                                ),
                                if ((cardElement.transformations).height.get() > 0) (cardElement.transformations).height.get()
                                    .toFloat() else max(
                                    (CardState.card.value.dpi.value * CardState.card.value.resolutionVert.value) - cardElement.transformations.offsetY.get(),
                                    0f
                                )
                            )
                        )
                    }

                    is ImageElement -> {
                        // TODO
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