package com.momo.cardmaker

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.momo.cardmaker.components.RenameState

/** A card element can be subclassed into all the elements that are added to cards, such as text or images. */
abstract class CardElement(
    defaultName: String,
) {
    var name = mutableStateOf(defaultName)
    private val transformations: CardElementTransformations = CardElementTransformations()
    private var folded = false

    /** Build the expandable segment, and fills it with the elements that are specific to this element type. */
    @Composable
    fun buildElements() {
        var foldedRemember by remember { mutableStateOf(folded) }
        val me by remember { mutableStateOf(this) }
        Row(modifier = Modifier) {
            // Clickable name text
            Column(
                modifier = Modifier
                    .weight(weight = 9f)
            ) {
                Row {
                    Text(
                        text = if (folded) "▲ ${name.value}" else "▼ ${name.value}",
                        modifier = Modifier
                            .padding(top = 8.dp, start = 32.dp)
                            .clickable {
                                if (ClickState.state.value == ClickState.States.RENAMING) {
                                    RenameState.rename(name)
                                    ClickState.off()
                                } else {
                                    folded = !folded
                                    foldedRemember = folded
                                }
                            },
                        style = MaterialTheme.typography.h4
                    )
                }
            }

            // Element buttons
            Column(
                modifier = Modifier
                    .weight(2f)
                    .align(Alignment.CenterVertically)
            ) {
                Row(
                    modifier = Modifier
                        .padding(end = 31.dp)
                        .align(Alignment.End)
                ) {
                    // Move up button
                    Column(
                        modifier = Modifier
                            .width(48.dp)
                    ) {
                        Button(modifier = Modifier
                            .fillMaxSize(),
                            onClick = {
                                CardState.card.value.moveElementUp(me)
                            }) {
                            Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = "Move Up")
                        }
                    }
                    // Move Down Button
                    Column(
                        modifier = Modifier
                            .width(48.dp)
                    ) {
                        Button(modifier = Modifier
                            .fillMaxSize(),
                            onClick = {
                                CardState.card.value.moveElementDown(me)
                            }) {
                            Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = "Move Down")
                        }
                    }
                    // Delete button
                    Column(
                        modifier = Modifier
                            .width(48.dp)
                    ) {
                        Button(modifier = Modifier
                            .fillMaxSize(),
                            onClick = {

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
                    buildSpecificElements(modifier = Modifier)
                    buildTransformationElements()
                }
            }
        }
    }

    /** Build specific composables to this card element type. */
    @Composable
    abstract fun buildSpecificElements(modifier: Modifier)

    /** Build the composables for all pinned Parameters associated with this CardElement. */
    @Composable
    open fun buildPinnedElements(modifier: Modifier) {
        transformations.buildPinnedElements()
    }

    /** Build the transformation segment. */
    @Composable
    fun buildTransformationElements() {
        Row(modifier = Modifier) {
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                transformations.offsetX.buildElements(modifier = Modifier, mutableStateOf("Offset X"))
            }
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                transformations.scaleX.buildElements(modifier = Modifier, mutableStateOf("Scale X"))
            }
        }
        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                transformations.offsetY.buildElements(modifier = Modifier, mutableStateOf("Offset Y"))
            }
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                transformations.scaleY.buildElements(modifier = Modifier, mutableStateOf("Scale Y"))
            }
        }
    }
}

/** This class holds all the transformation data of a card element. */
data class CardElementTransformations(
    val scaleX: DoubleParameter = DoubleParameter(defaultName = "Scale X", expression = "1.0"),
    val scaleY: DoubleParameter = DoubleParameter(defaultName = "Scale Y", expression = "1.0"),
    val offsetX: IntParameter = IntParameter(defaultName = "Offset X", expression = "0"),
    val offsetY: IntParameter = IntParameter(defaultName = "Offset Y", expression = "0")
) {
    /** Build the composables for all pinned transformations. */
    @Composable
    fun buildPinnedElements() {
        offsetX.let {
            if (it.isPinned) {
                it.buildElements(modifier = Modifier, label = it.name)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        offsetY.let {
            if (it.isPinned) {
                it.buildElements(modifier = Modifier, label = it.name)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        scaleX.let {
            if (it.isPinned) {
                it.buildElements(modifier = Modifier, label = it.name)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        scaleY.let {
            if (it.isPinned) {
                it.buildElements(modifier = Modifier, label = it.name)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/** Textbox element to add text to the card. */
class TextElement(
    defaultName: String = "Text Element"
) : CardElement(defaultName) {
    var text = RichTextParameter(defaultName = "Text", expression = "")

    @Composable
    override fun buildSpecificElements(modifier: Modifier) {
        Row(
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            text.buildElements(modifier = Modifier, mutableStateOf(""))
        }
    }

    @Composable
    override fun buildPinnedElements(modifier: Modifier) {
        text.let {
            if (it.isPinned) {
                it.buildElements(modifier = Modifier, label = it.name)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        super.buildPinnedElements(modifier)
    }
}

/** Image element to add images to the card. */
class ImageElement(
    defaultName: String = "Image Element"
) : CardElement(defaultName) {
    var url = RichTextParameter(defaultName = "Url", expression = "")

    @Composable
    override fun buildSpecificElements(modifier: Modifier) {

    }

    @Composable
    override fun buildPinnedElements(modifier: Modifier) {
        url.let {
            if (it.isPinned) {
                it.buildElements(modifier = Modifier, label = it.name)
            }
        }
        super.buildPinnedElements(modifier)
    }
}

