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
import androidx.compose.ui.unit.dp
import com.momo.cardmaker.components.*
import kotlinx.serialization.json.*

/** A card element can be subclassed into all the elements that are added to cards, such as text or images. */
abstract class CardElement(
    defaultName: String
) {
    var transformations = CardElementTransformations()

    var realWidth = 0.0f
    var realHeight = 0.0f

    val name = mutableStateOf("")
    private var folded = false

    init {
        rename(defaultName)
    }

    /** Serialize this object into a Json string. */
    open fun toJson(): JsonObject {
        return buildJsonObject {
            when (this@CardElement) {
                is RichTextElement -> put("type", "richText")
                is ImageElement -> put("type", "image")
                else -> {}
            }
            put("name", name.value)
            put("transformations", transformations.toJson())
        }
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
        Row(modifier = Modifier
            .padding(horizontal = 30.dp)
            .clickable { ElementState.toggleSelect(this@CardElement) }
            .background(
                if (ElementState.selectedElement.value?.equals(this@CardElement) == true) Color(0xFF013220).copy(
                    alpha = 0.1f
                ) else Color.Transparent
            )
        ) {
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
                                .padding(top = 8.dp),
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
                                RenameState.rename(this@CardElement)
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
                        .align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Element-specific buttons
                    if (this@CardElement is ImageElement) {
                        item {
                            IconButton(modifier = Modifier,
                                onClick = {
                                    val list = this@CardElement.masks.value.toMutableList()
                                    list.add(
                                        MaskParameter(
                                            defaultName = "Mask URL",
                                            defaultExpression = "",
                                            imageElement = this@CardElement
                                        )
                                    )

                                    this@CardElement.masks.value = list
                                }) {
                                Icon(
                                    imageVector = Icons.Outlined.Palette,
                                    contentDescription = "Add Mask"
                                )
                            }
                        }

                        item {
                            Box(
                                Modifier
                                    .height(24.dp)
                                    .width(1.dp)
                                    .background(Color(0xFF393B3D))
                            )
                        }
                    }

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
                                CardState.card.value.moveElementUp(this@CardElement)
                            }) {
                            Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = "Move Up")
                        }
                    }

                    item {
                        IconButton(modifier = Modifier
                            .fillMaxSize(),
                            onClick = {
                                CardState.card.value.moveElementDown(this@CardElement)
                            }) {
                            Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = "Move Down")
                        }
                    }

                    item {
                        IconButton(modifier = Modifier
                            .fillMaxSize(),
                            onClick = {
                                DeleteState.confirmDelete(this@CardElement)
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
    fun getPropertyValueByName(name: String): Float? {
        return when (name) {
            "offsetX" -> transformations.offsetX.get()
            "offsetY" -> transformations.offsetY.get()
            "width" -> realWidth
            "height" -> realHeight
            else -> null
        }
    }

    /** Read from a json objects the properties that are specific to this type. */
    abstract fun fromJsonSpecific(json: JsonObject)

    companion object {
        /** Create a new object from a Json object. */
        fun fromJson(json: JsonObject): CardElement? {
            val type = json["type"].toString().trim('\"')
            val name = json["name"].toString().trim('\"')
            val cardElement = when (type) {
                "richText" -> RichTextElement(name)
                "image" -> ImageElement(name)
                else -> null
            }

            val transformationsObject = json["transformations"]?.jsonObject

            if (transformationsObject != null) cardElement?.transformations =
                CardElementTransformations.fromJson(transformationsObject)

            cardElement?.fromJsonSpecific(json)

            return cardElement
        }
    }
}

/** Textbox element to add text to the card. */
class RichTextElement(
    defaultName: String = "Text Element"
) : CardElement(defaultName) {
    var text = RichTextParameter(defaultName = "Text", defaultExpression = "")

    override fun toJson(): JsonObject {
        return buildJsonObject {
            super.toJson().forEach {
                put(it.key, it.value)
            }
            put("text", text.toJson())
        }
    }

    override fun fromJsonSpecific(json: JsonObject) {
        val textObject = json["text"]?.jsonObject
        if (textObject != null) text = Parameter.fromJson(textObject) as RichTextParameter
    }

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
    var image = ImageParameter(defaultName = "Image URL", defaultExpression = "")
    var masks: MutableState<MutableList<MaskParameter>> = mutableStateOf(mutableListOf())

    override fun toJson(): JsonObject {
        return buildJsonObject {
            super.toJson().forEach {
                put(it.key, it.value)
            }
            put("image", image.toJson())
            putJsonArray("masks") {
                masks.value.forEach {
                    add(it.toJson())
                }
            }
        }
    }

    override fun fromJsonSpecific(json: JsonObject) {
        val imageObject = json["image"]?.jsonObject
        if (imageObject != null) {
            image = Parameter.fromJson(imageObject) as ImageParameter
            image.downloadImage()
        }

        val masksList: MutableList<MaskParameter> = mutableListOf()
        json["masks"]?.jsonArray?.forEach {
            val mask = Parameter.fromJson(it.jsonObject, this) as MaskParameter
            masksList.add(mask)
            mask.downloadImage()
        }
        masks.value = masksList
    }

    @Composable
    override fun buildSpecificElements() {
        image.buildElements(modifier = Modifier, mutableStateOf("URL"))
        for (mask in masks.value) mask.buildElements(modifier = Modifier, mutableStateOf("Mask URL"))
    }

    @Composable
    override fun buildPinnedElements() {
        image.let {
            if (it.isPinned.value) {
                it.buildElements(modifier = Modifier, label = it.name)
            }
        }
        for (mask in masks.value) {
            mask.let {
                if (it.isPinned.value) {
                    it.buildElements(modifier = Modifier, label = it.name)
                }
            }
        }
        super.buildPinnedElements()
    }
}

