sealed class CardElement {
    private val transformations: CardElementTransformations = CardElementTransformations()

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

data class TextElement(var text: String = "") : CardElement() {

}

data class ImageElement(var url: String = "") : CardElement() {

}

data class CardElementTransformations(
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var offsetX: Int = 0,
    var offsetY: Int = 0
)
