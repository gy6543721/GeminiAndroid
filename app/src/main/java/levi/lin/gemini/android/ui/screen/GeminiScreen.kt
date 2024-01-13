package levi.lin.gemini.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import levi.lin.gemini.android.GeminiUiState
import levi.lin.gemini.android.GeminiViewModel
import levi.lin.gemini.android.R
import levi.lin.gemini.android.ui.theme.LightBlue80

@Composable
internal fun GeminiScreenContainer(
    geminiViewModel: GeminiViewModel = viewModel()
) {
    val geminiUiState by geminiViewModel.uiState.collectAsState()

    GeminiScreen(
        uiState = geminiUiState,
        onButtonClicked = { inputText ->
            geminiViewModel.respond(inputText)
        }
    )
}

@Composable
fun GeminiScreen(
    uiState: GeminiUiState = GeminiUiState.Initial,
    onButtonClicked: (String) -> Unit = {}
) {
    var inputText by remember { mutableStateOf(value = "") }

    Scaffold(
        modifier = Modifier.imePadding(),
        bottomBar = {
            InputBar(
                inputText = inputText,
                onTextChange = { inputText = it },
                onButtonClicked = onButtonClicked
            )
        }
    ) { innerPadding ->
        ScreenContent(uiState, innerPadding)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputBar(inputText: String, onTextChange: (String) -> Unit, onButtonClicked: (String) -> Unit) {
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
                    IconButton(onClick = { onTextChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            }
        )
        Spacer(modifier = Modifier.weight(0.05f))
        Button(
            onClick = {
                onButtonClicked(inputText)
                keyboardController?.hide()
            },
        ) {
            Text(text = stringResource(id = R.string.action_go))
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
                            text = uiState.outputText,
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

@Composable
@Preview(showSystemUi = true)
fun GeminiScreenPreview() {
    GeminiScreen()
}