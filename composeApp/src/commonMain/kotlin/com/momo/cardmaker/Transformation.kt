package com.momo.cardmaker

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class Anchor {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER
}

/** This class holds all the transformation data of a card element. */
data class CardElementTransformations(
    val offsetX: IntParameter = IntParameter(defaultName = "Offset X", defaultExpression = "0"),
    val offsetY: IntParameter = IntParameter(defaultName = "Offset Y", defaultExpression = "0"),
    val width: IntParameter = IntParameter(defaultName = "Width", defaultExpression = "0"),
    val height: IntParameter = IntParameter(defaultName = "Height", defaultExpression = "0"),
    val anchor: MutableState<Anchor> = mutableStateOf(Anchor.TOP_LEFT)
) {
    /** Build the default transformation segment. */
    @Composable
    fun buildElements() {
        Row(modifier = Modifier) {
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                offsetX.buildElements(modifier = Modifier, mutableStateOf("Offset X"))
            }
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                width.buildElements(modifier = Modifier, mutableStateOf("Width"))
            }
        }
        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                offsetY.buildElements(modifier = Modifier, mutableStateOf("Offset Y"))
            }
            Column(
                modifier = Modifier.weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                height.buildElements(modifier = Modifier, mutableStateOf("Height"))
            }
        }
    }

    /** Build the composables for all pinned transformations. */
    @Composable
    fun buildPinnedElements() {
        offsetX.let {
            if (it.isPinned.value) {
                it.buildElements(modifier = Modifier, label = it.name)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        offsetY.let {
            if (it.isPinned.value) {
                it.buildElements(modifier = Modifier, label = it.name)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        width.let {
            if (it.isPinned.value) {
                it.buildElements(modifier = Modifier, label = it.name)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        height.let {
            if (it.isPinned.value) {
                it.buildElements(modifier = Modifier, label = it.name)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}