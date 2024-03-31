package levi.lin.gemini.android.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import levi.lin.gemini.android.BuildConfig
import levi.lin.gemini.android.model.MessageItem
import levi.lin.gemini.android.model.MessageType
import levi.lin.gemini.android.state.MessageState
import java.util.Locale

class GeminiViewModel(
    private var generativeModel: GenerativeModel
) : ViewModel() {
    private val _selectedImageList = MutableStateFlow<List<Bitmap>>(emptyList())
    val selectedImageList: StateFlow<List<Bitmap>> = _selectedImageList.asStateFlow()

    private val _selectedImageCount = MutableStateFlow(value = 0)
    val selectedImageCount: StateFlow<Int> = _selectedImageCount.asStateFlow()

    private val _generativeModelFlow = MutableSharedFlow<GenerativeModel>()
    val generativeModelFlow: SharedFlow<GenerativeModel> = _generativeModelFlow.asSharedFlow()

    private val _scrollToLatestMessageEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToLatestMessageEvent: SharedFlow<Unit> = _scrollToLatestMessageEvent.asSharedFlow()

    // Chat with Gemini
    private var chat = generativeModel.startChat(history = emptyList())

    private val _messageState: MutableStateFlow<MessageState> =
        MutableStateFlow(
            MessageState(
                chat.history.map { content ->
                    MessageItem(
                        text = content.parts.first().asTextOrNull() ?: "",
                        type = if (content.role == "user") MessageType.User else MessageType.Gemini,
                        isPending = false
                    )
                }
            )
        )
    val messageState: StateFlow<MessageState> = _messageState.asStateFlow()

    fun sendMessage(inputMessage: String) {
        val deviceLanguage = Locale.getDefault().displayLanguage
        val imageList = selectedImageList.value
        val displayMessage = when {
            imageList.isNotEmpty() && inputMessage.isNotBlank() -> "$inputMessage\n\uD83D\uDDBC\uFE0F ：${_selectedImageCount.value}"
            imageList.isNotEmpty() -> "\uD83D\uDDBC\uFE0F ：${_selectedImageCount.value}"
            else -> inputMessage
        }
        val prompt = if (imageList.isNotEmpty() && inputMessage.isBlank()) {
            "Describe all the images provided."
        } else {
            "$inputMessage (respond in $deviceLanguage)"
        }

        val inputContent = content(role = "user") {
            if (imageList.isNotEmpty()) {
                imageList.forEach { image ->
                    image(image = image)
                }
            }
            text(text = prompt)
        }

        // User's message
        val inputMessageItem = MessageItem(
            text = displayMessage.trim(),
            type = MessageType.User,
            isPending = true
        )
        _messageState.value.addMessage(message = inputMessageItem)

        viewModelScope.launch {
            try {
                val response = if (imageList.isNotEmpty()) {
                    generativeModel.generateContent(inputContent)
                } else {
                    chat.sendMessage(prompt = inputContent)
                }
                _messageState.value.replaceLastMessage()

                response.text?.let { outputContent ->
                    _messageState.value.addMessage(
                        message = MessageItem(
                            text = outputContent.trim(),
                            type = MessageType.Gemini,
                            isPending = false
                        )
                    )
                }
            } catch (e: Exception) {
                _messageState.value.replaceLastMessage()
                _messageState.value.addMessage(
                    message = MessageItem(
                        text = e.localizedMessage ?: "",
                        type = MessageType.Error,
                        isPending = false
                    )
                )
            }
            _messageState.value.updateMessagePendingStatus(messageId = inputMessageItem.id, isPending = false)
            _scrollToLatestMessageEvent.emit(Unit)
        }
    }

    fun setImageList(imageList: List<Bitmap>) {
        _selectedImageList.value = imageList
    }

    fun setImageCount(count: Int) {
        _selectedImageCount.value = count
    }

    fun clearImageList() {
        _selectedImageList.value = emptyList()
        _selectedImageCount.value = 0
    }

    suspend fun updateGenerativeModel(targetModelName: String) {
        if (generativeModel.modelName != targetModelName) {
            val currentHistory = getCurrentHistory()

            generativeModel = GenerativeModel(
                modelName = targetModelName,
                apiKey = BuildConfig.apiKey
            )

            restartChatWithHistory(currentHistory)
            _generativeModelFlow.emit(generativeModel)
        }
    }

    private fun getCurrentHistory(): List<Content> {
        return _messageState.value.messageItems.map { messageItem ->
            content(role = if (messageItem.type == MessageType.User) "user" else "model") {
                text(messageItem.text)
            }
        }
    }

    private fun restartChatWithHistory(history: List<Content>) {
        chat = generativeModel.startChat(history = history)
    }
}