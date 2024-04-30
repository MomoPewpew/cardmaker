package com.momo.cardmaker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material.OutlinedRichTextEditor
import com.momo.cardmaker.components.RichTextStyleRow
import com.notkamui.keval.Keval
import com.notkamui.keval.KevalInvalidExpressionException
import com.notkamui.keval.KevalInvalidSymbolException
import com.notkamui.keval.KevalZeroDivisionException
import kotlin.math.round

abstract class Parameter<T>(
    val name: String,
    var expression: String,
    val isHighlighted: Boolean = false
) {
    abstract @Composable
    fun buildElements(modifier: Modifier)

    abstract fun get(): T

    /** Gets the last integer value in the expression string. */
    fun addToConstant(add: Double) {
        try {
            // Confirm that the expression can be parsed
            Keval.eval(expression)

            // Establish either a constant value, or whether the last segment is a constant addition/substraction
            var constantString = ""
            if (expression.isEmpty()) {
                // Expression is empty and becomes 0
                constantString = "0"
                expression = constantString
            } else {
                val regex = Regex("(?:(?<=[0-9)}])[ ]*[+-][ ]*|^[ ]*)[0-9.]+\$")
                val match = regex.find(expression)

                if (match == null) {
                    // Expression is not empty but doesn't end with a constant addition/substraction
                    constantString = "0"
                    expression += "+$constantString"
                } else {
                    // Expression already has a constant addition/substraction at the end
                    constantString = match.value.trimStart()
                }
            }

            var newConstantString: String
            if (constantString.startsWith("-")) {
                // constantString must be "-[ ]*[0-9.]+$" at this point
                val regex = Regex("[0-9.]+\$")
                val match = regex.find(constantString)

                if (match == null) {
                    throw IllegalArgumentException("An error occurred while incrementing or decrementing. This should never happen. Please report this to Momo.")
                } else {
                    newConstantString =
                        constantString.replace(match.value, (match.value.toDouble() - add).toString())

                    // Handle double negatives on a negative to positive flip
                    val regex2 = Regex("- *-")
                    val match2 = regex2.find(newConstantString)
                    if (match2 != null) {
                        newConstantString = "+${newConstantString.replace("-", "")}"
                    }
                }
            } else {
                // constantString must be "[+]?[ ]*[0-9.]+$" at this point
                val regex = Regex("[0-9.]+\$")
                val match = regex.find(constantString)

                if (match == null) {
                    throw IllegalArgumentException("An error occurred while incrementing or decrementing. This should never happen. Please report this to Momo.")
                } else {
                    println(match.value)
                    newConstantString =
                        constantString.replace(match.value, (match.value.toDouble() + add).toString())

                    // Handle redundant plus signs on a positive to negative flip
                    val regex2 = Regex("[+] *-")
                    val match2 = regex2.find(newConstantString)
                    if (match2 != null) {
                        newConstantString = "-${newConstantString.replace("-", "").replace("+", "")}"
                    }
                }
            }

            expression = expression.replace(constantString, newConstantString)
        } catch (e: Exception) {
            when (e) {
                is KevalInvalidSymbolException, is KevalInvalidExpressionException, is KevalZeroDivisionException -> {
                    throw IllegalArgumentException("Your expression cannot be parsed. Please fix your expression before trying to increment or decrement it.")
                }

                else -> {
                    throw IllegalArgumentException("An error occurred while incrementing or decrementing. This should never happen. Please report this to Momo.")
                }
            }
        }
    }
}

class IntParameter(name: String, expression: String, isHighlighted: Boolean = false) :
    Parameter<Int>(name, expression, isHighlighted) {
    @Composable
    override fun buildElements(modifier: Modifier) {
        var numberText by remember { mutableStateOf(expression) }
        Row(
            modifier = modifier
                .padding(
                    horizontal = 16.dp
                )
        ) {
            Button(modifier = Modifier.height(48.dp),
                onClick = {
                    addToConstant(1.0)
                    numberText = expression
                }) {
                Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = "Increase")
            }
            TextField(
                modifier = Modifier.height(48.dp),
                maxLines = 1,
                value = numberText,
                onValueChange = { newValue ->
                    numberText = newValue
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Button(modifier = Modifier.height(48.dp),
                onClick = {
                    addToConstant(-1.0)
                    numberText = expression
                }) {
                Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = "Decrease")
            }
        }
    }

    override fun get(): Int {
        return try {
            round(Keval.eval(expression)).toInt()
        } catch (e: Exception) {
            0
        }
    }
}

class DoubleParameter(name: String, expression: String, isHighlighted: Boolean = false) :
    Parameter<Double>(name, expression, isHighlighted) {
    @Composable
    override fun buildElements(modifier: Modifier) {
        var numberText by remember { mutableStateOf(expression) }
        Row(
            modifier = modifier
                .padding(
                    horizontal = 16.dp
                )
        ) {
            Button(modifier = Modifier.height(48.dp),
                onClick = {
                    addToConstant(0.05)
                    numberText = expression
                }) {
                Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = "Increase")
            }
            TextField(
                modifier = Modifier.height(48.dp),
                maxLines = 1,
                value = numberText,
                onValueChange = { newValue ->
                    numberText = newValue
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Button(modifier = Modifier.height(48.dp),
                onClick = {
                    addToConstant(-0.05)
                    numberText = expression
                }) {
                Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = "Decrease")
            }
        }
    }

    override fun get(): Double {
        return try {
            Keval.eval(expression)
        } catch (e: Exception) {
            0.0
        }
    }
}

class TextParameter(name: String, expression: String, isHighlighted: Boolean = false) :
    Parameter<String>(name, expression, isHighlighted) {
    @Composable
    override fun buildElements(modifier: Modifier) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            val richTextState = rememberRichTextState()

            RichTextStyleRow(
                state = richTextState,
                modifier = Modifier
                    .fillMaxWidth()
            )

            OutlinedRichTextEditor(
                modifier = Modifier
                    .fillMaxWidth(),
                state = richTextState,
            )
        }
    }

    override fun get(): String {
        return expression
    }
}