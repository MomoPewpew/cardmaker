package com.momo.cardmaker

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.momo.cardmaker.components.CardPreview
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

var card = Card()

object PinningState {
    val state = mutableStateOf(false)

    fun togglePinning() {
        state.value = !state.value
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme {

    }

    Box(
        modifier = Modifier
    ) {
        // Background Image
        Image(
            painterResource(DrawableResource("background.jpg")),
            contentDescription = "backgroundImage",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Main UI
        Row(
            modifier = Modifier
                .padding(48.dp)
        ) {
            // Config column
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            topStartPercent = 2,
                            topEndPercent = 2,
                            bottomStartPercent = 2,
                            bottomEndPercent = 2
                        )
                    )
                    .background(Color.White.copy(alpha = 0.9f))
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(
                            topStartPercent = 2,
                            topEndPercent = 2,
                            bottomStartPercent = 2,
                            bottomEndPercent = 2
                        )
                    )
            ) {
                var advancedFolded by remember { mutableStateOf(false) }

                // Main button row
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 32.dp)
                        .height(48.dp)
                ) {
                    Button(
                        onClick = {
                            card.cardElements.add(TextElement())
                            advancedFolded =
                                true // Fold before unfolding to ensure a recomposition of the advanced segment
                            advancedFolded = false
                        },
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Add Text")
                    }
                    Button(
                        onClick = {
                            card.cardElements.add(ImageElement())
                            advancedFolded =
                                true // Fold before unfolding to ensure a recomposition of the  advanced segment
                            advancedFolded = false
                        },
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Add Image")
                    }
                    Button(
                        onClick = {
                            PinningState.togglePinning()
                        },
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.PushPin, contentDescription = "Decrease")
                    }
                }

                // Line
                Row(
                    modifier = Modifier
                        .padding(vertical = 0.dp, horizontal = 16.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth()) {
                        drawLine(
                            start = Offset(x = 0f, y = 0f),
                            end = Offset(x = size.width, y = 0f),
                            color = Color.Black.copy(alpha = 0.8f),
                            strokeWidth = 1f
                        )
                    }
                }

                // Pinned
                var pinnedFolded by remember { mutableStateOf(false) }
                Row(modifier = Modifier) {
                    Text(
                        text = if (pinnedFolded) "▲ Pinned" else "▼ Pinned",
                        modifier = Modifier
                            .padding(top = 16.dp, start = 32.dp)
                            .clickable { pinnedFolded = !pinnedFolded },
                        style = MaterialTheme.typography.h3
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .defaultMinSize(minHeight = 5.dp)
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            Color.Black.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(
                                topStart = 15.dp,
                                topEnd = 15.dp,
                                bottomStart = 15.dp,
                                bottomEnd = 15.dp
                            )
                        )
                ) {
                    if (!pinnedFolded && (PinningState.state.value || !PinningState.state.value)) { // This seemingly redundant check is made to force a recomposition after a new pin is made
                        Column(modifier = Modifier.fillMaxWidth()) {
                            for (cardElement in card.cardElements) {
                                cardElement.buildPinnedElements(modifier = Modifier)
                            }
                        }
                    }
                }

                // Advanced
                Row(modifier = Modifier) {
                    Text(
                        text = if (advancedFolded) "▲ Advanced" else "▼ Advanced",
                        modifier = Modifier
                            .padding(top = 16.dp, start = 32.dp)
                            .clickable { advancedFolded = !advancedFolded },
                        style = MaterialTheme.typography.h3
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .defaultMinSize(minHeight = 5.dp)
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            Color.Black.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(
                                topStart = 15.dp,
                                topEnd = 15.dp,
                                bottomStart = 15.dp,
                                bottomEnd = 15.dp
                            )
                        )
                ) {
                    if (!advancedFolded) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            for (cardElement in card.cardElements) {
                                cardElement.buildElements(
                                    modifier = Modifier
                                )
                            }
                        }
                    }
                }
            }

            // Card Preview Column
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .padding(start = 16.dp)
            ) {
                // Card preview
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .weight(weight = 0.9f, fill = false)
                ) {
                    CardPreview()
                }
                // Download buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .weight(0.1f)
                ) {
                    Button(
                        onClick = {
                            // Render image and download png
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text("Export as PNG")
                    }
                }
            }
        }
    }
}