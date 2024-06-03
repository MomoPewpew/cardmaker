package com.momo.cardmaker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.request.ImageRequest
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material.OutlinedRichTextEditor
import com.momo.cardmaker.components.ColorPickerState
import com.momo.cardmaker.components.FontDropdownState.fontFamilyMap
import com.momo.cardmaker.components.PopupState
import com.momo.cardmaker.components.RenameState
import com.momo.cardmaker.components.RichTextStyleRow
import com.notkamui.keval.Keval
import com.notkamui.keval.KevalInvalidExpressionException
import com.notkamui.keval.KevalInvalidSymbolException
import com.notkamui.keval.KevalZeroDivisionException
import kotlinx.serialization.json.*
import kotlin.math.round
import kotlin.math.roundToInt

/** This object maintains a tree of all card elements that reference other card elements. */
object EvaluateState {
    val referenceTree = mutableStateMapOf<CardElement, MutableList<CardElement>>()
    var visitedElements = mutableSetOf<CardElement>()
    var stopReplacements = false

    /**
     * Walk the reference tree to ensure that you don't end up back at the starting element.
     * @param motherElement This is the card element that holds the reference that we are evaluating.
     * @param cardElement This is the card element that is referenced by the mother element, and also the starting point of the walk.
     * @return A boolean that represent whether a cyclical reference occurred.
     * */
    fun walkReferenceTree(motherElement: CardElement, cardElement: CardElement): Boolean {
        if (visitedElements.contains(cardElement)) return false

        visitedElements.add(cardElement) // Mark element as visited

        referenceTree[cardElement]?.forEach {
            if (it == motherElement) {
                return true // Cycle detected
            } else {
                val hasCycle = walkReferenceTree(motherElement, it)
                if (hasCycle) return true
            }
        }
        visitedElements.remove(cardElement) // Remove from visited after traversal
        return false
    }
}

/**
 * Parameters represent the user-configurable parts of a CardElement.
 * @param defaultName The default name for this parameter.
 * @param defaultExpression The default value for this parameter. All parameters are stored as Strings which makes for easy Json serialization. Numeric parameters are evaluated as mathematical expressions.
 * @param cardElement The CardElement object that holds this Parameter.
 * @param isPinnedDefault Whether this Parameter should start out as pinned.
 */
