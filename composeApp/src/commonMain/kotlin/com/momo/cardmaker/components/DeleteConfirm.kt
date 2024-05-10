package com.momo.cardmaker.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import com.momo.cardmaker.CardElement
import com.momo.cardmaker.CardState
import com.momo.cardmaker.components.DeleteState.cardElement

object DeleteState {
    var cardElement: MutableState<CardElement?> = mutableStateOf(null)

    fun confirmDelete(cardElement: CardElement) {
        this.cardElement.value = cardElement
    }
}

@Composable
fun DeleteConfirm() {
    if (cardElement.value != null) {
        AlertDialog(
            onDismissRequest = { cardElement.value = null },
            title = {
                Text(
                    text = "Delete ${cardElement.value!!.name.value}",
                    style = MaterialTheme.typography.h5
                )
            },
            text = {
                Text(
                    text = "Are you sure that you want to delete the element '${cardElement.value!!.name.value}'",
                )
            },
            confirmButton = {
                Button(onClick = {
                    cardElement.value.let { CardState.card.value.removeElement(cardElement.value!!) }
                    cardElement.value = null
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { cardElement.value = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}