import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    val cardElements: MutableList<CardElement> = mutableListOf()

    MaterialTheme {

    }

    Box(
        modifier = Modifier
    ) {
        // Background Image
        Image(
            painterResource(DrawableResource("background.jpg")),
            contentDescription = "backgroundImage",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Main UI
        Row(
            modifier = Modifier
                .padding(48.dp)
        ) {
            // Config column
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            topStartPercent = 2,
                            topEndPercent = 2,
                            bottomStartPercent = 2,
                            bottomEndPercent = 2
                        )
                    )
                    .background(Color.White.copy(alpha = 0.9f))
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(
                            topStartPercent = 2,
                            topEndPercent = 2,
                            bottomStartPercent = 2,
                            bottomEndPercent = 2
                        )
                    )
            ) {
                // Main button row
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 32.dp)
                        .wrapContentHeight()
                ) {
                    Button(
                        onClick = {
                            cardElements.add(TextElement())
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text("Add Text")
                    }
                    Button(
                        onClick = {
                            cardElements.add(ImageElement())
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text("Add Image")
                    }
                }

                // Line
                Row(
                    modifier = Modifier
                        .padding(vertical = 0.dp, horizontal = 16.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth()) {
                        drawLine(
                            start = Offset(x = 0f, y = 0f),
                            end = Offset(x = size.width, y = 0f),
                            color = Color.Black.copy(alpha = 0.8f),
                            strokeWidth = 1f
                        )
                    }
                }

                // Parameters
                var paramtersFolded by remember { mutableStateOf(false) } // Initial state
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    // Folding button
                    Button(
                        onClick = {
                            paramtersFolded = !paramtersFolded
                        },
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text("Parameters")
                    }
                }
                if (!paramtersFolded) {
                    // TODO: This should be added for every parameter
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(50.dp)
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                Color.Black.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(
                                    topStartPercent = 5,
                                    topEndPercent = 5,
                                    bottomStartPercent = 5,
                                    bottomEndPercent = 5
                                )
                            )
                    ) {
                        // TODO: Parameter elements
                    }
                }

                // Parameters
                var advancedFolded by remember { mutableStateOf(false) } // Initial state
                Row(modifier = Modifier) {
                    // Folding button
                    Button(
                        onClick = {
                            advancedFolded = !advancedFolded
                        },
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text("Advanced")
                    }
                }
                if (!advancedFolded) {
                    // TODO: This should be added for every parameter
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(50.dp)
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                Color.Black.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(
                                    topStartPercent = 5,
                                    topEndPercent = 5,
                                    bottomStartPercent = 5,
                                    bottomEndPercent = 5
                                )
                            )
                    ) {
                        // TODO: Advanced elements
                    }
                }
            }

            // Card Preview Column
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .padding(start = 16.dp)
            ) {
                // Card preview
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .weight(0.9f)
                ) {
                    CardPreview(
                        modifier = Modifier
                    )
                }
                // Download buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .weight(0.1f)
                ) {
                    Button(
                        onClick = {
                            // Render image and download png
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
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
            //.size(width = 750.dp, height = 1050.dp)
            .aspectRatio(2.5f / 3.5f)
            .clip(
                RoundedCornerShape(
                    topStartPercent = 10,
                    topEndPercent = 10,
                    bottomStartPercent = 10,
                    bottomEndPercent = 10
                )
            )
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