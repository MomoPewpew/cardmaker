package com.momo.cardmaker

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.*

/** This class holds all the stacking data of a card element. */
data class CardElementStacking(
    val cardElement: CardElement,
    var horizontalIterations: IntParameter = IntParameter(
        defaultName = "Horizontal Iterations",
        defaultExpression = "1",
        cardElement = cardElement
    ),
    var horizontalInterval: FloatParameter = FloatParameter(
        defaultName = "Horizontal Interval",
        defaultExpression = "0.0",
        cardElement = cardElement
    ),
    var verticalIterations: IntParameter = IntParameter(
        defaultName = "Vertical Iterations",
        defaultExpression = "1",
        cardElement = cardElement
    ),
    var verticalInterval: FloatParameter = FloatParameter(
        defaultName = "Vertical Interval",
        defaultExpression = "0.0",
        cardElement = cardElement
    )
) {
    /**
     * Serialize this transformation set to a Json object.
     * @return The serialized Json object.
     * */
    fun toJson(): JsonObject {
        return buildJsonObject {
            put("horizontalIterations", horizontalIterations.toJson())
            put("horizontalInterval", horizontalInterval.toJson())
            put("verticalIterations", verticalIterations.toJson())
            put("verticalInterval", verticalInterval.toJson())
        }
    }

    /** Build the default stacking segment. */
    @Composable
    fun buildElements() {
        Row(modifier = Modifier) {
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                horizontalIterations.buildElements(mutableStateOf("Horizontal"), isPinnedElements = false)
            }
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                horizontalInterval.buildElements(mutableStateOf("Interval"), isPinnedElements = false)
            }
        }
        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                verticalIterations.buildElements(mutableStateOf("Vertical"), isPinnedElements = false)
            }
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                verticalInterval.buildElements(mutableStateOf("Interval"), isPinnedElements = false)
            }
        }
    }

    /** Build the composables for all pinned stacking parameters. */
    @Composable
    fun buildPinnedElements() {
        horizontalIterations.let {
            if (it.isPinned.value) {
                it.buildElements(label = it.name, isPinnedElements = true)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        horizontalInterval.let {
            if (it.isPinned.value) {
                it.buildElements(label = it.name, isPinnedElements = true)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        verticalIterations.let {
            if (it.isPinned.value) {
                it.buildElements(label = it.name, isPinnedElements = true)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        verticalInterval.let {
            if (it.isPinned.value) {
                it.buildElements(label = it.name, isPinnedElements = true)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    companion object {
        /**
         * Create a new transformation set from a serialized Json object.
         * @param json The object that holds the serialized transformation values.
         * @param cardElement The card element that will hold the new transformation values.
         * @return A new CardElementTransformations object made from the serialized Json object.
         * */
        fun fromJson(json: JsonObject, cardElement: CardElement): CardElementStacking {
            val stacking = CardElementStacking(cardElement)

            val offsetXObject = json["horizontalIterations"]?.jsonObject
            if (offsetXObject != null) stacking.horizontalIterations =
                Parameter.fromJson(offsetXObject, cardElement) as IntParameter

            val offsetYObject = json["horizontalInterval"]?.jsonObject
            if (offsetYObject != null) stacking.horizontalInterval =
                Parameter.fromJson(offsetYObject, cardElement) as FloatParameter

            val widthObject = json["verticalIterations"]?.jsonObject
            if (widthObject != null) stacking.verticalIterations =
                Parameter.fromJson(widthObject, cardElement) as IntParameter

            val heightObject = json["verticalInterval"]?.jsonObject
            if (heightObject != null) stacking.verticalInterval =
                Parameter.fromJson(heightObject, cardElement) as FloatParameter

            return stacking
        }
    }
}