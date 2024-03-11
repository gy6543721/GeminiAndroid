package levi.lin.gemini.android.ui.view.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import levi.lin.gemini.android.R

@Composable
fun InputBar(
    inputText: String,
    selectedImageBitmapList: List<Bitmap?>,
    selectedImageCount: Int,
    onTextChange: (String) -> Unit,
    onButtonClicked: (String) -> Unit,
    onImageSelected: () -> Unit,
    onClearImage: () -> Unit,
    onResetScroll: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.primary)
            .fillMaxWidth()
            .padding(all = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = inputText,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            label = { Text(text = stringResource(id = R.string.gemini_input_label)) },
            placeholder = { Text(text = stringResource(id = R.string.gemini_input_hint)) },
            trailingIcon = {
                if (inputText.isNotEmpty()) {
                    IconButton(onClick = {
                        onTextChange("")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            }
        )
        Spacer(modifier = Modifier.weight(0.05f))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RectangleShape),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.BottomStart),
                onClick = { onImageSelected() }
            ) {
                if (selectedImageBitmapList.isNotEmpty()) {
                    selectedImageBitmapList.first()?.let { image ->
                        Image(
                            bitmap = image.asImageBitmap(),
                            modifier = Modifier.fillMaxSize(),
                            contentDescription = "Selected Image",
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (selectedImageCount > 1) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            val fontSize = when (selectedImageCount.toString().length) {
                                1 -> 10.sp
                                2 -> 8.sp
                                else -> 6.sp
                            }
                            Text(
                                text = selectedImageCount.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = fontSize,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Select Image",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (selectedImageBitmapList.isNotEmpty()) {
                IconButton(
                    onClick = { onClearImage() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(15.dp)
                        .background(Color.Gray)
                        .align(Alignment.TopEnd),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Images",
                        tint = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(0.01f))
        IconButton(
            onClick = {
                onButtonClicked(inputText)
                keyboardController?.hide()
                onResetScroll()
            },
            modifier = Modifier
                .weight(0.2f)
                .aspectRatio(ratio = 1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}