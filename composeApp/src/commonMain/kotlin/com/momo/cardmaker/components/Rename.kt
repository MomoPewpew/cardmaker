package com.momo.cardmaker.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.momo.cardmaker.components.RenameState.isOpen

object RenameState {
    var name = mutableStateOf("")
    var newName = mutableStateOf(name.value)
    var isOpen = mutableStateOf(false)

    fun rename(name: MutableState<String>) {
        this.name = name
        newName = mutableStateOf(RenameState.name.value)
        isOpen.value = true
    }
}

@Composable
fun Rename() {
    if (isOpen.value) {
        AlertDialog(
            onDismissRequest = { isOpen.value = false },
            title = {
                Text(
                    text = "Rename ${RenameState.name.value}",
                    style = MaterialTheme.typography.h5
                )
            },
            text = {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    maxLines = 1,
                    value = RenameState.newName.value,
                    onValueChange = {
                        RenameState.newName.value = it
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (RenameState.newName.value.isEmpty()) {
                        PopupState.popup("Name Error", "A name cannot be empty.")
                    } else {
                        RenameState.name.value = RenameState.newName.value
                    }
                    isOpen.value = false
                }) {
                    Text("OK")
                }
            }
        )
    }
}