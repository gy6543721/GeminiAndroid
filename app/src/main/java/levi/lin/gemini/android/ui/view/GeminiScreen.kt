package levi.lin.gemini.android.ui.view

import android.graphics.Bitmap
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import levi.lin.gemini.android.viewmodel.GeminiViewModel
import levi.lin.gemini.android.state.MessageState
import levi.lin.gemini.android.ui.view.component.InputBar
import levi.lin.gemini.android.ui.view.component.MessageList

@Composable
internal fun GeminiScreenContainer(
    geminiViewModel: GeminiViewModel = viewModel(),
    onImageSelected: () -> Unit = {}
) {
    val messageState by geminiViewModel.messageState.collectAsState()
    val selectedImageList by geminiViewModel.selectedImageList.collectAsState()
    val selectedImageCount by geminiViewModel.selectedImageCount.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedImageList) {
        val targetModelName =
            if (selectedImageList.isNotEmpty()) "gemini-pro-vision" else "gemini-pro"
        geminiViewModel.updateGenerativeModel(targetModelName = targetModelName)
    }

    LaunchedEffect(Unit) {
        geminiViewModel.scrollToLatestMessageEvent.collect {
            coroutineScope.launch {
                listState.animateScrollToItem(index = messageState.messageItems.size - 1)
            }
        }
    }

    GeminiScreen(
        coroutineScope = coroutineScope,
        messageState = messageState,
        listState = listState,
        selectedImageBitmapList = selectedImageList,
        selectedImageCount = selectedImageCount,
        onButtonClicked = { inputText ->
            geminiViewModel.sendMessage(inputText)
        },
        onImageSelected = onImageSelected,
        onClearImage = {
            geminiViewModel.clearImageList()
        }
    )
}

@Composable
fun GeminiScreen(
    coroutineScope: CoroutineScope,
    messageState: MessageState,
    listState: LazyListState,
    selectedImageBitmapList: List<Bitmap?> = emptyList(),
    selectedImageCount: Int = 0,
    onButtonClicked: (String) -> Unit = {},
    onImageSelected: () -> Unit = {},
    onClearImage: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf(value = "") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
            },
        bottomBar = {
            InputBar(
                inputText = inputText,
                selectedImageBitmapList = selectedImageBitmapList,
                selectedImageCount = selectedImageCount,
                onTextChange = { text ->
                    inputText = text
                },
                onButtonClicked = onButtonClicked,
                onImageSelected = onImageSelected,
                onClearImage = onClearImage,
                onResetScroll = {
                    coroutineScope.launch {
                        listState.scrollToItem(index = messageState.messageItems.size - 1)
                    }
                }
            )
        }
    ) { innerPadding ->
        MessageList(
            messages = messageState.messageItems,
            listState = listState,
            innerPadding = innerPadding
        )
    }
}