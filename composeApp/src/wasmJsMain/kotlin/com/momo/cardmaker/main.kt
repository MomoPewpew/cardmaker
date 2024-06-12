package com.momo.cardmaker

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.momo.cardmaker.components.ImportMode
import io.ktor.util.*

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    imageUtils = ImageUtilsWasmJs

    val data = getUrlParam("data")

    if (data != null) {
        val decodedData = data.decodeBase64String()
        CardState.import(ImportMode.REGULAR, decodedData)
    }

    CanvasBasedWindow(canvasElementId = "ComposeTarget") { App() }
}

external fun getUrlParam(param: String): String?