abstract class Parameter<T>(
    defaultName: String,
    defaultExpression: String,
    val cardElement: CardElement,
    isPinnedDefault: Boolean = false
) {
    var name = mutableStateOf(defaultName)
    var expression = mutableStateOf(defaultExpression)
    var isPinned = mutableStateOf(isPinnedDefault)

    /**
     * Serialize this object to a Json object.
     * @return A Json object that holds the serialized Parameter.
     * */
    fun toJson(): JsonObject {
        return buildJsonObject {
            when (this@Parameter) {
                is IntParameter -> put("type", "int")
                is FloatParameter -> put("type", "float")
                is RichTextParameter -> put("type", "richText")
                is MaskParameter -> put("type", "mask")
                is ImageParameter -> put("type", "image")
                else -> {}
            }
            put("name", name.value)
            put("expression", expression.value)
            put("isPinned", isPinned.value)
            if (this@Parameter is MaskParameter) put("color", color.value)
            if (this@Parameter is RichTextParameter) put("fontFamilyName", fontFamilyName.value)
        }
    }

    /**
     * Build the composable elements that are needed to change this Parameter
     * @param label The name of the Parameter as shown in the UI.
     * @param isPinnedElements Whether the element render is happening in the Pinned submenu.
     */
    @Composable
    abstract fun buildElements(label: MutableState<String>, isPinnedElements: Boolean)

    /**
     * Evaluate the outcome of this Parameter.
     * @return The evaluated outcome of this Parameter.
     */
    abstract fun get(): T

    /**
     * Evaluate references, and then evaluate this Parameter as a mathematical expression.
     * @return The evaluated result, or 0 if the expression is invalid.
     */
    fun evaluate(): Double {
        var s = expression.value

        EvaluateState.referenceTree[cardElement] = mutableListOf()
        val list = EvaluateState.referenceTree[cardElement]

        val regex = Regex("\\{([^}]*)}")

        val matches = regex.findAll(s)

        for (match in matches) {
            val content = match.groups[1]?.value
            if (content != null) {
                if (EvaluateState.stopReplacements) {
                    s = s.replace("{$content}", "0")
                } else {
                    val lastDotIndex = content.lastIndexOf('.')

                    if (lastDotIndex != -1) {
                        val cardElementName = content.substring(0, lastDotIndex)
                        val propertyValueName = content.substring(lastDotIndex + 1)

                        if (cardElementName.isEmpty() || propertyValueName.isEmpty()) continue

                        val cardElement =
                            CardState.card.value.cardElements.value.find { it.name.value == cardElementName }

                        if (cardElement != null) {
                            list!!.add(cardElement)

                            EvaluateState.visitedElements = mutableSetOf()
                            if (EvaluateState.walkReferenceTree(
                                    this.cardElement,
                                    cardElement
                                )
                            ) {
                                PopupState.popup(
                                    "Circular reference error",
                                    "A circular reference has been detected in your expressions.\n\n" +
                                            "Until you resolve this circular reference, all references will be treated as value 0."
                                )
                                s = s.replace("{$content}", "0")
                                EvaluateState.stopReplacements = true
                            } else {
                                val propertyValue = cardElement.getPropertyValueByName(propertyValueName)

                                if (propertyValue != null) {
                                    s = s.replace("{$content}", propertyValue.toString())
                                }
                            }
                        }
                    }
                }
            }
        }

        return Keval.eval(s)
    }

    /**
     * Add to the last constant value in the expression string.
     * @param add The amount that should be added.
     */
    fun addToConstant(add: Float) {
        try {
            // Confirm that the expression can be parsed
            evaluate()

            // Handle redundant + sign on constant value
            val regex = Regex("^[ +]+[0-9.]+\$")
            val match = regex.find(expression.value)
            if (match != null) {
                expression.value = expression.value.replace("+", "")
            }

            // Establish either a constant value, or whether the last segment is a constant addition/subtraction
            val constantString: String
            if (expression.value.isEmpty()) {
                // expression.value is empty and becomes 0
                constantString = "0"
                expression.value = constantString
            } else {
                val regex2 = Regex("(?:(?<=[0-9)}]) *[+-] *|^ *-? *)[0-9.]+\$")
                val match2 = regex2.find(expression.value)
                if (match2 == null) {
                    // Expression is not empty but doesn't end with a constant addition/subtraction
                    constantString = "+0"
                    expression.value += constantString
                } else {
                    // Expression already has a constant addition/subtraction at the end
                    constantString = match2.value.trimStart()
                }
            }

            var newConstantString: String
            if (constantString.startsWith("-")) {
                // constantString must be "-[ ]*[0-9.]+$" at this point
                val regex2 = Regex("[0-9.]+\$")
                val match2 = regex2.find(constantString)

                if (match2 == null) {
                    PopupState.popup(
                        "Unexpected Error!",
                        "An error occurred while incrementing or decrementing. This should never happen. Please report this to Momo."
                    )
                    return
                } else {
                    var newValueString = (((match2.value.toFloat() - add) * 10000f).roundToInt() / 10000f).toString()
                    if (newValueString.contains("E")) newValueString = "0.0"

                    newConstantString = constantString.replace(match2.value, newValueString)

                    // Handle double negatives on a negative to positive flip
                    val regex3 = Regex("- *-")
                    val match3 = regex3.find(newConstantString)
                    if (match3 != null) {
                        newConstantString = "+${newConstantString.replace("-", "")}"
                    }

                    // Handle the -0.0 case
                    val regex4 = Regex("- *0.0\$")
                    val match4 = regex4.find(newConstantString)
                    if (match4 != null) {
                        newConstantString = newConstantString.replace("-", "+")
                    }
                }
            } else {
                // constantString must be "[+]?[ ]*[0-9.]+$" at this point
                val regex2 = Regex("[0-9.]+\$")
                val match2 = regex2.find(constantString)

                if (match2 == null) {
                    PopupState.popup(
                        "Unexpected Error!",
                        "An error occurred while incrementing or decrementing. This should never happen. Please report this to Momo."
                    )
                    return
                } else {
                    var newValueString = (((match2.value.toFloat() + add) * 10000f).roundToInt() / 10000f).toString()
                    if (newValueString.contains("E")) newValueString = "0.0"

                    newConstantString = constantString.replace(match2.value, newValueString)

                    // Handle redundant plus signs on a positive to negative flip
                    val regex3 = Regex("[+] *-")
                    val match3 = regex3.find(newConstantString)
                    if (match3 != null) {
                        newConstantString = "-${newConstantString.replace("-", "").replace("+", "")}"
                    }
                }
            }

            expression.value =
                expression.value.substring(0, (expression.value.length - constantString.length)) + newConstantString

            // Handle the +0.0 case
            val regex2 = Regex("^[+] *0.0\$")
            val match2 = regex2.find(expression.value)
            if (match2 != null) {
                expression.value = "0.0"
            }
        } catch (e: Exception) {
            when (e) {
                is KevalInvalidSymbolException, is KevalInvalidExpressionException, is KevalZeroDivisionException -> {
                    PopupState.popup(
                        "Parse Error",
                        "Your expression cannot be parsed. Please fix your expression before trying to increment or decrement it."
                    )
                    return
                }

                else -> {
                    PopupState.popup(
                        "Unexpected Error!",
                        "An error occurred while incrementing or decrementing. This should never happen. Please report this to Momo."
                    )
                    return
                }
            }
        }
    }

    companion object {
        /**
         * Create a new Parameter from a serialized Json object.
         * @param json The serialized Json object.
         * @param cardElement The CardElement that the new Parameter will belong to.
         * @return The newly created Parameter.
         */
        fun fromJson(
            json: JsonObject,
            cardElement: CardElement
        ): Parameter<out Comparable<*>>? {
            val type = json["type"]?.jsonPrimitive?.content
            val name = json["name"]?.jsonPrimitive?.content ?: ""
            val expression = json["expression"]?.jsonPrimitive?.content ?: ""
            val isPinned = json["isPinned"]?.jsonPrimitive?.boolean ?: false

            val parameter = when (type) {
                "int" -> IntParameter(
                    defaultName = name,
                    defaultExpression = expression,
                    cardElement = cardElement,
                    isPinnedDefault = isPinned
                )

                "float" -> FloatParameter(
                    defaultName = name,
                    defaultExpression = expression,
                    cardElement = cardElement,
                    isPinnedDefault = isPinned
                )

                "richText" -> RichTextParameter(
                    defaultName = name,
                    defaultExpression = expression,
                    cardElement = cardElement as RichTextElement,
                    isPinnedDefault = isPinned
                )

                "image" -> ImageParameter(
                    defaultName = name,
                    defaultExpression = expression,
                    cardElement = cardElement as ImageElement,
                    isPinnedDefault = isPinned
                )

                "mask" -> MaskParameter(
                    defaultName = name,
                    defaultExpression = expression,
                    cardElement = cardElement as ImageElement,
                    isPinnedDefault = isPinned
                )

                else -> null
            }

            if (parameter is MaskParameter) {
                parameter.color.value = json["color"]?.jsonPrimitive?.longOrNull ?: parameter.color.value
            } else if (parameter is RichTextParameter) {
                parameter.applyFontFamily(json["fontFamilyName"]?.jsonPrimitive?.content ?: "Default")
            }

            return parameter
        }
    }
}

