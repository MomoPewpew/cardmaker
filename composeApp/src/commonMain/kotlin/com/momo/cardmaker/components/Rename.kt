package com.momo.cardmaker.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.momo.cardmaker.CardElement
import com.momo.cardmaker.components.RenameState.cardElement
import com.momo.cardmaker.components.RenameState.isOpen
import com.momo.cardmaker.components.RenameState.name
import com.momo.cardmaker.components.RenameState.newName

object RenameState {
    var name = mutableStateOf("")
    var cardElement: MutableState<CardElement>? = null
    var newName = mutableStateOf(name.value)
    var isOpen = mutableStateOf(false)

    fun rename(name: MutableState<String>) {
        this.cardElement = null
        this.name = name
        newName = mutableStateOf(this.name.value)
        isOpen.value = true
    }

    fun rename(cardElement: CardElement) {
        this.cardElement = mutableStateOf(cardElement)
        this.name = this.cardElement?.value?.name ?: mutableStateOf("")
        newName = mutableStateOf(this.name.value)
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
                    text = "Rename ${name.value}",
                    style = MaterialTheme.typography.h5
                )
            },
            text = {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    maxLines = 1,
                    value = newName.value,
                    onValueChange = {
                        newName.value = it
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.value.isEmpty()) {
                        PopupState.popup("Name Error", "A name cannot be empty.")
                    } else if (cardElement != null) {
                        cardElement!!.value.rename(newName.value)
                    } else {
                        name.value = newName.value
                    }
                    isOpen.value = false
                }) {
                    Text("OK")
                }
            }
        )
    }
}