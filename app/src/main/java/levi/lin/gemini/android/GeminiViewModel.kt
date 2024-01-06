package levi.lin.gemini.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
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

    fun respond(inputText: String) {
        _uiState.value = GeminiUiState.Loading

        val deviceLanguage = Locale.getDefault().displayLanguage
        val prompt =
            "Read the following content, and respond with detail answer in $deviceLanguage as if you are the speaker's special one: $inputText"

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(prompt)
                response.text?.let { outputContent ->
                    _uiState.value = GeminiUiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = GeminiUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}