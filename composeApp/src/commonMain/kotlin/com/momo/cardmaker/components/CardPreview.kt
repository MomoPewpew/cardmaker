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
import androidx.compose.ui.text.drawText
import com.momo.cardmaker.*
import kotlin.math.max

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
                when (cardElement) {
                    is RichTextElement -> {
                        val text = cardElement.text.richTextState.annotatedString
                        val style = TextStyle.Default

                        val width = textMeasurer.measure(text, style).size.width.toFloat()
                        val height = textMeasurer.measure(text, style).size.height.toFloat()

                        val offset = getOffset(
                            cardElement.transformations.anchor.value,
                            cardElement.transformations.offsetX.get(),
                            width,
                            cardElement.transformations.offsetY.get(),
                            height
                        )

                        drawText(
                            textMeasurer = textMeasurer,
                            text = text,
                            style = style,
                            topLeft = offset,
                            size = Size(
                                if (cardElement.transformations.width.get() > 0) (cardElement.transformations).width.get() else max(
                                    (CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value) - cardElement.transformations.offsetX.get(),
                                    0f
                                ),
                                if ((cardElement.transformations).height.get() > 0) (cardElement.transformations).height.get() else max(
                                    (CardState.card.value.dpi.value * CardState.card.value.resolutionVert.value) - cardElement.transformations.offsetY.get(),
                                    0f
                                )
                            )
                        )
                    }

                    is ImageElement -> {
                        if (cardElement.imageBitmap.value != null) {
                            val imageWidth = cardElement.imageBitmap.value!!.width.toFloat()
                            val imageHeight = cardElement.imageBitmap.value!!.height.toFloat()

                            val maxAvailableWidth =
                                (CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value) - cardElement.transformations.offsetX.get()
                            val maxAvailableHeight =
                                (CardState.card.value.dpi.value * CardState.card.value.resolutionVert.value) - cardElement.transformations.offsetY.get()

                            if (maxAvailableWidth > 0 && maxAvailableHeight > 0) {
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

fun getOffset(anchor: Anchor, offsetX: Float, width: Float, offsetY: Float, height: Float): Offset {
    val (anchorOffsetX, anchorOffsetY) = when (anchor) {
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

        else -> {
            Pair(0f, 0f)
        }
    }

    return Offset(
        x = offsetX + anchorOffsetX,
        y = -offsetY + anchorOffsetY
    )
}