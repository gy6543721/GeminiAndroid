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

    private val _selectedImageBitmapList = MutableStateFlow<List<Bitmap>>(emptyList())
    val selectedImageBitmapList: StateFlow<List<Bitmap>> = _selectedImageBitmapList.asStateFlow()

    private val _selectedImageCount = MutableStateFlow(value = 0)
    val selectedImageCount: StateFlow<Int> = _selectedImageCount.asStateFlow()

    private val _generativeModelFlow = MutableSharedFlow<GenerativeModel>()
    val generativeModelFlow: SharedFlow<GenerativeModel> = _generativeModelFlow.asSharedFlow()

    fun respond(inputText: String) {
        _uiState.value = GeminiUiState.Loading

        val deviceLanguage = Locale.getDefault().displayLanguage
        val prompt =
            "$inputText (respond in $deviceLanguage)"
        val imageList = selectedImageBitmapList.value
        val inputContent = content {
            if (imageList.isNotEmpty()) {
                imageList.forEach { image ->
                    image(image = image)
                }
            }
            text(text = prompt)
        }

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(inputContent)
                response.text?.let { outputContent ->
                    _uiState.value = GeminiUiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = GeminiUiState.Error(errorMessage = e.localizedMessage ?: "")
            }
        }
    }

    fun setImageBitmapList(imageList: List<Bitmap>) {
        _selectedImageBitmapList.value = imageList
    }

    fun setImageCount(count: Int) {
        _selectedImageCount.value = count
    }

    fun clearSelectedImages() {
        _selectedImageBitmapList.value = emptyList()
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