/**
 * A parameter that holds Int values.
 * @param defaultName The default name for this parameter.
 * @param defaultExpression The default value for this parameter. All parameters are stored as Strings which makes for easy Json serialization. Numeric parameters are evaluated as mathematical expressions.
 * @param cardElement The CardElement object that holds this Parameter.
 * @param isPinnedDefault Whether this Parameter should start out as pinned.
 */
class IntParameter(
    defaultName: String,
    defaultExpression: String,
    cardElement: CardElement,
    isPinnedDefault: Boolean = false
) : Parameter<Int>(defaultName, defaultExpression, cardElement, isPinnedDefault) {
    @Composable
    override fun buildElements(label: MutableState<String>, isPinnedElements: Boolean) {
        Box {
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .padding(
                        horizontal = 16.dp
                    )
            ) {
                if (label.value.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = label.value,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.h5
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(4f)
                        .padding(start = 10.dp)
                ) {
                    Row {
                        Column(
                            modifier = Modifier.width(48.dp)
                        ) {
                            Button(modifier = Modifier
                                .fillMaxSize(),
                                onClick = {
                                    addToConstant(1.0f)
                                    expression.value = expression.value
                                }) {
                                Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = "Increase")
                            }
                        }
                        Column(
                            modifier = Modifier
                        ) {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                value = expression.value,
                                onValueChange = { newValue ->
                                    EvaluateState.stopReplacements = false
                                    expression.value = newValue
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier.width(48.dp)
                ) {
                    Button(modifier = Modifier
                        .fillMaxSize(),
                        onClick = {
                            addToConstant(-1.0f)
                            expression.value = expression.value
                        }) {
                        Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = "Decrease")
                    }
                }
            }
            // Clickable overlay
            if (ClickState.state.value != ClickState.States.NONE) {
                Box(modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        when (ClickState.state.value) {
                            ClickState.States.PINNING -> isPinned.value = !isPinned.value
                            ClickState.States.RENAMING -> RenameState.rename(name)

                            else -> {}
                        }
                        ClickState.off()
                    }
                )
            }
        }
    }

    override fun get(): Int {
        return try {
            round(evaluate()).toInt()
        } catch (e: Exception) {
            0
        }
    }
}

