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
import com.momo.cardmaker.CardState
import com.momo.cardmaker.ImageElement
import com.momo.cardmaker.TextElement
import com.momo.cardmaker.showBorder

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
                    val textElement = cardElement
                    val text = textElement.text.expression.value

                    val style = TextStyle.Default

                    // Draw the text at the specified position
                    drawText(
                        textMeasurer = textMeasurer,
                        text = text,
                        style = style,
                        topLeft = Offset(
                            x = textElement.transformations.offsetX.get().toFloat(),
                            y = textElement.transformations.offsetY.get().toFloat()
                        )
                    )
                }

                is ImageElement -> {
                    val imageElement = cardElement as ImageElement

                    // TODO
                }

                else -> {}
            }
        }
    }
}

fun sizeFromDimensions(size: Size, width: Int, height: Int): Size {
    return Size(
        size.width * (width / (CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value)),
        size.height * (width / (CardState.card.value.dpi.value * CardState.card.value.resolutionVert.value))
    )
}