package com.momo.cardmaker

import FontInfo
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.request.ImageRequest
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material.OutlinedRichTextEditor
import com.momo.cardmaker.components.ColorPickerState
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

abstract class Parameter<T>(
    defaultName: String,
    defaultExpression: String,
    isPinnedDefault: Boolean = false
) {
    var name = mutableStateOf(defaultName)
    var expression = mutableStateOf(defaultExpression)
    var isPinned = mutableStateOf(isPinnedDefault)

    /** Serialize this object into a Json string. */
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
        }
    }

    @Composable
    abstract fun buildElements(modifier: Modifier, label: MutableState<String>, isPinnedElements: Boolean)

    abstract fun get(): T

    fun evaluate(): Double {
        var s = expression.value

        val regex = Regex("\\{([^}]*)}")

        val matches = regex.findAll(s)

        for (match in matches) {
            val content = match.groups[1]?.value
            if (content != null) {
                val lastDotIndex = content.lastIndexOf('.')

                if (lastDotIndex != -1) {
                    val cardElementName = content.substring(0, lastDotIndex)
                    val propertyValueName = content.substring(lastDotIndex + 1)

                    if (cardElementName.isEmpty() || propertyValueName.isEmpty()) continue

                    val cardElement =
                        CardState.card.value.cardElements.value.find { it.name.value == cardElementName }

                    if (cardElement != null) {
                        val propertyValue = cardElement.getPropertyValueByName(propertyValueName)

                        if (propertyValue != null) {
                            s = s.replace("{$content}", propertyValue.toString())
                        }
                    }
                }
            }
        }

        return Keval.eval(s).coerceIn(-30000.0, 30000.0)
    }

    /** Gets the last integer value in the expression string. */
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
                    newConstantString =
                        constantString.replace(
                            match2.value,
                            (((match2.value.toDouble() - add) * 10000.0).roundToInt() / 10000.0).toString()
                        )

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
                    newConstantString =
                        constantString.replace(
                            match2.value,
                            (((match2.value.toDouble() + add) * 10000.0).roundToInt() / 10000.0).toString()
                        )

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
        /** Create a new object from a Json object. */
        fun fromJson(json: JsonObject, imageElement: ImageElement? = null): Parameter<out Comparable<*>>? {
            val type = json["type"]?.jsonPrimitive?.content
            val name = json["name"]?.jsonPrimitive?.content ?: ""
            val expression = json["expression"]?.jsonPrimitive?.content ?: ""
            val isPinned = json["isPinned"]?.jsonPrimitive?.boolean ?: false

            val parameter = when (type) {
                "int" -> IntParameter(defaultName = name, defaultExpression = expression, isPinnedDefault = isPinned)
                "float" -> FloatParameter(
                    defaultName = name,
                    defaultExpression = expression,
                    isPinnedDefault = isPinned
                )

                "richText" -> RichTextParameter(
                    defaultName = name,
                    defaultExpression = expression,
                    isPinnedDefault = isPinned
                )

                "image" -> ImageParameter(
                    defaultName = name,
                    defaultExpression = expression,
                    isPinnedDefault = isPinned
                )

                "mask" -> MaskParameter(
                    defaultName = name,
                    defaultExpression = expression,
                    isPinnedDefault = isPinned,
                    imageElement = imageElement
                )

                else -> null
            }

            if (parameter is MaskParameter) parameter.color.value =
                json["color"]?.jsonPrimitive?.intOrNull?.toLong() ?: parameter.color.value

            return parameter
        }
    }
}

class IntParameter(defaultName: String, defaultExpression: String, isPinnedDefault: Boolean = false) :
    Parameter<Int>(defaultName, defaultExpression, isPinnedDefault) {
    @Composable
    override fun buildElements(modifier: Modifier, label: MutableState<String>, isPinnedElements: Boolean) {
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

class FloatParameter(defaultName: String, defaultExpression: String, isPinnedDefault: Boolean = false) :
    Parameter<Float>(defaultName, defaultExpression, isPinnedDefault) {
    @Composable
    override fun buildElements(modifier: Modifier, label: MutableState<String>, isPinnedElements: Boolean) {
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
                            addToConstant(1.0f)
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

class RichTextParameter(defaultName: String, defaultExpression: String, isPinnedDefault: Boolean = false) :
    Parameter<String>(defaultName, defaultExpression, isPinnedDefault) {
    val richTextState = RichTextState().setHtml(expression.value)
    private val color: MutableState<Long> = mutableStateOf(0xFFFF0000)

    @Composable
    override fun buildElements(modifier: Modifier, label: MutableState<String>, isPinnedElements: Boolean) {
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
                        state = richTextState,
                        color = color,
                        modifier = Modifier
                            .fillMaxWidth()
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
}


open class ImageParameter(defaultName: String, defaultExpression: String, isPinnedDefault: Boolean = false) :
    Parameter<String>(defaultName, defaultExpression, isPinnedDefault) {
    var imageBitmap: MutableState<ImageBitmap?> = mutableStateOf(null)
    var uriChanged = true

    @Composable
    override fun buildElements(modifier: Modifier, label: MutableState<String>, isPinnedElements: Boolean) {
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
                                if (it.key.equals(Key.Enter)) {
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

class MaskParameter(
    defaultName: String,
    defaultExpression: String,
    isPinnedDefault: Boolean = false,
    val imageElement: ImageElement?
) :
    ImageParameter(defaultName, defaultExpression, isPinnedDefault) {
    val color = mutableStateOf(0xFF000000)

    @Composable
    override fun buildElements(modifier: Modifier, label: MutableState<String>, isPinnedElements: Boolean) {
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
                            if (it.key.equals(Key.Enter)) {
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
                            val list = imageElement?.masks?.value?.toMutableList()
                            list?.remove(this@MaskParameter)

                            if (list != null) {
                                imageElement?.masks?.value = list
                            }
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