/**
 * A parameter that holds Float values.
 * @param defaultName The default name for this parameter.
 * @param defaultExpression The default value for this parameter. All parameters are stored as Strings which makes for easy Json serialization. Numeric parameters are evaluated as mathematical expressions.
 * @param cardElement The CardElement object that holds this Parameter.
 * @param isPinnedDefault Whether this Parameter should start out as pinned.
 */
class FloatParameter(
    defaultName: String,
    cardElement: CardElement,
    defaultExpression: String,
    isPinnedDefault: Boolean = false
) : Parameter<Float>(defaultName, defaultExpression, cardElement, isPinnedDefault) {
    @Composable
    override fun buildElements(label: MutableState<String>, isPinnedElements: Boolean) {
        Box {
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .padding(
                        horizontal = 16.dp
                    )
            ) {
                if (label.value.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = label.value,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.h5
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(4f)
                        .padding(start = 10.dp)
                ) {
                    Row {
                        Column(
                            modifier = Modifier
                                .width(48.dp)
                        ) {
                            Button(modifier = Modifier
                                .fillMaxSize(),
                                onClick = {
                                    addToConstant(1.0f)
                                    expression.value = expression.value
                                }) {
                                Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = "Increase")
                            }
                        }
                        Column(
                            modifier = Modifier
                        ) {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                value = expression.value,
                                onValueChange = { newValue ->
                                    EvaluateState.stopReplacements = false
                                    expression.value = newValue
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier.width(48.dp)
                ) {
                    Button(modifier = Modifier
                        .fillMaxSize(),
                        onClick = {
                            addToConstant(-1.0f)
                            expression.value = expression.value
                        }) {
                        Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = "Decrease")
                    }
                }
            }

            // Clickable overlay
            if (ClickState.state.value != ClickState.States.NONE) {
                Box(modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        when (ClickState.state.value) {
                            ClickState.States.PINNING -> isPinned.value = !isPinned.value
                            ClickState.States.RENAMING -> RenameState.rename(name)

                            else -> {}
                        }
                        ClickState.off()
                    }
                )
            }
        }
    }

    override fun get(): Float {
        return try {
            evaluate().toFloat()
        } catch (e: Exception) {
            0.0f
        }
    }
}

/**
 * A parameter that holds Rich Text.
 * @param defaultName The default name for this parameter.
 * @param defaultExpression The default value for this parameter. All parameters are stored as Strings which makes for easy Json serialization. Numeric parameters are evaluated as mathematical expressions.
 * @param cardElement The CardElement object that holds this Parameter.
 * @param isPinnedDefault Whether this Parameter should start out as pinned.
 */
class RichTextParameter(
    defaultName: String,
    defaultExpression: String,
    cardElement: RichTextElement,
    isPinnedDefault: Boolean = false
) : Parameter<String>(defaultName, defaultExpression, cardElement, isPinnedDefault) {
    val richTextState = RichTextState().setHtml(expression.value)
    val color: MutableState<Long> = mutableStateOf(0xFFFF0000)
    val fontFamilyName = mutableStateOf("Default")

    @Composable
    override fun buildElements(label: MutableState<String>, isPinnedElements: Boolean) {
        Box {
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp
                    )
            ) {
                if (label.value.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = label.value,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.h5
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(4f)
                ) {
                    val html by remember(richTextState.annotatedString) {
                        mutableStateOf(richTextState.toHtml())
                    }

                    expression.value = html

                    RichTextStyleRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        parameter = this@RichTextParameter
                    )

                    OutlinedRichTextEditor(
                        modifier = Modifier
                            .fillMaxWidth(),
                        state = richTextState
                    )
                }
            }

            // Clickable overlay
            if (ClickState.state.value != ClickState.States.NONE) {
                Box(modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        when (ClickState.state.value) {
                            ClickState.States.PINNING -> isPinned.value = !isPinned.value
                            ClickState.States.RENAMING -> RenameState.rename(name)

                            else -> {}
                        }
                        ClickState.off()
                    }
                )
            }
        }
    }

    override fun get(): String {
        return expression.value
    }

    fun applyFontFamily(familyName: String) {
        fontFamilyName.value = familyName

        val fontFamily = fontFamilyMap[familyName]

        val currentSel = TextRange(richTextState.selection.start, richTextState.selection.end)

        richTextState.selection = TextRange(0, richTextState.annotatedString.length)
        richTextState.addSpanStyle(SpanStyle(fontFamily = fontFamily))
        fontFamilyName.value = familyName

        richTextState.selection = currentSel
    }
}

