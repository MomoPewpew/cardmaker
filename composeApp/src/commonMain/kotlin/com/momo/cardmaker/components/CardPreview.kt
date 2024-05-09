package com.momo.cardmaker.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import com.momo.cardmaker.*
import kotlin.math.max

/** Build composables for previewing cards. */
@Composable
fun CardPreview(modifier: Modifier = Modifier, textMeasurer: TextMeasurer) {
    var modified = modifier

    modified =
        modified.aspectRatio(CardState.card.value.resolutionHoriz.value / CardState.card.value.resolutionVert.value)

    if (showBorder.value) {
        modified = modified.clip(
            RoundedCornerShape(
                topStartPercent = 4,
                topEndPercent = 4,
                bottomStartPercent = 4,
                bottomEndPercent = 4
            )
        )
    } else {
        modified = modified.clip(RectangleShape)
    }

    modified = modified.background(Color.White)

    if (showBorder.value) {
        modified = modified.border(
            width = 1.dp,
            color = Color.Black,
            shape = RoundedCornerShape(
                topStartPercent = 4,
                topEndPercent = 4,
                bottomStartPercent = 4,
                bottomEndPercent = 4
            )
        )
    }

    Canvas(
        modifier = modified
    )
    {
        scale(
            size.width / (CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value),
            pivot = Offset(0f, 0f)
        ) {
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
                                ) (cardElement.transformations).width.get()
                                    .toFloat() else max(size.width - cardElement.transformations.offsetX.get(), 0f),
                                if ((cardElement.transformations).height.get()
                                        .toFloat() > 0f
                                ) (cardElement.transformations).height.get()
                                    .toFloat() else max(size.height - cardElement.transformations.offsetY.get(), 0f)
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
    }
}