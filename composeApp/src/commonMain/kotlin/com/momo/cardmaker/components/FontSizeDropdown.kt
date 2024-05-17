package com.momo.cardmaker.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** The composable dropdown that's used for font size selection in the RichTextStyleRow. */
@Composable
fun FontSizeDropdownMenu(
    onSizeSelected: (Float) -> Unit,
    selectedSize: Float
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.width(100.dp)) {
        Row(
            modifier = Modifier
                .focusProperties { canFocus = false }
                .fillMaxWidth()
                .clickable { expanded = !expanded })
        {
            Icon(
                modifier = Modifier.padding(horizontal = 4.dp).width(30.dp),
                imageVector = Icons.Outlined.FormatSize,
                contentDescription = "Font Size Icon"
            )

            Text(
                modifier = Modifier.weight(1f),
                text = selectedSize.toInt().toString(),
                textAlign = TextAlign.End
            )

            Icon(
                modifier = Modifier.width(30.dp),
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Font Selection"
            )
        }
        DropdownMenu(
            modifier = Modifier
                .focusProperties { canFocus = false },
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (i in 1..300) {
                DropdownMenuItem(onClick = {
                    onSizeSelected(i.toFloat())
                    expanded = false
                }) {
                    Text(text = i.toString())
                }
            }
        }
    }
}
