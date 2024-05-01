package com.momo.cardmaker.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*

object PopupState {
    var title = mutableStateOf("")
    var text = mutableStateOf("")

    fun popup(title: String, text: String) {
        this.title.value = title
        this.text.value = text
    }
}

@Composable
fun Popup() {
    if (PopupState.text.value.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { PopupState.text.value = "" },
            title = {
                Text(
                    PopupState.title.value,
                    style = MaterialTheme.typography.h5
                )
            },
            text = { Text(PopupState.text.value) },
            confirmButton = {
                Button(onClick = { PopupState.text.value = "" }) {
                    Text("OK")
                }
            }
        )
    }
}