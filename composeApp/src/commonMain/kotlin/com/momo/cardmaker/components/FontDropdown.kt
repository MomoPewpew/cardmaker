import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties

@Composable
fun FontDropdownMenu(
    fontList: List<FontInfo>,
    onFontSelected: (FontInfo) -> Unit,
    selectedFont: FontInfo?
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
            Text(text = selectedFont?.family ?: "Select Font")
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Font Selection")
        }
        DropdownMenu(
            modifier = Modifier
                .focusProperties { canFocus = false },
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            fontList.forEach { font ->
                DropdownMenuItem(onClick = {
                    onFontSelected(font)
                    expanded = false
                }) {
                    Text(text = font.family)
                }
            }
        }
    }
}