/**
 * A parameter that holds an image URI and the image that was downloaded from it.
 * @param defaultName The default name for this parameter.
 * @param defaultExpression The default value for this parameter. All parameters are stored as Strings which makes for easy Json serialization. Numeric parameters are evaluated as mathematical expressions.
 * @param cardElement The CardElement object that holds this Parameter.
 * @param isPinnedDefault Whether this Parameter should start out as pinned.
 */
open class ImageParameter(
    defaultName: String,
    defaultExpression: String,
    cardElement: ImageElement,
    isPinnedDefault: Boolean = false
) :
    Parameter<String>(defaultName, defaultExpression, cardElement, isPinnedDefault) {
    var imageBitmap: MutableState<ImageBitmap?> = mutableStateOf(null)
    var uriChanged = true

    @Composable
    override fun buildElements(label: MutableState<String>, isPinnedElements: Boolean) {
        Box {
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp
                    )
            ) {
                if (label.value.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = label.value,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.h5
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(4f)
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .onFocusEvent { if (!it.isFocused) downloadImage() }
                            .onKeyEvent {
                                if (it.key == Key.Enter) {
                                    downloadImage()
                                    true
                                } else false
                            },
                        maxLines = 1,
                        value = expression.value,
                        onValueChange = {
                            expression.value = it
                            uriChanged = true
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                }
            }

            // Clickable overlay
            if (ClickState.state.value != ClickState.States.NONE) {
                Box(modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        when (ClickState.state.value) {
                            ClickState.States.PINNING -> isPinned.value = !isPinned.value
                            ClickState.States.RENAMING -> RenameState.rename(name)

                            else -> {}
                        }
                        ClickState.off()
                    }
                )
            }
        }
    }

    override fun get(): String {
        return expression.value
    }

    /**
     * Attempt to download the image that is hosted at the provided URL.
     */
    @OptIn(ExperimentalCoilApi::class)
    fun downloadImage() {
        if (!uriChanged || get().isEmpty()) return
        uriChanged = false

        val request = ImageRequest.Builder(context)
            .data(get())
            .target(
                onSuccess = { result ->
                    imageBitmap.value = result.toBitmap().asComposeImageBitmap()
                }
            ).build()

        imageLoader.enqueue(request)
    }
}

/**
 * A parameter that holds a mask image URI and the mask image that was downloaded from it.
 * @param defaultName The default name for this parameter.
 * @param defaultExpression The default value for this parameter. All parameters are stored as Strings which makes for easy Json serialization. Numeric parameters are evaluated as mathematical expressions.
 * @param cardElement The CardElement object that holds this Parameter.
 * @param isPinnedDefault Whether this Parameter should start out as pinned.
 */
class MaskParameter(
    defaultName: String,
    defaultExpression: String,
    cardElement: ImageElement,
    isPinnedDefault: Boolean = false
) :
    ImageParameter(defaultName, defaultExpression, cardElement, isPinnedDefault) {
    val color = mutableStateOf(0xFF000000)

    @Composable
    override fun buildElements(label: MutableState<String>, isPinnedElements: Boolean) {
        Box {
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp
                    )
            ) {
                if (label.value.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = label.value,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.h5
                        )
                    }
                }

                TextField(
                    modifier = Modifier
                        .weight(3f)
                        .padding(vertical = 8.dp)
                        .onFocusEvent { if (!it.isFocused) downloadImage() }
                        .onKeyEvent {
                            if (it.key == Key.Enter) {
                                downloadImage()
                                true
                            } else false
                        },
                    maxLines = 1,
                    value = expression.value,
                    onValueChange = {
                        expression.value = it
                        uriChanged = true
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                IconButton(modifier = Modifier
                    .weight(if (isPinnedElements) 1f else 0.5f)
                    .align(Alignment.CenterVertically),
                    onClick = {
                        ColorPickerState.pick(color)
                    }) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = "Mask Color Picker"
                    )
                }

                if (!isPinnedElements) {
                    IconButton(modifier = Modifier
                        .weight(0.5f)
                        .align(Alignment.CenterVertically),
                        onClick = {
                            val list = (cardElement as ImageElement).masks.value.toMutableList()
                            list.remove(this@MaskParameter)

                            cardElement.masks.value = list
                        }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Remove Mask"
                        )
                    }
                }
            }

            // Clickable overlay
            if (ClickState.state.value != ClickState.States.NONE) {
                Box(modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        when (ClickState.state.value) {
                            ClickState.States.PINNING -> isPinned.value = !isPinned.value
                            ClickState.States.RENAMING -> RenameState.rename(name)

                            else -> {}
                        }
                        ClickState.off()
                    }
                )
            }
        }
    }
}