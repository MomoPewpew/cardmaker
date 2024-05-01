package com.momo.cardmaker.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import com.momo.cardmaker.CardElement
import com.momo.cardmaker.CardState

object DeleteState {
    var cardElement: MutableState<CardElement?> = mutableStateOf(null)

    fun confirmDelete(cardElement: CardElement) {
        this.cardElement.value = cardElement
    }
}

@Composable
fun DeleteConfirm() {
    if (DeleteState.cardElement.value != null) {
        AlertDialog(
            onDismissRequest = { PopupState.text.value = "" },
            title = {
                Text(
                    text = "Delete ${(DeleteState.cardElement.value as CardElement).name.value}",
                    style = MaterialTheme.typography.h5
                )
            },
            text = {
                Text(
                    text = "Are you sure that you want to delete the element '${(DeleteState.cardElement.value as CardElement).name.value}'",
                )
            },
            confirmButton = {
                Button(onClick = {
                    DeleteState.cardElement.value?.let { CardState.card.value.removeElement(it) }
                    DeleteState.cardElement.value = null
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { DeleteState.cardElement.value = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}