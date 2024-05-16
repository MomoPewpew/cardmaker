package com.momo.cardmaker.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.eygraber.compose.colorpicker.ColorPicker
import com.momo.cardmaker.components.ColorPickerState.color
import com.momo.cardmaker.components.ColorPickerState.colorString
import com.momo.cardmaker.components.ColorPickerState.newColor
import com.momo.cardmaker.components.ColorPickerState.showWindow

/** The state manager that's used for color picking. */
object ColorPickerState {
    var color: MutableState<Long>? = null
    val newColor: MutableState<Long> = mutableStateOf(0)
    val showWindow = mutableStateOf(false)
    val colorString = mutableStateOf("")

    fun pick(color: MutableState<Long>) {
        this.color = color
        newColor.value = color.value
        showWindow.value = true
        colorString.value = color.value.toString(16)
    }
}

/** The composable dialog that's used to pick colors. */
@Composable
fun ColorPickerWindow() {
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
                    ColorPicker(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(all = 25.dp),
                        onColorSelected = {
                            val red = it.red.times(255).toInt()
                            val green = it.green.times(255).toInt()
                            val blue = it.blue.times(255).toInt()
                            val alpha = it.alpha.times(255).toInt()

                            val longValue = (alpha shl 24) or (red shl 16) or (green shl 8) or blue

                            newColor.value = longValue.toLong()
                            colorString.value = it.value.toString(16).substring(0, 8)
                        }
                    )
                    Row(
                        modifier = Modifier
                            .height(80.dp)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        TextField(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .weight(3f, fill = false),
                            maxLines = 1,
                            value = colorString.value,
                            onValueChange = {
                                colorString.value = it
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )

                        Button(modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                            .weight(1f, fill = false)
                            .fillMaxHeight(),
                            onClick = {
                                try {
                                    var intValue = if (colorString.value.startsWith("0x")) {
                                        colorString.value.substring(2)
                                    } else {
                                        colorString.value
                                    }
                                    if (intValue.length == 6) intValue = "FF${colorString.value}"

                                    newColor.value = intValue.toLong(16)
                                } catch (_: NumberFormatException) {
                                }

                                color!!.value = newColor.value
                                showWindow.value = false
                            }
                        ) {
                            Text(text = "OK")
                        }

                        Button(modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                            .weight(1f)
                            .fillMaxHeight(),
                            onClick = {
                                showWindow.value = false
                            }
                        ) {
                            Text(text = "Cancel")
                        }
                    }
                }
            }
        }
    }
}