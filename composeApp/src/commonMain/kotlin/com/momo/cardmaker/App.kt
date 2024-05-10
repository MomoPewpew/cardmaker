package com.momo.cardmaker

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.PlatformContext
import com.momo.cardmaker.components.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import kotlin.math.max

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

var showBorder = mutableStateOf(true)
var imageUtils: ImageUtils? = null

val context = PlatformContext.INSTANCE
val imageLoader = ImageLoader(context)

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    val textMeasurer = rememberTextMeasurer()

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
                                CardState.card.value.addElement(RichTextElement())
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
                                CardState.card.value.addElement(ImageElement())
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
                        Button(
                            onClick = {
                                ClickState.toggleRenaming()
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (ClickState.state.value == ClickState.States.RENAMING) Color.Gray else Color(
                                    0xFF013220
                                )
                            ),
                        ) {
                            Text("Rename")
                        }

                        // Pin parameter
                        Button(
                            onClick = {
                                ClickState.togglePinning()
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (ClickState.state.value == ClickState.States.PINNING) Color.Gray else Color(
                                    0xFF013220
                                )
                            ),
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
                            Box(
                                modifier = Modifier
                                    .padding(start = 21.dp, top = 8.dp)
                            ) {
                                Row(modifier = Modifier) {
                                    Icon(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .padding(horizontal = 8.dp),
                                        imageVector = if (pinnedFolded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = if (pinnedFolded) "Folded" else "Expanded"
                                    )
                                    Text(
                                        text = "Pinned",
                                        modifier = Modifier
                                            .clickable { pinnedFolded = !pinnedFolded },
                                        style = MaterialTheme.typography.h3
                                    )
                                }
                                Box(modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        pinnedFolded = !pinnedFolded
                                    }
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
                                Spacer(modifier = Modifier.height(8.dp))
                                if (!pinnedFolded) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CardState.card.value.cardElements.value.forEach { cardElement ->
                                            cardElement.buildPinnedElements()
                                        }
                                    }
                                }
                            }

                            // Advanced
                            Box(
                                modifier = Modifier
                                    .padding(start = 21.dp, top = 8.dp)
                            ) {
                                Row(modifier = Modifier) {
                                    Icon(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .padding(horizontal = 8.dp),
                                        imageVector = if (advancedFolded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = if (advancedFolded) "Folded" else "Expanded"
                                    )
                                    Text(
                                        text = "Advanced",
                                        modifier = Modifier
                                            .clickable { advancedFolded = !advancedFolded },
                                        style = MaterialTheme.typography.h3
                                    )
                                }
                                Box(modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        advancedFolded = !advancedFolded
                                    }
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
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
                            )
                            {
                                Spacer(modifier = Modifier.height(8.dp))
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
                    // Aspect ratio fields
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .weight(0.1f, fill = false)
                            .padding(bottom = 8.dp)
                            .background(Color.White.copy(alpha = 0.9f))
                    ) {
                        // Width
                        Column(modifier = Modifier.weight(1f)) {
                            Row {
                                Icon(
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(horizontal = 8.dp),
                                    imageVector = Icons.Filled.Code,
                                    contentDescription = "Width inches"
                                )
                                TextField(
                                    modifier = Modifier,
                                    maxLines = 1,
                                    value = CardState.card.value.resolutionHoriz.value.toString(),
                                    onValueChange = { newText ->
                                        val floatValue = newText.toFloatOrNull()
                                        if (floatValue != null) {
                                            CardState.card.value.resolutionHoriz.value = max(floatValue, 0.1f)
                                        }
                                    },
                                    textStyle = TextStyle(textAlign = TextAlign.Center),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                        // Height
                        Column(modifier = Modifier.weight(1f)) {
                            Row {
                                Icon(
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(horizontal = 8.dp),
                                    imageVector = Icons.Filled.UnfoldMore,
                                    contentDescription = "DPI"
                                )
                                TextField(
                                    modifier = Modifier,
                                    maxLines = 1,
                                    value = CardState.card.value.resolutionVert.value.toString(),
                                    onValueChange = { newText ->
                                        val floatValue = newText.toFloatOrNull()
                                        if (floatValue != null) {
                                            CardState.card.value.resolutionVert.value = max(floatValue, 0.1f)
                                        }
                                    },
                                    textStyle = TextStyle(textAlign = TextAlign.Center),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                        // DPI
                        Column(modifier = Modifier.weight(1f)) {
                            Row {
                                Text(
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(horizontal = 8.dp),
                                    text = "DPI",
                                    style = MaterialTheme.typography.h5
                                )
                                TextField(
                                    modifier = Modifier,
                                    maxLines = 1,
                                    value = CardState.card.value.dpi.value.toString(),
                                    onValueChange = { newText ->
                                        val intValue = newText.toIntOrNull()
                                        if (intValue != null) {
                                            CardState.card.value.dpi.value = max(intValue, 1)
                                        }
                                    },
                                    textStyle = TextStyle(textAlign = TextAlign.Center),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                    // Card preview
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .weight(weight = 0.9f, fill = false)
                    ) {
                        Column {
                            CardPreview(textMeasurer = textMeasurer)
                        }
                    }
                    // Download buttons
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .weight(0.1f)
                    ) {
                        // Export
                        Button(
                            onClick = {
                                val bitmap = CardState.card.value.drawToBitmap(textMeasurer)
                                val viewModel = ViewModel()

                                viewModel.triggerSaveImage(bitmap, "card.png")
                            },
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(horizontal = 16.dp)
                        ) {
                            Text("Export")
                        }
                        // Toggle Border
                        Button(
                            onClick = {
                                showBorder.value = !showBorder.value
                            },
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(horizontal = 16.dp)
                        ) {
                            Text("Preview Corners")
                        }
                        // Bleed
                        Button(
                            onClick = {
                                CardState.card.value.bleedColor.let {
                                    if (it.value == 0.toLong()) {
                                        it.value = 0xFF000000
                                        ColorPickerState.pick(it)
                                    } else {
                                        it.value = 0
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(horizontal = 16.dp)
                        ) {
                            Text("Bleed")
                        }
                    }
                }
            }

            // Popup window
            Popup()

            // Rename window
            Rename()

            // Delete Confirm window
            DeleteConfirm()

            // Color picker
            ColorPickerWindow()
        }
    }
}