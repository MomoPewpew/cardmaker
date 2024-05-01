package com.momo.cardmaker

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** A card element can be subclassed into all the elements that are added to cards, such as text or images. */
abstract class CardElement {
    var name: String = ""
    private val transformations: CardElementTransformations = CardElementTransformations()
    private var folded = false

    /** Build the expandable segment, and fills it with the elements that are specific to this element type. */
    @Composable
    fun buildElements() {
        var foldedRemember by remember { mutableStateOf(folded) }
        Row(modifier = Modifier) {
            Text(
                text = if (folded) "▲ $name" else "▼ $name",
                modifier = Modifier
                    .padding(top = 8.dp, start = 32.dp)
                    .clickable {
                        folded = !folded
                        foldedRemember = folded
                    },
                style = MaterialTheme.typography.h4
            )
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
        transformations.offsetX.let {
            if (it.isPinned) {
                it.buildElements(modifier = Modifier, label = it.name.value)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        transformations.offsetY.let {
            if (it.isPinned) {
                it.buildElements(modifier = Modifier, label = it.name.value)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        transformations.scaleX.let {
            if (it.isPinned) {
                it.buildElements(modifier = Modifier, label = it.name.value)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        transformations.scaleY.let {
            if (it.isPinned) {
                it.buildElements(modifier = Modifier, label = it.name.value)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    /** Build the transformation segment. */
    @Composable
    fun buildTransformationElements() {
        Row(modifier = Modifier) {
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                transformations.offsetX.buildElements(modifier = Modifier, "Offset X")
            }
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                transformations.scaleX.buildElements(modifier = Modifier, "Scale X")
            }
        }
        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                transformations.offsetY.buildElements(modifier = Modifier, "Offset Y")
            }
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                transformations.scaleY.buildElements(modifier = Modifier, "Scale Y")
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
)

/** Textbox element to add text to the card. */
data class TextElement(
    var text: TextParameter = TextParameter(defaultName = "Text", expression = "")
) : CardElement() {
    init {
        name = "Text Element"
    }

    @Composable
    override fun buildSpecificElements(modifier: Modifier) {
        Row(
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            text.buildElements(modifier = Modifier, "")
        }
    }

    @Composable
    override fun buildPinnedElements(modifier: Modifier) {
        text.let {
            if (it.isPinned) {
                it.buildElements(modifier = Modifier, label = it.name.value)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        super.buildPinnedElements(modifier)
    }
}

/** Image element to add images to the card. */
data class ImageElement(
    var url: TextParameter = TextParameter(defaultName = "Url", expression = "")
) : CardElement() {
    init {
        name = "Image Element"
    }

    @Composable
    override fun buildSpecificElements(modifier: Modifier) {
        TODO("Not yet implemented")
    }

    @Composable
    override fun buildPinnedElements(modifier: Modifier) {
        url.let {
            if (it.isPinned) {
                it.buildElements(modifier = Modifier, label = it.name.value)
            }
        }
        super.buildPinnedElements(modifier)
    }
}

