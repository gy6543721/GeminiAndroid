package levi.lin.gemini.android.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import levi.lin.gemini.android.BuildConfig
import levi.lin.gemini.android.ui.state.GeminiUiState
import java.util.Locale

class GeminiViewModel(
    private var generativeModel: GenerativeModel
) : ViewModel() {

    private val _uiState: MutableStateFlow<GeminiUiState> =
        MutableStateFlow(GeminiUiState.Initial)
    val uiState: StateFlow<GeminiUiState> =
        _uiState.asStateFlow()

    private val _selectedImageBitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val selectedImageBitmaps: StateFlow<List<Bitmap>> = _selectedImageBitmaps.asStateFlow()

    private val _selectedImageCount = MutableStateFlow(0)
    val selectedImageCount: StateFlow<Int> = _selectedImageCount.asStateFlow()

    private val _generativeModelFlow = MutableSharedFlow<GenerativeModel>()
    val generativeModelFlow: SharedFlow<GenerativeModel> = _generativeModelFlow.asSharedFlow()

    fun respond(inputText: String) {
        _uiState.value = GeminiUiState.Loading

        val deviceLanguage = Locale.getDefault().displayLanguage
        val textPrompt =
            "Content:($inputText) \\n You are a lovely assistant. According to the provided content, "
        val imagePrompt =
            "Content:($inputText) \\n You are a lovely assistant. According to the provided images and content, "
        val generalPrompt =
            "if the content is a question, answer the question in $deviceLanguage. If the content is a request, respond with detailed information in $deviceLanguage."
        val imageList = selectedImageBitmaps.value
        val inputContent = content {
            if (imageList.isNotEmpty()) {
                imageList.forEach { image ->
                    image(image = image)
                }
                text(text = imagePrompt + generalPrompt)
            } else {
                text(text = textPrompt + generalPrompt)
            }
        }

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(inputContent)
                response.text?.let { outputContent ->
                    _uiState.value = GeminiUiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = GeminiUiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    fun setImageBitmaps(images: List<Bitmap>) {
        _selectedImageBitmaps.value = images
    }

    fun setImageCount(count: Int) {
        _selectedImageCount.value = count
    }

    fun clearSelectedImages() {
        _selectedImageBitmaps.value = emptyList()
        _selectedImageCount.value = 0
    }

    suspend fun updateGenerativeModel(targetModelName: String) {
        if (generativeModel.modelName != targetModelName) {
            generativeModel = GenerativeModel(
                modelName = targetModelName,
                apiKey = BuildConfig.apiKey
            )
            _generativeModelFlow.emit(generativeModel)
        }
    }
}