package com.momo.cardmaker.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.momo.cardmaker.components.HelpState.showWindow

object HelpState {
    val showWindow = mutableStateOf(false)

    fun show() {
        showWindow.value = true
    }
}

@Composable
fun HelpWindow() {
    if (showWindow.value) {
        Dialog(
            onDismissRequest = { showWindow.value = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(4.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .height(800.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                        ) {
                            Column {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    text = "CardMaker v1.0",
                                    style = MaterialTheme.typography.h3,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    text = "Pinning",
                                    style = MaterialTheme.typography.h4
                                )
                                Text(
                                    text = "You can pin specific parameters using the thumbtack button on the top left.\n\n" +
                                            "Pinning an element is a way to highlight a parameter as one that is specific to this card, rather than a part of a template.\n\n" +
                                            "Pinned elements will conveniently show up in the 'Pinned' section, allowing you to easily keep track of which fields need to be changed to make new cards that look like this one."
                                )

                                Text(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    text = "Importing/Exporting",
                                    style = MaterialTheme.typography.h4
                                )
                                Text(
                                    text = "You can import/export cards using the import/export button on the bottom right.\n\n" +
                                            "In this menu, you can export your card as a PNG, or you can copy the Json values that make up this cards data in the textbox below.\n\n" +
                                            "When you click 'Import Json', the text box will become editable and you can paste in a Json string from a previous card. Click OK to confirm.\n\n" +
                                            "Importing only the pinned elements means that you will only import a parameter if it has the same element name, same parameter name, and also it is a pinned parameter in both the current and the imported card. This is a way for you to take the Json strings from old cards, and import them into updated templates."
                                )

                                Text(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    text = "Expressions",
                                    style = MaterialTheme.typography.h4
                                )
                                Text(
                                    text = "The transformation values support mathematical expressions, as well as references to other transformation values.\n\n" +
                                            "For example, of you want to have the Y-offset of an image be dependent on the height of a text field, you can set the Y-offset of that image to\n\n" +
                                            "{Lore Text.height} + 100\n\n" +
                                            "where 'Lore Text' in this example is the name of that text element.\n\n" +
                                            "Supported reference are 'offsetX', 'offsetY', 'width' and 'height'. If the width or height of the referenced element is set to 0 then the automatically determined dimension is referenced instead.\n\n" +
                                            "If you want to know all of the supported mathematical expressions then please check out the documentation for \n" +
                                            "https://github.com/notKamui/Keval"
                                )

                                Text(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    text = "Image Masking",
                                    style = MaterialTheme.typography.h4
                                )
                                Text(
                                    text = "You can add a color mask to any image element by clicking the palette icon in the element option buttons.\n\n" +
                                            "In order to mask specific parts of an image, provide a url to a version of that image that only has this part showing.\n\n" +
                                            "With the mask layer in place, you can now use the color picker to recolor this part."
                                )

                                Text(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    text = "Anchor",
                                    style = MaterialTheme.typography.h4
                                )
                                Text(
                                    text = "The diagonal arrow buttons at the top-right of every element represent that elements anchor point.\n\n" +
                                            "The anchor point is the point that will be placed at exactly the Offset X/Offset Y location.\n\n" +
                                            "The anchor point is also the point from which automatic dimensions are determined, if the dimensions are set to 0."
                                )

                                Text(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    text = "Corners",
                                    style = MaterialTheme.typography.h4
                                )
                                Text(
                                    text = "The 'Corners' button underneath the card preview will round the corners of the preview window. Your final PNG export render will still always output a rectangular image, since most media prefer to round the corners themselves."
                                )

                                Text(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    text = "Bleed",
                                    style = MaterialTheme.typography.h4
                                )
                                Text(
                                    text = "The 'Bleed' button underneath the card preview will allow you to add a bleed zone to your card. These are the colored borders that you might have seen on a lot of trading cards before.\n\n" +
                                            "Bleed zones are a demand for the printing process, and it serves to give the printers some leeway in where they cut the printed cards.\n\n" +
                                            "The automatically generated bleed zone is sized to be 2 millimeters, which is the standardized norm for playing cards."
                                )
                            }
                        }
                    }

                    Row {
                        Box {
                            Column(modifier = Modifier.fillMaxWidth().align(Alignment.Center)) {
                                Icon(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    imageVector = Icons.Filled.ArrowDownward,
                                    contentDescription = "Scrollable Indicator"
                                )
                            }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Button(modifier = Modifier
                                    .height(50.dp)
                                    .align(Alignment.End),
                                    onClick = {
                                        showWindow.value = false
                                    }) {
                                    Text(text = "OK")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}