import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun FontSizeDropdownMenu(
    onSizeSelected: (Float) -> Unit,
    selectedSize: Float
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.width(75.dp)) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .focusProperties { canFocus = false }
                .fillMaxWidth()
                .clickable { expanded = !expanded })
        {
            Text(
                text = selectedSize.toInt().toString(),
                textAlign = TextAlign.End
            )
            Icon(
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
