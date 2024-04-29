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

/** A card element can be subclassed into all the elements that are added to cards, such as text or images. */
abstract class CardElement {
    var name: String = ""
    private val transformations: CardElementTransformations = CardElementTransformations()
    var folded = false

    /** Build the expandable segment, and fills it with the elements that are specific to this element type. */
    @Composable
    fun buildElements(modifier: Modifier) {
        var foldedRemember by remember { mutableStateOf(folded) }
        Row(modifier = Modifier) {
            Text(
                text = if (folded) "▲ $name" else "▼ $name",
                modifier = Modifier
                    .padding(top = 16.dp, start = 32.dp)
                    .clickable {
                        folded = !folded
                        foldedRemember = folded
                    },
                style = MaterialTheme.typography.h4
            )
        }
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
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
            if (!foldedRemember) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    buildSpecificElements(modifier = Modifier)
                    buildTransformationElements(modifier = Modifier)
                }
            }
        }
    }

    /** Build specific composables to this card element type. */
    abstract @Composable
    fun buildSpecificElements(modifier: Modifier)

    /** Build the transformation segment. */
    @Composable
    fun buildTransformationElements(modifier: Modifier) {

    }

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

/** This class holds all the transformation data of a card element. */
data class CardElementTransformations(
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var offsetX: Int = 0,
    var offsetY: Int = 0
)

/** Text element. Composes a rich text editor. */
data class TextElement(
    var text: String = ""
) : CardElement() {
    init {
        name = "Text Element"
    }

    @Composable
    override fun buildSpecificElements(modifier: Modifier) {
        Row(modifier = Modifier) {
            Text(
                text = "Text",
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp, start = 32.dp),
                style = MaterialTheme.typography.h5
            )
        }
        Row(modifier = Modifier) {
            val state = rememberRichTextState()
            RichTextEditor(
                modifier = Modifier.fillMaxWidth(),
                state = state,
            )
        }
    }
}

/** Image element. Composes a text box to input URLs. */
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

