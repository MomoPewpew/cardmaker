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
                .fillMaxWidth()
                .clickable { expanded = !expanded })
        {
            Text(text = selectedFont?.family ?: "Select Font")
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Font Selection")
        }
        DropdownMenu(
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
