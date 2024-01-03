package levi.lin.gemini.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.background(color = LightBlue80)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 10.dp)
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text(text = stringResource(R.string.gemini_input_label)) },
                        placeholder = { Text(text = stringResource(R.string.gemini_input_hint)) },
                        trailingIcon = {
                            if (inputText.isNotEmpty()) {
                                IconButton(onClick = { inputText = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        },
                    )
                    Spacer(Modifier.weight(0.05f))
                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onButtonClicked(inputText)
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text(text = stringResource(R.string.action_go))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (uiState) {
                is GeminiUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    ) {
                        items(uiState.outputText.lines()) { line ->
                            Text(
                                text = line,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }

                is GeminiUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                is GeminiUiState.Error -> {
                    Text(
                        text = uiState.errorMessage,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {
                    Spacer(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun GeminiScreenPreview() {
    GeminiScreen()
}