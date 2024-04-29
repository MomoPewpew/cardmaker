package com.momo.cardmaker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.notkamui.keval.Keval
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
                    constantString = match.toString().trimStart()
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
                        constantString.replace(match.toString(), (match.toString().toDouble() - add).toString())

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
                    newConstantString =
                        constantString.replace(match.toString(), (match.toString().toDouble() + add).toString())

                    // Handle redundant plus signs on a positive to negative flip
                    val regex2 = Regex("[+] *-")
                    val match2 = regex2.find(newConstantString)
                    if (match2 != null) {
                        newConstantString = "-${newConstantString.replace("-", "").replace("+", "")}"
                    }
                }
            }

            expression.replace(constantString, newConstantString)
        } catch (e: Exception) {
            throw IllegalArgumentException("Your expression cannot be parsed. Please fix your expression before trying to increment or decrement it.")
        }
    }
}

class IntParameter(name: String, expression: String, isHighlighted: Boolean = false) :
    Parameter<Int>(name, expression, isHighlighted) {
    @Composable
    override fun buildElements(modifier: Modifier) {

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

    }

    override fun get(): Double {
        return try {
            Keval.eval(expression)
        } catch (e: Exception) {
            0.0
        }
    }
}
