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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.momo.cardmaker.*

/** Build composables for previewing cards. */
@Composable
fun CardPreview(modifier: Modifier = Modifier) {
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

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modified
    )
    {
        CardState.card.value.cardElements.value.forEach { cardElement ->
            when (cardElement) {
                is TextElement -> {
                    val text = cardElement.text.richTextState.annotatedString

                    var anchorOffsetX = 0
                    var anchorOffsetY = 0

                    // TODO: Add proper anchor transformations for every anchor point
                    when (cardElement.text.anchor.value) {
                        Anchor.TOP_RIGHT -> {}
                        Anchor.BOTTOM_LEFT -> {}
                        Anchor.BOTTOM_RIGHT -> {}
                        Anchor.CENTER -> {}
                        else -> {}
                    }

                    drawText(
                        textMeasurer = textMeasurer,
                        text = text,
                        style = TextStyle.Default,
                        topLeft = offsetFromTransformations(
                            size,
                            cardElement.transformations.offsetX.get() + anchorOffsetX,
                            cardElement.transformations.offsetY.get() + anchorOffsetY
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

/** Convert pixel dimensions into a Compose Size object. */
fun sizeFromDimensions(size: Size, width: Int, height: Int): Size {
    return Size(
        size.width * (width / (CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value)),
        size.height * (height / (CardState.card.value.dpi.value * CardState.card.value.resolutionVert.value))
    )
}

/** Convert pixel dimensions into a Compose Offset object. */
fun offsetFromTransformations(size: Size, width: Int, height: Int): Offset {
    return Offset(
        size.width * (width / (CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value)),
        size.height * (height / (CardState.card.value.dpi.value * CardState.card.value.resolutionVert.value))
    )
}