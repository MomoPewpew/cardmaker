package com.momo.cardmaker

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
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

data class Card(
    val cardElements: MutableState<MutableList<CardElement>> = mutableStateOf(mutableListOf()),
    val resolutionHoriz: MutableState<Float> = mutableStateOf(2.5f),
    val resolutionVert: MutableState<Float> = mutableStateOf(3.5f),
    val dpi: MutableState<Int> = mutableStateOf(300)
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
                            cardElement.transformations.offsetY.get().toFloat()
                        )

                        var anchorOffsetX = 0f
                        var anchorOffsetY = 0f

                        when (cardElement.text.anchor.value) {
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
                                if ((cardElement.transformations as TextElementTransformations).width.get()
                                        .toFloat() > 0f
                                ) (cardElement.transformations).width.get().toFloat() else size.width,
                                if ((cardElement.transformations).height.get()
                                        .toFloat() > 0f
                                ) (cardElement.transformations).height.get().toFloat() else size.height
                            )
                        )
                    }

                    is ImageElement -> {
                        // TODO
                    }

                    else -> {}
                }
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