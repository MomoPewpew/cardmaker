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
import com.momo.cardmaker.components.DeleteState
import com.momo.cardmaker.components.ElementState
import com.momo.cardmaker.components.RenameState
import com.momo.cardmaker.components.RichTextStyleButton
import kotlinx.serialization.json.*

/**
 * A card element can be subclassed into all the elements that are added to cards, such as text or images.
 * @param defaultName The starting name of this Card Element. If a duplicate exists, an index will be added to it.
 * @param card The Card that will hold this CardElement.
 * */
abstract class CardElement(
    defaultName: String,
    val card: Card = CardState.card.value
) {
    var transformations = CardElementTransformations(cardElement = this)
    var stacking: CardElementStacking? = null

    var realWidth = 0.0f
    var realHeight = 0.0f

    val name = mutableStateOf("")
    private var folded = mutableStateOf(false)

    init {
        rename(defaultName)
    }

    /**
     * Serialize this Card Element to a Json object.
     * @return The serialized Json object.
     * */
    open fun toJson(): JsonObject {
        return buildJsonObject {
            when (this@CardElement) {
                is RichTextElement -> put("type", "richText")
                is ImageElement -> put("type", "image")
                else -> {}
            }
            put("name", name.value)
            put("transformations", transformations.toJson())
            stacking?.toJson()?.let { put("stacking", it) }
        }
    }

    /**
     * Updates this elements name. If the name is already in use, add an index to it.
     * @param newName The suggested new name. If there are duplicates within this Card, an index will be added to the name.
     * */
    fun rename(newName: String) {
        if (name.value == newName) return

        var modifiedName = newName
        var nameInUse = true
        var index = 1

        while (nameInUse) {
            nameInUse = false
            for (cardElement in card.cardElements.value) {
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

    /**
     * Build the expandable segment, and fills it with the elements that are specific to this element type.
     * */
    @Composable
    fun buildElements() {
        var hasStacking by remember { mutableStateOf(stacking != null) }
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
            ) {
                Box {
                    Row {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(top = 8.dp),
                            imageVector = if (folded.value) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (folded.value) "Folded" else "Expanded"
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
                                folded.value = !folded.value
                            }
                        }
                    )
                }
            }

            // Element buttons
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
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
                                            cardElement = this@CardElement
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
                        IconButton(modifier = Modifier,
                            enabled = !hasStacking,
                            onClick = {
                                hasStacking = true
                                stacking = CardElementStacking(cardElement = this@CardElement)
                            }) {
                            Icon(
                                imageVector = Icons.Outlined.ViewColumn,
                                contentDescription = "Add Stacking"
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
                        IconButton(modifier = Modifier,
                            onClick = {
                                this@CardElement.card.duplicateElement(this@CardElement)
                            }) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Duplicate"
                            )
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
            if (!folded.value) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    buildSpecificElements()
                    transformations.buildElements()
                    if (hasStacking) {
                        stacking?.let {
                            Row {
                                Text(
                                    modifier = Modifier.padding(start = 48.dp, bottom = 8.dp),
                                    text = "Stacking",
                                    style = MaterialTheme.typography.h4
                                )

                                Column(modifier = Modifier.fillMaxWidth().padding(end = 16.dp)) {
                                    IconButton(
                                        modifier = Modifier
                                            .align(Alignment.End),
                                        onClick = {
                                            stacking = null
                                            hasStacking = false
                                        },
                                    ) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete Stacking")
                                    }
                                }
                            }

                            it.buildElements()
                        }
                    }
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
        stacking?.buildPinnedElements()
    }

    /** Get the value of one of this card's properties by name. Used in expression replacement.
     * @param name The name of the property that we are getting.
     * @return The Float value of the named property, or null.
     * */
    fun getPropertyValueByName(name: String): Float? {
        return when (name) {
            "offsetX" -> transformations.offsetX.get()
            "offsetY" -> transformations.offsetY.get()
            "width" -> realWidth
            "height" -> realHeight
            "horizontalStackingIterations" -> {
                stacking?.horizontalIterations?.get()?.toFloat() ?: 1f
            }
            "horizontalStackingInterval" -> {
                stacking?.horizontalInterval?.get() ?: 0f
            }
            "verticalStackingIterations" -> {
                stacking?.verticalIterations?.get()?.toFloat() ?: 1f
            }
            "verticalStackingInterval" -> {
                stacking?.verticalInterval?.get() ?: 0f
            }
            else -> null
        }
    }

    /** Read from a json objects the properties that are specific to this type. */
    abstract fun fromJsonSpecific(json: JsonObject)

    companion object {
        /**
         * Create a new CardElement from a serialized Json object.
         * @param json The serialized Json object.
         * @param card The card that will hold this CardElement.
         * @return The new Card Element, or null.
         * */
        fun fromJson(json: JsonObject, card: Card): CardElement? {
            val type = json["type"]?.jsonPrimitive?.content
            val name = json["name"]?.jsonPrimitive?.content ?: ""
            val cardElement = when (type) {
                "richText" -> RichTextElement(name, card)
                "image" -> ImageElement(name, card)
                else -> return null
            }

            val transformationsObject = json["transformations"]?.jsonObject

            if (transformationsObject != null) cardElement.transformations =
                CardElementTransformations.fromJson(transformationsObject, cardElement)

            val stackingObject = json["stacking"]?.jsonObject

            if (stackingObject != null) cardElement.stacking =
                CardElementStacking.fromJson(stackingObject, cardElement)

            cardElement.fromJsonSpecific(json)

            cardElement.folded.value = true

            return cardElement
        }
    }
}

/** Textbox element to add rich text to the card.
 * @param defaultName The starting name of this Card Element. If a duplicate exists, an index will be added to it.
 * @param card The Card that will hold this CardElement.
 * */
class RichTextElement(
    defaultName: String = "Text Element",
    card: Card = CardState.card.value
) : CardElement(defaultName, card) {
    var text = RichTextParameter(defaultName = "Text", defaultExpression = "", this)

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
        if (textObject != null) text = Parameter.fromJson(textObject, this) as RichTextParameter
    }

    @Composable
    override fun buildSpecificElements() {
        Row(
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            text.buildElements(mutableStateOf(""), isPinnedElements = false)
        }
    }

    @Composable
    override fun buildPinnedElements() {
        text.let {
            if (it.isPinned.value) {
                it.buildElements(label = it.name, isPinnedElements = true)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        super.buildPinnedElements()
    }
}

/** Image element to add images to the card.
 * @param defaultName The starting name of this Card Element. If a duplicate exists, an index will be added to it.
 * @param card The Card that will hold this CardElement.
 * */
class ImageElement(
    defaultName: String = "Image Element",
    card: Card = CardState.card.value
) : CardElement(defaultName, card) {
    var image = ImageParameter(defaultName = "Image URL", defaultExpression = "", this)
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
            image = Parameter.fromJson(imageObject, this) as ImageParameter
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
        image.buildElements(mutableStateOf("URL"), isPinnedElements = false)
        masks.value.forEach {
            it.buildElements(
                mutableStateOf("Mask URL"),
                isPinnedElements = false
            )
        }
    }

    @Composable
    override fun buildPinnedElements() {
        image.let {
            if (it.isPinned.value) {
                it.buildElements(label = it.name, isPinnedElements = true)
            }
        }
        masks.value.forEach {
            it.let {
                if (it.isPinned.value) {
                    it.buildElements(label = it.name, isPinnedElements = true)
                }
            }
        }
        super.buildPinnedElements()
    }
}

