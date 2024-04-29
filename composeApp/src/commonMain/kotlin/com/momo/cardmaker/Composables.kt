package com.momo.cardmaker

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

/** Build composables for previewing cards. */
@Composable
fun CardPreview(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            //.size(width = 750.dp, height = 1050.dp)
            .aspectRatio(2.5f / 3.5f)
            .clip(
                RoundedCornerShape(
                    topStartPercent = 10,
                    topEndPercent = 10,
                    bottomStartPercent = 10,
                    bottomEndPercent = 10
                )
            )
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(
                    topStartPercent = 10,
                    topEndPercent = 10,
                    bottomStartPercent = 10,
                    bottomEndPercent = 10
                )
            )
    )
}