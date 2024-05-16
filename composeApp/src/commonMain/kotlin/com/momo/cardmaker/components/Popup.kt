package com.momo.cardmaker.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import com.momo.cardmaker.components.PopupState.text
import com.momo.cardmaker.components.PopupState.title

/** The state holder that is used for showing alerts.. */
object PopupState {
    var title = mutableStateOf("")
    var text = mutableStateOf("")

    fun popup(title: String, text: String) {
        this.title.value = title
        this.text.value = text
    }
}

/** The composable dialog that's used to alert the user of errors. */
@Composable
fun Popup() {
    if (text.value.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { text.value = "" },
            title = {
                Text(
                    title.value,
                    style = MaterialTheme.typography.h5
                )
            },
            text = { Text(text.value) },
            confirmButton = {
                Button(onClick = { text.value = "" }) {
                    Text("OK")
                }
            }
        )
    }
}