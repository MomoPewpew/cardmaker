import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor

abstract class CardElement {
    var name: String = ""
    private val transformations: CardElementTransformations = CardElementTransformations()

    /** Build the expandable segment, and fills it with the elements that are specific to this element type. */
    @Composable
    fun buildElements(modifier: Modifier) {
        var folded by remember { mutableStateOf(false) }
        Row(modifier = Modifier) {
            Text(
                text = if (folded) "▲ $name" else "▼ $name",
                modifier = Modifier
                    .padding(top = 16.dp, start = 32.dp)
                    .clickable { folded = !folded },
                style = MaterialTheme.typography.h4
            )
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .defaultMinSize(minHeight = 5.dp)
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(
                        topStartPercent = 5,
                        topEndPercent = 5,
                        bottomStartPercent = 5,
                        bottomEndPercent = 5
                    )
                )
        ) {
            if (!folded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    buildSpecificElements(modifier)

                }
            }
        }
    }

    abstract @Composable
    fun buildSpecificElements(modifier: Modifier)

    fun setScaleX(value: Float) {
        transformations.scaleX = value
    }

    fun getScaleX(): Float {
        return transformations.scaleX
    }

    fun incrementScaleX() {
        transformations.scaleX += 0.05f
    }

    fun decrementScaleX() {
        transformations.scaleX -= 0.05f
    }

    fun setScaleY(value: Float) {
        transformations.scaleY = value
    }

    fun getScaleY(): Float {
        return transformations.scaleY
    }

    fun incrementScaleY() {
        transformations.scaleY += 0.05f
    }

    fun decrementScaleY() {
        transformations.scaleY -= 0.05f
    }

    fun setOffsetX(value: Int) {
        transformations.offsetX = value
    }

    fun incrementOffsetX() {
        transformations.offsetX++
    }

    fun decrementOffsetX() {
        transformations.offsetX--
    }

    fun setOffsetY(value: Int) {
        transformations.offsetY = value
    }

    fun incrementOffsetY() {
        transformations.offsetY++
    }

    fun decrementOffsetY() {
        transformations.offsetY--
    }
}

data class CardElementTransformations(
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var offsetX: Int = 0,
    var offsetY: Int = 0
)

data class TextElement(
    var text: String = ""
) : CardElement() {
    init {
        name = "Text Element"
    }

    @Composable
    override fun buildSpecificElements(modifier: Modifier) {
        Row(modifier = Modifier) {
            val state = rememberRichTextState()
            RichTextEditor(
                state = state,
            )
        }
    }
}

data class ImageElement(
    var url: String = ""
) : CardElement() {
    init {
        name = "Image Element"
    }

    @Composable
    override fun buildSpecificElements(modifier: Modifier) {
        TODO("Not yet implemented")
    }
}