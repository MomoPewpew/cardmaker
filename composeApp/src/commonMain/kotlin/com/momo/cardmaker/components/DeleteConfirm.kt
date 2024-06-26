package com.momo.cardmaker.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.momo.cardmaker.CardElement
import com.momo.cardmaker.CardState
import com.momo.cardmaker.components.DeleteState.cardElement

/** The state holder that is used for element deletion confirmation. */
object DeleteState {
    var cardElement: MutableState<CardElement?> = mutableStateOf(null)

    fun confirmDelete(cardElement: CardElement) {
        this.cardElement.value = cardElement
    }
}

/** The composable dialog that's used to confirm element deletion. */
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