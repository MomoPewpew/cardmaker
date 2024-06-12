package com.momo.cardmaker.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.momo.cardmaker.*
import com.momo.cardmaker.components.ImportExportState.importMode
import com.momo.cardmaker.components.ImportExportState.showWindow
import com.momo.cardmaker.components.ImportExportState.textFieldValue
import com.momo.cardmaker.components.ImportExportState.url
import io.ktor.util.*

enum class ImportMode {
    NONE,
    REGULAR,
    PINNED_ONLY
}

/** The state holder that is used to show the import/export composable. */
object ImportExportState {
    val showWindow = mutableStateOf(false)
    val textFieldValue = mutableStateOf("")
    var url = ""
    val importMode = mutableStateOf(ImportMode.NONE)

    fun show() {
        showWindow.value = true
        textFieldValue.value = CardState.card.value.toJson().toString()
        url = "https://momopewpew.github.io/cardmaker-Site/?data=${textFieldValue.value.encodeBase64()}"
        importMode.value = ImportMode.NONE
    }
}

/** The composable dialog that holds the import and export features. */
@Composable
fun ImportExport(textMeasurer: TextMeasurer) {
    if (showWindow.value) {
        Dialog(
            onDismissRequest = { showWindow.value = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(4.dp),
            ) {
                Column {
                    Button(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                            .fillMaxWidth(),
                        onClick = {
                            val bitmap = CardState.card.value.drawToBitmap(textMeasurer)
                            val imageSaveManager = ImageSaveManager()

                            imageSaveManager.triggerSaveImage(bitmap, "card.png")
                        }
                    ) {
                        Text("Export PNG")
                    }

                    Button(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        onClick = {
                            importMode.value = ImportMode.REGULAR
                            textFieldValue.value = ""
                        }
                    ) {
                        Text("Import Json")
                    }

                    Button(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        onClick = {
                            importMode.value = ImportMode.PINNED_ONLY
                            textFieldValue.value = ""
                        }
                    ) {
                        Text("Import Json (pinned parameters only)")
                    }

                    TextField(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .height(56.dp),
                        value = textFieldValue.value,
                        maxLines = 1,
                        onValueChange = { newValue ->
                            if (importMode.value != ImportMode.NONE) {
                                textFieldValue.value = newValue
                            }
                        }
                    )

                    TextField(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .height(56.dp),
                        value = url,
                        maxLines = 1,
                        onValueChange = {}
                    )

                    Button(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            .fillMaxWidth(),
                        onClick = {
                            CardState.import(importMode = importMode.value, textFieldValue.value)
                            showWindow.value = false
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}