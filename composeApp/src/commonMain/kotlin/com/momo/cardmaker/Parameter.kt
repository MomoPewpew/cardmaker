package com.momo.cardmaker

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
    val isPinned: Boolean = false
) {
    abstract @Composable
    fun buildElements(modifier: Modifier, label: String)

    abstract fun get(): T

    /** Gets the last integer value in the expression string. */
    fun addToConstant(add: Double) {
        try {
            // Confirm that the expression can be parsed
            Keval.eval(expression)

            // Handle redundant + sign on constant value
            val regex = Regex("^[ +]+[0-9.]+\$")
            val match = regex.find(expression)
            if (match != null) {
                expression = expression.replace("+", "")
            }

            // Establish either a constant value, or whether the last segment is a constant addition/substraction
            var constantString = ""
            if (expression.isEmpty()) {
                // Expression is empty and becomes 0
                constantString = "0"
                expression = constantString
            } else {
                val regex2 = Regex("(?:(?<=[0-9)}]) *[+-] *|^ *-? *)[0-9.]+\$")
                val match2 = regex2.find(expression)
                if (match2 == null) {
                    // Expression is not empty but doesn't end with a constant addition/substraction
                    constantString = "+0"
                    expression += constantString
                } else {
                    // Expression already has a constant addition/substraction at the end
                    constantString = match2.value.trimStart()
                }
            }

            var newConstantString: String
            if (constantString.startsWith("-")) {
                // constantString must be "-[ ]*[0-9.]+$" at this point
                val regex2 = Regex("[0-9.]+\$")
                val match2 = regex2.find(constantString)

                if (match2 == null) {
                    throw IllegalArgumentException("An error occurred while incrementing or decrementing. This should never happen. Please report this to Momo.")
                } else {
                    newConstantString =
                        constantString.replace(match2.value, (match2.value.toDouble() - add).toString())

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
                    throw IllegalArgumentException("An error occurred while incrementing or decrementing. This should never happen. Please report this to Momo.")
                } else {
                    newConstantString =
                        constantString.replace(match2.value, (match2.value.toDouble() + add).toString())

                    // Handle redundant plus signs on a positive to negative flip
                    val regex3 = Regex("[+] *-")
                    val match3 = regex3.find(newConstantString)
                    if (match3 != null) {
                        newConstantString = "-${newConstantString.replace("-", "").replace("+", "")}"
                    }
                }
            }

            expression = expression.substring(0, (expression.length - constantString.length)) + newConstantString

            // Handle the +0.0 case
            val regex2 = Regex("^[+] *0.0\$")
            val match2 = regex2.find(expression)
            if (match2 != null) {
                expression = "0.0"
            }
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
    override fun buildElements(modifier: Modifier, label: String) {
        var numberText by remember { mutableStateOf(expression) }
        Row(
            modifier = Modifier
                .height(48.dp)
                .padding(
                    horizontal = 16.dp
                )
        ) {
            if (label.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.h5
                    )
                }
            }
            Column(
                modifier = Modifier.weight(4f)
            ) {
                Row() {
                    Column(
                        modifier = Modifier.width(48.dp)
                    ) {
                        Button(modifier = Modifier
                            .fillMaxSize(),
                            onClick = {
                                addToConstant(1.0)
                                numberText = expression
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
                            value = numberText,
                            onValueChange = { newValue ->
                                numberText = newValue
                                expression = newValue
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                        numberText = expression
                    }) {
                    Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = "Decrease")
                }
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
    override fun buildElements(modifier: Modifier, label: String) {
        var numberText by remember { mutableStateOf(expression) }
        Row(
            modifier = Modifier
                .height(48.dp)
                .padding(
                    horizontal = 16.dp
                )
        ) {
            if (label.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.h5
                    )
                }
            }
            Column(
                modifier = Modifier.weight(4f)
            ) {
                Row() {
                    Column(
                        modifier = Modifier.width(48.dp)
                    ) {
                        Button(modifier = Modifier
                            .fillMaxSize(),
                            onClick = {
                                addToConstant(0.05)
                                numberText = expression
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
                            value = numberText,
                            onValueChange = { newValue ->
                                numberText = newValue
                                expression = newValue
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                        numberText = expression
                    }) {
                    Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = "Decrease")
                }
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
    override fun buildElements(modifier: Modifier, label: String) {
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