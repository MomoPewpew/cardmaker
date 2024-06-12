package com.momo.cardmaker

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.momo.cardmaker.components.*
import kotlinx.serialization.json.*
import org.jetbrains.skia.Bitmap
import kotlin.math.abs

/** The state holder that holds the Card object. */
object CardState {
    var card = mutableStateOf(Card())

    fun import(importMode: ImportMode, jsonString: String) {
        if (importMode != ImportMode.NONE && jsonString.isNotEmpty()) {
            val json = try {
                Json.parseToJsonElement(jsonString).jsonObject
            } catch (_: Exception) {
                PopupState.popup("Json Parse Error", "Your json string was improperly formatted.")
                return
            }

            when (importMode) {
                ImportMode.REGULAR -> {
                    card.value = Card.fromJson(json)
                }

                ImportMode.PINNED_ONLY -> {
                    val card = Card.fromJson(json)

                    CardState.card.value.cardElements.value.forEach { cardElement ->
                        val importElement =
                            card.cardElements.value.find { it.name.value == cardElement.name.value && it::class == cardElement::class }

                        if (importElement == null) return@forEach

                        cardElement.transformations.offsetX.overrideIfSimilar(importElement.transformations.offsetX)
                        cardElement.transformations.offsetY.overrideIfSimilar(importElement.transformations.offsetY)
                        cardElement.transformations.width.overrideIfSimilar(importElement.transformations.width)
                        cardElement.transformations.height.overrideIfSimilar(importElement.transformations.height)

                        when (cardElement) {
                            is RichTextElement -> {
                                importElement as RichTextElement
                                cardElement.text.overrideIfSimilar(importElement.text)
                            }

                            is ImageElement -> {
                                importElement as ImageElement
                                cardElement.image.overrideIfSimilar(importElement.image)
                                for (i in 0..<cardElement.masks.value.size) {
                                    val importMask =
                                        importElement.masks.value.getOrNull(i) ?: continue

                                    if (cardElement.masks.value[i].overrideIfSimilar(importMask)) cardElement.masks.value[i].color.value =
                                        importMask.color.value
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

/**
 * A Card that represents the layout of the created image.
 * @param cardElements The list of all CardElements in this Card.
 * @param resolutionHoriz The horizontal resolution of this Card, in inches.
 * @param resolutionVert The vertical resolution of this Card, in inches.
 * @param dpi The Dots per Inch resolution of this Card.
 * @param bleedColor The Long value that represents this Cards bleed zone color.
 * */
data class Card(
    val cardElements: MutableState<MutableList<CardElement>> = mutableStateOf(mutableListOf()),
    val resolutionHoriz: MutableState<Float> = mutableStateOf(2.5f),
    val resolutionVert: MutableState<Float> = mutableStateOf(3.5f),
    val dpi: MutableState<Int> = mutableStateOf(300),
    val bleedColor: MutableState<Long> = mutableStateOf(0)
) {
    /**
     * Serialize this Card to a Json object.
     * @return The serialized Json object.
     * */
    fun toJson(): JsonObject {
        return buildJsonObject {
            put("dpi", dpi.value)
            put("resolutionHoriz", resolutionHoriz.value)
            put("resolutionVert", resolutionVert.value)
            put("bleedColor", bleedColor.value)
            putJsonArray("cardElements") {
                cardElements.value.forEach {
                    add(it.toJson())
                }
            }
        }

    }

    /**
     * Adds a new CardElement to this Card in a way that will trigger recomposition.
     * @param element The CardElement that should be added.
     */
    fun addElement(element: CardElement) {
        val elements = cardElements.value.toMutableList()
        elements.add(0, element)

        cardElements.value = elements
    }

    /**
     * Removes a CardElement from this Card in a way that will trigger recomposition. Also unselects this CardElement if it was selected.
     * @param element The CardElement that should be added.
     */
    fun removeElement(element: CardElement) {
        val elements = cardElements.value.toMutableList()
        elements.remove(element)

        if (ElementState.selectedElement.value?.equals(element) == true) ElementState.selectedElement.value = null

        cardElements.value = elements
    }

    /**
     * Duplicate an existing CardElement.
     * @param element The CardElement that should be duplicated.
     */
    fun duplicateElement(element: CardElement) {
        val elements = cardElements.value.toMutableList()

        val index = elements.indexOf(element)

        CardElement.fromJson(element.toJson(), this)?.let {
            elements.add(index, it)
            cardElements.value = elements
        }
    }

    /**
     * Reorganizes this Cards element list in a way that triggers recomposition.
     * @param element The element that should be moved up in the list.
     */
    fun moveElementUp(element: CardElement) {
        val elements = cardElements.value.toMutableList()
        val currentIndex = elements.indexOf(element)
        if (currentIndex > 0) {
            val temp = elements[currentIndex]
            elements[currentIndex] = elements[currentIndex - 1]
            elements[currentIndex - 1] = temp
        }

        cardElements.value = elements
    }

    /**
     * Reorganizes this Cards element list in a way that triggers recomposition.
     * @param element The element that should be moved down in the list.
     */
    fun moveElementDown(element: CardElement) {
        val elements = cardElements.value.toMutableList()
        val currentIndex = elements.indexOf(element)
        if (currentIndex < elements.lastIndex) {
            val temp = elements[currentIndex]
            elements[currentIndex] = elements[currentIndex + 1]
            elements[currentIndex + 1] = temp
        }

        cardElements.value = elements
    }

    /**
     * Runs the same render code that is used for Previewing, but return it as a skia Bitmap.
     * @param textMeasurer A TextMeasurer. This is passed in as a parameter, because TextMeasurers must be created in a composable context and this is not that.
     * @return The rendered skia Bitmap.
     */
    fun drawToBitmap(textMeasurer: TextMeasurer): Bitmap {
        val drawScope = CanvasDrawScope()
        val size = Size(
            dpi.value * resolutionHoriz.value,
            dpi.value * resolutionVert.value
        )
        val bitmap = drawScope.asBitmap(size) {
            CardState.card.value.cardElements.value.asReversed().forEach { cardElement ->
                val (maxAvailableWidth, maxAvailableHeight) = getAvailableSpace(cardElement.transformations)

                val horizontalStackingIterations =
                    (cardElement.stacking?.horizontalIterations?.get() ?: 1).coerceAtLeast(0)
                val horizontalStackingInterval = cardElement.stacking?.horizontalInterval?.get() ?: 0F
                val verticalStackingIterations =
                    (cardElement.stacking?.verticalIterations?.get() ?: 1).coerceAtLeast(0)
                val verticalStackingInterval = cardElement.stacking?.verticalInterval?.get() ?: 0F

                for (column in 0..<horizontalStackingIterations) {
                    for (row in 0..<verticalStackingIterations) {
                        var elementWidth: Float
                        var elementHeight: Float

                        when (cardElement) {
                            is RichTextElement -> {
                                val text = cardElement.text.richTextState.annotatedString
                                val style = TextStyle.Default

                                elementWidth = abs(cardElement.transformations.width.get().coerceIn(-30000f, 30000f))

                                if (elementWidth == 0f) elementWidth = maxAvailableWidth

                                // Find line break spots for wrapping
                                val lineBreaks = mutableListOf<Int>()

                                var currentStartIndex = 0
                                while (currentStartIndex < text.length) {
                                    var lastIndex = text.length
                                    var spacesOnly = true

                                    while (textMeasurer.measure(
                                            text.subSequence(currentStartIndex, lastIndex),
                                            style
                                        ).size.width > elementWidth
                                    ) {
                                        if (spacesOnly) {
                                            val lastSpaceIndex = text.subSequence(currentStartIndex, lastIndex)
                                                .lastIndexOf(" ") + currentStartIndex
                                            if (lastSpaceIndex > currentStartIndex) {
                                                lastIndex = lastSpaceIndex
                                            } else {
                                                lastIndex = text.length
                                                spacesOnly = false
                                            }
                                        } else {
                                            if (lastIndex > currentStartIndex + 1) lastIndex--
                                            else break
                                        }
                                    }

                                    lineBreaks.add(lastIndex)
                                    currentStartIndex = lastIndex
                                }

                                // Apply the found line breaks
                                val wrappedText = buildAnnotatedString {
                                    var startIndex = 0
                                    for (breakIndex in lineBreaks) {
                                        if (text.subSequence(startIndex, breakIndex).text.startsWith(" ")) startIndex++

                                        append(text.subSequence(startIndex, breakIndex))
                                        startIndex = breakIndex
                                    }
                                    if (startIndex < text.length) {
                                        if (text.subSequence(startIndex, text.length).text.startsWith(" ")) startIndex++

                                        append(text.subSequence(startIndex, text.length))
                                    }
                                }

                                elementHeight = abs(cardElement.transformations.height.get().coerceIn(-30000f, 30000f))
                                if (elementHeight == 0f) elementHeight =
                                    textMeasurer.measure(wrappedText, style).size.height.toFloat()

                                if (cardElement.transformations.width.get() == 0f) elementWidth =
                                    textMeasurer.measure(wrappedText, style).size.width.toFloat()

                                val topLeft = getOffset(
                                    cardElement.transformations,
                                    elementWidth,
                                    elementHeight
                                )

                                val topLeftStacked = Offset(
                                    topLeft.x + column * horizontalStackingInterval,
                                    topLeft.y + row * verticalStackingInterval
                                )

                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = wrappedText,
                                    style = style,
                                    topLeft = topLeftStacked,
                                    size = Size(elementWidth, elementHeight)
                                )
                            }

                            is ImageElement -> {
                                if (cardElement.image.imageBitmap.value != null) {
                                    val imageWidth = cardElement.image.imageBitmap.value!!.width.toFloat()
                                    val imageHeight = cardElement.image.imageBitmap.value!!.height.toFloat()

                                    elementWidth = cardElement.transformations.width.get()
                                    elementHeight = cardElement.transformations.height.get()

                                    if (elementWidth == 0f) {
                                        if (elementHeight == 0f) {
                                            val widthRatio = maxAvailableWidth / imageWidth
                                            val heightRatio = maxAvailableHeight / imageHeight

                                            if (widthRatio > heightRatio) {
                                                elementWidth = imageWidth * heightRatio
                                                elementHeight = imageHeight * heightRatio
                                            } else {
                                                elementWidth = imageWidth * widthRatio
                                                elementHeight = imageHeight * widthRatio
                                            }
                                        } else {
                                            elementWidth = imageWidth * (elementHeight / imageHeight)
                                        }
                                    } else if (elementHeight == 0f) {
                                        elementHeight = imageHeight * (elementWidth / imageWidth)
                                    }

                                    val topLeft = getOffset(
                                        cardElement.transformations,
                                        elementWidth,
                                        elementHeight
                                    )

                                    val topLeftStacked = Offset(
                                        topLeft.x + column * horizontalStackingInterval,
                                        topLeft.y + row * verticalStackingInterval
                                    )

                                    scale(
                                        scaleX = (elementWidth / imageWidth),
                                        scaleY = (elementHeight / imageHeight),
                                        pivot = topLeftStacked
                                    ) {
                                        drawImage(
                                            image = cardElement.image.imageBitmap.value!!,
                                            topLeft = topLeftStacked
                                        )
                                        for (mask in cardElement.masks.value) {
                                            if (mask.imageBitmap.value == null) continue

                                            drawImage(
                                                image = mask.imageBitmap.value!!,
                                                topLeft = topLeftStacked,
                                                colorFilter = ColorFilter.tint(color = Color(mask.color.value)),
                                                blendMode = BlendMode.Color
                                            )
                                        }
                                    }
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }

            // Bleed border
            if (CardState.card.value.bleedColor.value > 0) {
                val bleedThickness: Float = CardState.card.value.dpi.value / 12.7f
                val color = Color(CardState.card.value.bleedColor.value)

                drawRect(
                    color = color,
                    size = Size(
                        CardState.card.value.dpi.value * CardState.card.value.resolutionHoriz.value,
                        CardState.card.value.dpi.value * CardState.card.value.resolutionVert.value
                    ),
                    style = Stroke(width = bleedThickness * 2)
                )
            }
        }

        return bitmap.asSkiaBitmap()
    }

    companion object {
        /**
         * Create a new Card from a serialized Json object.
         * @param json The serialized Json object.
         * @return The new Card.
         * */
        fun fromJson(json: JsonObject): Card {
            val card = Card()

            card.dpi.value = json["dpi"]?.jsonPrimitive?.intOrNull ?: card.dpi.value
            card.resolutionHoriz.value =
                json["resolutionHoriz"]?.jsonPrimitive?.floatOrNull ?: card.resolutionHoriz.value
            card.resolutionVert.value = json["resolutionVert"]?.jsonPrimitive?.floatOrNull ?: card.resolutionVert.value
            card.bleedColor.value = json["bleedColor"]?.jsonPrimitive?.longOrNull ?: card.bleedColor.value

            val cardElementsList: MutableList<CardElement> = mutableListOf()
            json["cardElements"]?.jsonArray?.forEach {
                val cardElement = CardElement.fromJson(it.jsonObject, card)
                if (cardElement != null) cardElementsList.add(cardElement)
            }
            card.cardElements.value = cardElementsList

            return card
        }
    }
}

/**
 * Convert this CanvasDrawScope into a Compose Bitmap.
 * @param size The dimensions of the canvas.
 * @param onDraw The drawable context that will be rendered into a Compose Bitmap.
 */
fun CanvasDrawScope.asBitmap(size: Size, onDraw: DrawScope.() -> Unit): ImageBitmap {
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    draw(Density(1f), LayoutDirection.Ltr, Canvas(bitmap), size) { onDraw() }
    return bitmap
}