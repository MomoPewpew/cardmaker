import FontDropdownState.fontFamilyMap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

object FontDropdownState {
    val fontFamilyMap: MutableMap<String, FontFamily?> = mutableMapOf(
        Pair("Default", null),
        Pair("Serif", FontFamily.Serif),
        Pair("SansSerif", FontFamily.SansSerif),
        Pair("Cursive", FontFamily.Cursive),
        Pair("Monospace", FontFamily.Monospace)
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FontDropdownMenu(
    fontList: List<FontInfo>,
    onFontSelected: (FontFamily?) -> Unit,
    selectedFont: String
) {
    var expanded by remember { mutableStateOf(false) }

    if (fontFamilyMap.size == 5) {
        fontList.forEach { fontInfo ->
            fontFamilyMap[fontInfo.family] = FontFamily(Font(resource = FontResource(fontInfo.filename)))
        }
    }

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

            Text(text = selectedFont)

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
                        fontFamilyMap[family]
                    )
                    expanded = false
                }) {
                    Text(text = family)
                }
            }
        }
    }
}
