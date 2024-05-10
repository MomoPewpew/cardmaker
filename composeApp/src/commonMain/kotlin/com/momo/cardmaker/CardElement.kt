package com.momo.cardmaker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.request.ImageRequest
import com.momo.cardmaker.components.DeleteState
import com.momo.cardmaker.components.RenameState
import com.momo.cardmaker.components.RichTextStyleButton

/** A card element can be subclassed into all the elements that are added to cards, such as text or images. */
abstract class CardElement(
    defaultName: String
) {
    val transformations = CardElementTransformations()
    val name = mutableStateOf("")
    private var folded = false

    init {
        rename(defaultName)
    }

    /** Updates this elements name. If the name is already in use, add an index to it. */
    fun rename(newName: String) {
        if (name.value == newName) return

        var modifiedName = newName
        var nameInUse = true
        var index = 1

        while (nameInUse) {
            nameInUse = false
            for (cardElement in CardState.card.value.cardElements.value) {
                if (cardElement.name.value == modifiedName) {
                    nameInUse = true
                    index++
                    modifiedName = "$newName $index"
                    break
                }
            }
        }

        name.value = modifiedName
    }

    /** Build the expandable segment, and fills it with the elements that are specific to this element type. */
    @Composable
    fun buildElements() {
        var foldedRemember by remember { mutableStateOf(folded) }
        val me = mutableStateOf(this)
        Row(modifier = Modifier) {
            // Clickable name text
            Column(
                modifier = Modifier
                    .weight(weight = 9f)
            ) {
                Box {
                    Row {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 30.dp, top = 8.dp),
                            imageVector = if (folded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (folded) "Folded" else "Expanded"
                        )
                        Text(
                            text = name.value,
                            modifier = Modifier
                                .padding(top = 8.dp, start = 8.dp),
                            style = MaterialTheme.typography.h4
                        )
                    }
                    Box(modifier = Modifier
                        .padding(start = 15.dp)
                        .matchParentSize()
                        .clickable {
                            if (ClickState.state.value == ClickState.States.RENAMING) {
                                RenameState.rename(me.value)
                                ClickState.off()
                            } else {
                                folded = !folded
                                foldedRemember = folded
                            }
                        }
                    )
                }
            }

            // Element buttons
            Column(
                modifier = Modifier
                    .weight(4f)
                    .align(Alignment.CenterVertically)
            ) {
                LazyRow(
                    modifier = Modifier
                        .padding(end = 31.dp)
                        .align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item {
                        RichTextStyleButton(
                            onClick = {
                                transformations.anchor.value = Anchor.TOP_LEFT
                            },
                            isSelected = transformations.anchor.value == Anchor.TOP_LEFT,
                            icon = Icons.Outlined.NorthWest,
                        )
                    }

                    item {
                        RichTextStyleButton(
                            onClick = {
                                transformations.anchor.value = Anchor.TOP_RIGHT
                            },
                            isSelected = transformations.anchor.value == Anchor.TOP_RIGHT,
                            icon = Icons.Outlined.NorthEast,
                        )
                    }

                    item {
                        RichTextStyleButton(
                            onClick = {
                                transformations.anchor.value = Anchor.BOTTOM_LEFT
                            },
                            isSelected = transformations.anchor.value == Anchor.BOTTOM_LEFT,
                            icon = Icons.Outlined.SouthWest,
                        )
                    }

                    item {
                        RichTextStyleButton(
                            onClick = {
                                transformations.anchor.value = Anchor.BOTTOM_RIGHT
                            },
                            isSelected = transformations.anchor.value == Anchor.BOTTOM_RIGHT,
                            icon = Icons.Outlined.SouthEast,
                        )
                    }

                    item {
                        RichTextStyleButton(
                            onClick = {
                                transformations.anchor.value = Anchor.CENTER
                            },
                            isSelected = transformations.anchor.value == Anchor.CENTER,
                            icon = Icons.Outlined.CenterFocusWeak,
                        )
                    }

                    item {
                        Box(
                            Modifier
                                .height(24.dp)
                                .width(1.dp)
                                .background(Color(0xFF393B3D))
                        )
                    }

                    item {
                        IconButton(modifier = Modifier
                            .fillMaxSize(),
                            onClick = {
                                CardState.card.value.moveElementUp(me.value)
                            }) {
                            Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = "Move Up")
                        }
                    }

                    item {
                        IconButton(modifier = Modifier
                            .fillMaxSize(),
                            onClick = {
                                CardState.card.value.moveElementDown(me.value)
                            }) {
                            Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = "Move Down")
                        }
                    }

                    item {
                        IconButton(modifier = Modifier
                            .fillMaxSize(),
                            onClick = {
                                DeleteState.confirmDelete(me.value)
                            }) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
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
            if (!foldedRemember) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    buildSpecificElements()
                    buildTransformationElements()
                }
            }
        }
    }

    /** Build specific composables to this card element type. */
    @Composable
    abstract fun buildSpecificElements()

    /** Build the composables for all pinned Parameters associated with this CardElement. */
    @Composable
    open fun buildPinnedElements() {
        transformations.buildPinnedElements()
    }

    /** Build the transformation segment. */
    @Composable
    fun buildTransformationElements() {
        transformations.buildElements()
    }

    /** Get the value of one of this card's properties by name. Used in expression replacement. */
    fun getPropertyValueByName(name: String): Double? {
        return when (name) {
            "offsetX" -> transformations.offsetX.get().toDouble()
            "offsetY" -> transformations.offsetY.get().toDouble()
            else -> null
        }
    }
}

/** Textbox element to add text to the card. */
class RichTextElement(
    defaultName: String = "Text Element"
) : CardElement(defaultName) {
    var text = RichTextParameter(defaultName = "Text", defaultExpression = "")

    @Composable
    override fun buildSpecificElements() {
        Row(
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            text.buildElements(modifier = Modifier, mutableStateOf(""))
        }
    }

    @Composable
    override fun buildPinnedElements() {
        text.let {
            if (it.isPinned.value) {
                it.buildElements(modifier = Modifier, label = it.name)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        super.buildPinnedElements()
    }
}

/** Image element to add images to the card. */
class ImageElement(
    defaultName: String = "Image Element"
) : CardElement(defaultName) {
    var uri = UriParameter(defaultName = "URL", defaultExpression = "", imageElement = this)
    var imageBitmap: MutableState<ImageBitmap?> = mutableStateOf(null)
    var uriChanged = true

    @OptIn(ExperimentalCoilApi::class)
    fun downloadImage() {
        if (!uriChanged || uri.get().isEmpty()) return
        uriChanged = false

        val request = ImageRequest.Builder(context)
            .data(uri.get())
            .target(
                onSuccess = { result ->
                    imageBitmap.value = result.toBitmap().asComposeImageBitmap()
                }
            ).build()

        imageLoader.enqueue(request)
    }

    @Composable
    override fun buildSpecificElements() {
        uri.buildElements(modifier = Modifier, mutableStateOf("URL"))
    }

    @Composable
    override fun buildPinnedElements() {
        uri.let {
            if (it.isPinned.value) {
                it.buildElements(modifier = Modifier, label = it.name)
            }
        }
        super.buildPinnedElements()
    }
}

