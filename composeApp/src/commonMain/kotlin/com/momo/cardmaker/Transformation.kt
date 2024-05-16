package com.momo.cardmaker

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.*

enum class Anchor {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER
}

/** This class holds all the transformation data of a card element. */
data class CardElementTransformations(
    val cardElement: CardElement,
    var offsetX: FloatParameter = FloatParameter(
        defaultName = "Offset X",
        defaultExpression = "0.0",
        cardElement = cardElement
    ),
    var offsetY: FloatParameter = FloatParameter(
        defaultName = "Offset Y",
        defaultExpression = "0.0",
        cardElement = cardElement
    ),
    var width: FloatParameter = FloatParameter(
        defaultName = "Width",
        defaultExpression = "0.0",
        cardElement = cardElement
    ),
    var height: FloatParameter = FloatParameter(
        defaultName = "Height",
        defaultExpression = "0.0",
        cardElement = cardElement
    ),
    var anchor: MutableState<Anchor> = mutableStateOf(Anchor.TOP_LEFT)
) {
    /** Serialize this object into a Json string. */
    fun toJson(): JsonObject {
        return buildJsonObject {
            put("offsetX", offsetX.toJson())
            put("offsetY", offsetY.toJson())
            put("width", width.toJson())
            put("height", height.toJson())
            put("anchor", anchor.value.name)
        }
    }

    /** Build the default transformation segment. */
    @Composable
    fun buildElements() {
        Row(modifier = Modifier) {
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                offsetX.buildElements(modifier = Modifier, mutableStateOf("Offset X"), isPinnedElements = false)
            }
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                width.buildElements(modifier = Modifier, mutableStateOf("Width"), isPinnedElements = false)
            }
        }
        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                offsetY.buildElements(modifier = Modifier, mutableStateOf("Offset Y"), isPinnedElements = false)
            }
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                height.buildElements(modifier = Modifier, mutableStateOf("Height"), isPinnedElements = false)
            }
        }
    }

    /** Build the composables for all pinned transformations. */
    @Composable
    fun buildPinnedElements() {
        offsetX.let {
            if (it.isPinned.value) {
                it.buildElements(modifier = Modifier, label = it.name, isPinnedElements = true)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        offsetY.let {
            if (it.isPinned.value) {
                it.buildElements(modifier = Modifier, label = it.name, isPinnedElements = true)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        width.let {
            if (it.isPinned.value) {
                it.buildElements(modifier = Modifier, label = it.name, isPinnedElements = true)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        height.let {
            if (it.isPinned.value) {
                it.buildElements(modifier = Modifier, label = it.name, isPinnedElements = true)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    companion object {
        /** Create a new object from a Json object. */
        fun fromJson(json: JsonObject, cardElement: CardElement): CardElementTransformations {
            val transformations = CardElementTransformations(cardElement)

            val offsetXObject = json["offsetX"]?.jsonObject
            if (offsetXObject != null) transformations.offsetX =
                Parameter.fromJson(offsetXObject, cardElement) as FloatParameter

            val offsetYObject = json["offsetY"]?.jsonObject
            if (offsetYObject != null) transformations.offsetY =
                Parameter.fromJson(offsetYObject, cardElement) as FloatParameter

            val widthObject = json["width"]?.jsonObject
            if (widthObject != null) transformations.width =
                Parameter.fromJson(widthObject, cardElement) as FloatParameter

            val heightObject = json["height"]?.jsonObject
            if (heightObject != null) transformations.height =
                Parameter.fromJson(heightObject, cardElement) as FloatParameter

            try {
                transformations.anchor.value = Anchor.valueOf(json["anchor"]?.jsonPrimitive.toString())
            } catch (_: IllegalArgumentException) {
            }

            return transformations
        }
    }
}