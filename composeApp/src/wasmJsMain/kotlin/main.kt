import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.momo.cardmaker.App
import com.momo.cardmaker.imageUtils

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    imageUtils = ImageUtilsWasmJs
    CanvasBasedWindow(canvasElementId = "ComposeTarget") { App() }
}