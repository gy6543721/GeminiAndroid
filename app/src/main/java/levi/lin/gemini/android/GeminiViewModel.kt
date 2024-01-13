package levi.lin.gemini.android

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class GeminiViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _uiState: MutableStateFlow<GeminiUiState> =
        MutableStateFlow(GeminiUiState.Initial)
    val uiState: StateFlow<GeminiUiState> =
        _uiState.asStateFlow()

    private val _selectedImageBitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val selectedImageBitmaps: StateFlow<List<Bitmap>> = _selectedImageBitmaps.asStateFlow()

    private val _selectedImageCount = MutableStateFlow(0)
    val selectedImageCount: StateFlow<Int> = _selectedImageCount.asStateFlow()

    fun respond(inputText: String) {
        _uiState.value = GeminiUiState.Loading

        val deviceLanguage = Locale.getDefault().displayLanguage
        val textPrompt =
            "According to the contents provided as below: [$inputText], "
        val imagePrompt =
            "According to the images and contents: [$inputText] provided, "
        val generalPrompt =
            "respond with detail answers or advices in $deviceLanguage as if you are the speaker's special one."
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
}