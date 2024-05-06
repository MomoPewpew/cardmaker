package com.momo.cardmaker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.momo.cardmaker.CardState
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

    Box(modifier = modified)
}