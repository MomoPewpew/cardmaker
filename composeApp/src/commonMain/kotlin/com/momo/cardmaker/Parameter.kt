package com.momo.cardmaker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material.OutlinedRichTextEditor
import com.momo.cardmaker.components.PopupState
import com.momo.cardmaker.components.RenameState
import com.momo.cardmaker.components.RichTextStyleRow
import com.notkamui.keval.Keval
import com.notkamui.keval.KevalInvalidExpressionException
import com.notkamui.keval.KevalInvalidSymbolException
import com.notkamui.keval.KevalZeroDivisionException
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

    @Composable
    abstract fun buildElements(modifier: Modifier, label: MutableState<String>)

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

        return Keval.eval(s)
    }

    /** Gets the last integer value in the expression string. */
    fun addToConstant(add: Double) {
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
}

class IntParameter(defaultName: String, defaultExpression: String, isHighlighted: Boolean = false) :
    Parameter<Int>(defaultName, defaultExpression, isHighlighted) {
    @Composable
    override fun buildElements(modifier: Modifier, label: MutableState<String>) {
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
                                    addToConstant(1.0)
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
                            addToConstant(-1.0)
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

class FloatParameter(defaultName: String, defaultExpression: String, isHighlighted: Boolean = false) :
    Parameter<Float>(defaultName, defaultExpression, isHighlighted) {
    @Composable
    override fun buildElements(modifier: Modifier, label: MutableState<String>) {
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
                                    addToConstant(0.05)
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
                            addToConstant(-0.05)
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

class RichTextParameter(defaultName: String, defaultExpression: String, isHighlighted: Boolean = false) :
    Parameter<String>(defaultName, defaultExpression, isHighlighted) {
    val richTextState = RichTextState().setMarkdown(expression.value)
    private val color: MutableState<Long> = mutableStateOf(0xFFFF0000)

    @Composable
    override fun buildElements(modifier: Modifier, label: MutableState<String>) {
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
                    val markDown by remember(richTextState.annotatedString) {
                        mutableStateOf(richTextState.toMarkdown())
                    }

                    expression.value = markDown

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


class UriParameter(defaultName: String, defaultExpression: String, isHighlighted: Boolean = false) :
    Parameter<String>(defaultName, defaultExpression, isHighlighted) {
    @Composable
    override fun buildElements(modifier: Modifier, label: MutableState<String>) {
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
                            .padding(vertical = 8.dp),
                        maxLines = 1,
                        value = expression.value,
                        onValueChange = {
                            expression.value = it
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
}