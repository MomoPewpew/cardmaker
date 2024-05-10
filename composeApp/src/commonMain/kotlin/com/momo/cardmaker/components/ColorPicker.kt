package com.momo.cardmaker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eygraber.compose.colorpicker.ColorPicker
import com.momo.cardmaker.components.ColorPickerState.color
import com.momo.cardmaker.components.ColorPickerState.colorString
import com.momo.cardmaker.components.ColorPickerState.newColor
import com.momo.cardmaker.components.ColorPickerState.showWindow

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

@Composable
fun ColorPickerWindow() {
    if (showWindow.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { showWindow.value = false }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.weight(1f)) {}

                Row(
                    modifier = Modifier
                        .weight(2f)
                        .align(Alignment.CenterHorizontally)
                        .clip(
                            RoundedCornerShape(
                                topStart = 4.dp,
                                topEnd = 4.dp,
                                bottomStart = 4.dp,
                                bottomEnd = 4.dp
                            )
                        )
                        .background(color = Color.White)
                        .clickable(onClick = {}, enabled = false)
                        .border(
                            width = 1.dp,
                            Color.Black,
                            shape = RoundedCornerShape(
                                topStart = 4.dp,
                                topEnd = 4.dp,
                                bottomStart = 4.dp,
                                bottomEnd = 4.dp
                            )
                        )
                ) {
                    Column {
                        ColorPicker(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .weight(1f)
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
                                .padding(vertical = 8.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Column {
                                TextField(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp),
                                    maxLines = 1,
                                    value = colorString.value,
                                    onValueChange = {
                                        colorString.value = it
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                                )
                            }

                            Column {
                                Button(modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                                    .fillMaxHeight(),
                                    onClick = {
                                        try {
                                            val intValue = if (colorString.value.startsWith("0x")) {
                                                colorString.value.substring(2)
                                            } else {
                                                colorString.value
                                            }
                                            newColor.value = intValue.toLong(16)
                                        } catch (_: NumberFormatException) {
                                        }

                                        color!!.value = newColor.value
                                        showWindow.value = false
                                    }
                                ) {
                                    Text(text = "OK")
                                }
                            }

                            Column {
                                Button(modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
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

                Row(modifier = Modifier.weight(1f)) {}
            }
        }
    }
}