package com.momo.cardmaker.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import com.momo.cardmaker.components.RenameState.isOpen

object RenameState {
    var name = mutableStateOf("")
    var isOpen = mutableStateOf(false)
    var oldName = ""

    fun rename(name: MutableState<String>) {
        this.name = name
        oldName = name.value
        isOpen.value = true
    }
}

@Composable
fun Rename() {
    if (isOpen.value) {
        AlertDialog(
            onDismissRequest = { isOpen.value = false },
            title = { Text("Rename ${RenameState.oldName}") },
            text = {
                TextField(
                    value = RenameState.name.value,
                    onValueChange = {
                        RenameState.name.value = it
                    }
                )
            },
            confirmButton = {
                Button(onClick = {
                    isOpen.value = false
                }) {
                    Text("OK")
                }
            }
        )
    }
}