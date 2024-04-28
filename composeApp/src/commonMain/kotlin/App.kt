import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme {
        val cardElements: MutableList<CardElement> = mutableListOf()

        // Main button row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            Button(
                onClick = {
                    cardElements.add(TextElement())
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Add Text")
            }
            Button(
                onClick = {
                    cardElements.add(ImageElement())
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Add Image")
            }
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Config column
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.65f)
                    .padding(vertical =16.dp, horizontal = 16.dp)
            ) {

            }

            // Card Preview Column
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.3f)
                    .padding(vertical =16.dp, horizontal = 16.dp)
            ) {
                // Card preview
                Row(
                    modifier = Modifier
                        .weight(0.9f)
                        .align(Alignment.CenterHorizontally)
                ) {
                    CardPreview(
                        modifier = Modifier
                    )
                }
                // Download buttons
                Row(
                    modifier = Modifier
                        .weight(0.1f)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick = {
                            // Render image and download png
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("Export as PNG")
                    }
                }
            }
        }
    }
}

@Composable
fun CardPreview(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 750.dp, height = 1050.dp)
            // This is 2.5 inches by 3.5 inches, at 300 dpi
            //.aspectRatio(ratio = 2.5f / 3.5f)
            .padding(vertical =16.dp, horizontal = 16.dp)
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(
                    topStartPercent = 10,
                    topEndPercent = 10,
                    bottomStartPercent = 10,
                    bottomEndPercent = 10
                )
            )
    )
}