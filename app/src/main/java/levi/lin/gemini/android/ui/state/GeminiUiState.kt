package levi.lin.gemini.android.ui.state

sealed interface GeminiUiState {
    data object Initial : GeminiUiState

    data object Loading : GeminiUiState

    data class Success(
        val outputText: String
    ) : GeminiUiState

    data class Error(
        val errorMessage: String
    ) : GeminiUiState
}