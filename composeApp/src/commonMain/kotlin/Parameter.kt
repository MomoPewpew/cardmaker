import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

sealed class Parameter<T>(val name: String, val value: T) {
    abstract @Composable
    fun buildElements(modifier: Modifier)
}