import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.momo.cardmaker.App
import com.momo.cardmaker.imageUtils

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "cardmaker",
    ) {
        imageUtils = ImageUtilsDesktop
        App()
    }
}