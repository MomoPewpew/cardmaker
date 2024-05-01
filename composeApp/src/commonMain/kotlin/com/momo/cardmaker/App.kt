package com.momo.cardmaker

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import com.momo.cardmaker.components.Popup
import com.momo.cardmaker.components.Rename
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

object ClickState {
    enum class States {
        NONE,
        PINNING,
        RENAMING
    }

    val state = mutableStateOf(States.NONE)

    fun off() {
        state.value = States.NONE
    }

    fun togglePinning() {
        if (state.value == States.PINNING) {
            state.value = States.NONE
        } else {
            state.value = States.PINNING
        }
    }

    fun toggleRenaming() {
        if (state.value == States.RENAMING) {
            state.value = States.NONE
        } else {
            state.value = States.RENAMING
        }
    }
}

object CardState {
    var card = mutableStateOf(Card())
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme(
        colors = lightColors(
            primary = Color(0xFF013220)
        )
    ) {
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
                            .padding(vertical = 16.dp, horizontal = 32.dp)
                            .height(48.dp)
                    ) {
                        // Add text
                        Button(
                            onClick = {
                                CardState.card.value.cardElements.value.add(TextElement())
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

                        // Add image
                        Button(
                            onClick = {
                                CardState.card.value.cardElements.value.add(ImageElement())
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

                        // Vertical Line
                        Canvas(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp)
                        ) {
                            drawLine(
                                start = Offset(x = 0f, y = 0f),
                                end = Offset(x = 0f, y = size.height),
                                color = Color.Black.copy(alpha = 0.6f),
                                strokeWidth = 1f
                            )
                        }

                        // Rename
                        val renameColor =
                            if (ClickState.state.value == ClickState.States.RENAMING) Color.Gray else Color(0xFF013220)
                        Button(
                            onClick = {
                                ClickState.toggleRenaming()
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = renameColor)
                        ) {
                            Text("Rename")
                        }

                        // Pin parameter
                        val pinColor =
                            if (ClickState.state.value == ClickState.States.PINNING) Color.Gray else Color(0xFF013220)
                        Button(
                            onClick = {
                                ClickState.togglePinning()
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = pinColor)
                        ) {
                            Icon(imageVector = Icons.Outlined.PushPin, contentDescription = "Pin")
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

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column {
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
                                if (!pinnedFolded && (ClickState.state.value == ClickState.States.PINNING || ClickState.state.value != ClickState.States.PINNING)) { // This seemingly redundant check is made to force a recomposition after a new pin is made
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CardState.card.value.cardElements.value.forEach { cardElement ->
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
                                    .defaultMinSize(minHeight = 8.dp)
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
                                        CardState.card.value.cardElements.value.forEach { cardElement ->
                                            cardElement.buildElements()
                                        }
                                    }
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

            // Popup window
            Popup()

            // Rename window
            Rename()
        }
    }
}