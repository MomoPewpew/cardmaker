package com.momo.cardmaker

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.momo.cardmaker.components.ImportMode
import io.ktor.util.*

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    imageUtils = ImageUtilsWasmJs
    getCurrentUrl()?.let { hostName = it }

    val data = getUrlParam("data")?.replace(" ", "+")

    if (data != null) {
        val decodedData = data.decodeBase64String()
        CardState.import(ImportMode.REGULAR, decodedData)
    }

    CanvasBasedWindow(canvasElementId = "ComposeTarget") { App() }
}

external fun getUrlParam(param: String): String?

external fun getCurrentUrl(): String?