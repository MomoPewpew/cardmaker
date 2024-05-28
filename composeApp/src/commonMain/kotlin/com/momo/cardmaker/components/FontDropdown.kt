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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.momo.cardmaker.components.FontDropdownState.fontFamilyMap

/** The state manager for the map of FontFamilies. We cache these, since a font family has to be created in a composable context. */
object FontDropdownState {
    val fontFamilyMap: MutableMap<String, FontFamily?> = mutableMapOf(
        Pair("Default", null),
        Pair("Serif", FontFamily.Serif),
        Pair("SansSerif", FontFamily.SansSerif),
        Pair("Cursive", FontFamily.Cursive),
        Pair("Monospace", FontFamily.Monospace)
    )
}

/** The composable dropdown that's used for font family selection in the RichTextStyleRow. */
@Composable
fun FontDropdownMenu(
    onFontSelected: (String) -> Unit,
    selectedFont: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .focusProperties { canFocus = false }
                .fillMaxWidth()
                .clickable { expanded = !expanded })
        {
            Icon(
                modifier = Modifier.padding(horizontal = 4.dp),
                imageVector = Icons.Outlined.FormatSize,
                contentDescription = "Font Family Icon"
            )

            Text(text = selectedFont, fontFamily = fontFamilyMap[selectedFont])

            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Default")
        }
        DropdownMenu(
            modifier = Modifier
                .focusProperties { canFocus = false },
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            fontFamilyMap.keys.sorted().forEach { family ->
                DropdownMenuItem(onClick = {
                    onFontSelected(
                        family
                    )
                    expanded = false
                }) {
                    Text(text = family, fontFamily = fontFamilyMap[family])
                }
            }
        }
    }
}
