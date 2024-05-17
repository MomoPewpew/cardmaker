package com.momo.cardmaker

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    imageUtils = ImageUtilsWasmJs
    CanvasBasedWindow(canvasElementId = "ComposeTarget") { App() }
}