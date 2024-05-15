package com.momo.cardmaker.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.momo.cardmaker.*
import com.momo.cardmaker.components.ImportExportState.importMode
import com.momo.cardmaker.components.ImportExportState.showWindow
import com.momo.cardmaker.components.ImportExportState.textFieldValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

enum class ImportMode {
    NONE,
    REGULAR,
    PINNED_ONLY
}

object ImportExportState {
    val showWindow = mutableStateOf(false)
    val textFieldValue = mutableStateOf("")
    val importMode = mutableStateOf(ImportMode.NONE)

    fun show() {
        showWindow.value = true
        textFieldValue.value = CardState.card.value.toJson().toString()
        importMode.value = ImportMode.NONE
    }
}

@Composable
fun ImportExport(textMeasurer: TextMeasurer) {
    if (showWindow.value) {
        Dialog(
            onDismissRequest = { showWindow.value = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(4.dp),
            ) {
                Column {
                    Button(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                            .fillMaxWidth(),
                        onClick = {
                            val bitmap = CardState.card.value.drawToBitmap(textMeasurer)
                            val viewModel = ViewModel()

                            viewModel.triggerSaveImage(bitmap, "card.png")
                        }
                    ) {
                        Text("Export PNG")
                    }

                    Button(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        onClick = {
                            importMode.value = ImportMode.REGULAR
                            textFieldValue.value = ""
                        }
                    ) {
                        Text("Import Json")
                    }

                    Button(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        onClick = {
                            importMode.value = ImportMode.PINNED_ONLY
                            textFieldValue.value = ""
                        }
                    ) {
                        Text("Import Json (pinned parameters only)")
                    }

                    TextField(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .height(56.dp),
                        value = textFieldValue.value,
                        maxLines = 1,
                        onValueChange = { newValue ->
                            if (importMode.value != ImportMode.NONE) {
                                textFieldValue.value = newValue
                            }
                        }
                    )

                    Button(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            .fillMaxWidth(),
                        onClick = {
                            if (importMode.value != ImportMode.NONE && textFieldValue.value.isNotEmpty()) {
                                val json = try {
                                    Json.parseToJsonElement(textFieldValue.value).jsonObject
                                } catch (_: Exception) {
                                    PopupState.popup("Json Parse Error", "Your json string was improperly formatted.")
                                    return@Button
                                }

                                when (importMode.value) {
                                    ImportMode.REGULAR -> {
                                        CardState.card.value = Card.fromJson(json)
                                    }

                                    ImportMode.PINNED_ONLY -> {
                                        val card = Card.fromJson(json)

                                        CardState.card.value.cardElements.value.forEach { cardElement ->
                                            val importElement =
                                                card.cardElements.value.find { it.name.value == cardElement.name.value && it::class == cardElement::class }

                                            if (importElement == null) return@forEach

                                            if (cardElement.transformations.offsetX.isSimilar(importElement.transformations.offsetX)) cardElement.transformations.offsetX =
                                                importElement.transformations.offsetX
                                            if (cardElement.transformations.offsetY.isSimilar(importElement.transformations.offsetY)) cardElement.transformations.offsetY =
                                                importElement.transformations.offsetY
                                            if (cardElement.transformations.width.isSimilar(importElement.transformations.width)) cardElement.transformations.width =
                                                importElement.transformations.width
                                            if (cardElement.transformations.height.isSimilar(importElement.transformations.height)) cardElement.transformations.height =
                                                importElement.transformations.height

                                            when (cardElement) {
                                                is RichTextElement -> {
                                                    importElement as RichTextElement
                                                    if (cardElement.text.isSimilar(importElement.text)) cardElement.text =
                                                        importElement.text
                                                }

                                                is ImageElement -> {
                                                    importElement as ImageElement
                                                    if (cardElement.image.isSimilar(importElement.image)) cardElement.image =
                                                        importElement.image
                                                    for (i in 0..<cardElement.masks.value.size) {
                                                        val importMask =
                                                            importElement.masks.value.getOrNull(i) ?: continue
                                                        if (cardElement.masks.value[i].isSimilar(importMask)) cardElement.masks.value[i] =
                                                            importMask
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    else -> {}
                                }
                            }

                            showWindow.value = false
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

private fun <T> Parameter<T>.isSimilar(parameter: Parameter<T>): Boolean {
    return (isPinned.value && parameter.isPinned.value && name.value == parameter.name.value)
}
