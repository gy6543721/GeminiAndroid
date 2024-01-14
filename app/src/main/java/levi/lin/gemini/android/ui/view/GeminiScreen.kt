package levi.lin.gemini.android.ui.view

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.ai.client.generativeai.GenerativeModel
import levi.lin.gemini.android.BuildConfig
import levi.lin.gemini.android.ui.state.GeminiUiState
import levi.lin.gemini.android.viewmodel.GeminiViewModel
import levi.lin.gemini.android.R
import levi.lin.gemini.android.ui.theme.LightBlue80

@Composable
internal fun GeminiScreenContainer(
    geminiViewModel: GeminiViewModel = viewModel(),
    onImageSelected: () -> Unit = {}
) {
    val geminiUiState by geminiViewModel.uiState.collectAsState()
    val selectedImageBitmapList by geminiViewModel.selectedImageBitmaps.collectAsState()
    val selectedImageCount by geminiViewModel.selectedImageCount.collectAsState()

    LaunchedEffect(selectedImageBitmapList) {
        val targetModelName =
            if (selectedImageBitmapList.isNotEmpty()) "gemini-pro-vision" else "gemini-pro"
        geminiViewModel.updateGenerativeModel(GenerativeModel(targetModelName, BuildConfig.apiKey))
    }

    GeminiScreen(
        uiState = geminiUiState,
        selectedImageBitmapList = selectedImageBitmapList,
        selectedImageCount = selectedImageCount,
        onButtonClicked = { inputText ->
            geminiViewModel.respond(inputText)
        },
        onImageSelected = onImageSelected,
        onClearImages = {
            geminiViewModel.clearSelectedImages()
        }
    )
}

@Composable
fun GeminiScreen(
    uiState: GeminiUiState = GeminiUiState.Initial,
    selectedImageBitmapList: List<Bitmap?> = emptyList(),
    selectedImageCount: Int = 0,
    onButtonClicked: (String) -> Unit = {},
    onImageSelected: () -> Unit = {},
    onClearImages: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf(value = "") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        bottomBar = {
            InputBar(
                inputText = inputText,
                selectedImageBitmapList = selectedImageBitmapList,
                selectedImageCount = selectedImageCount,
                onTextChange = { inputText = it },
                onButtonClicked = onButtonClicked,
                onImageSelected = onImageSelected,
                onClearImages = onClearImages
            )
        }
    ) { innerPadding ->
        ScreenContent(uiState, innerPadding)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputBar(
    inputText: String,
    selectedImageBitmapList: List<Bitmap?>,
    selectedImageCount: Int,
    onTextChange: (String) -> Unit,
    onButtonClicked: (String) -> Unit,
    onImageSelected: () -> Unit,
    onClearImages: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = Modifier
            .background(color = LightBlue80)
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
        Spacer(modifier = Modifier.weight(0.02f))
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
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.Red),
                            contentAlignment = Alignment.Center
                        ) {
                            val fontSize = when (selectedImageCount.toString().length) {
                                1 -> 12.sp
                                2 -> 10.sp
                                else -> 8.sp
                            }
                            Text(
                                text = selectedImageCount.toString(),
                                color = Color.White,
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
                    onClick = { onClearImages() },
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
            },
            modifier = Modifier
                .weight(0.2f)
                .aspectRatio(ratio = 1f)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ScreenContent(uiState: GeminiUiState, innerPadding: PaddingValues) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.padding(innerPadding)) {
        when (uiState) {
            is GeminiUiState.Success -> {
                Box(modifier = Modifier.verticalScroll(scrollState)) {
                    SelectionContainer {
                        Text(
                            text = uiState.outputText.trim(),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            is GeminiUiState.Loading -> {
                LoadingIndicator()
            }

            is GeminiUiState.Error -> {
                ErrorMessage(uiState.errorMessage)
            }

            else -> Spacer(modifier = Modifier.fillMaxHeight())
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = Color.Red)
    }
}
