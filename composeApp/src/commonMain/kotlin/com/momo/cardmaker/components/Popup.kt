package com.momo.cardmaker.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*

object PopupState {
    var title = mutableStateOf("")
    var text = mutableStateOf("")

    fun popup(title_: String, text_: String) {
        title.value = title_
        text.value = text_
    }
}

@Composable
fun Popup() {
    if (!PopupState.text.value.isEmpty()) {
        AlertDialog(
            onDismissRequest = { PopupState.text.value = "" },
            title = { Text(PopupState.title.value) },
            text = { Text(PopupState.text.value) },
            confirmButton = {
                Button(onClick = { PopupState.text.value = "" }) {
                    Text("OK")
                }
            }
        )
    }
}