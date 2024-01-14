package levi.lin.gemini.android

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import levi.lin.gemini.android.ui.view.GeminiScreenContainer
import levi.lin.gemini.android.ui.theme.GeminiAndroidTheme
import levi.lin.gemini.android.utility.GenerativeModelListener
import levi.lin.gemini.android.viewmodel.GeminiViewModel
import java.io.IOException

class MainActivity : ComponentActivity(), GenerativeModelListener {
    // Gemini AI Model
    private lateinit var generativeModel: GenerativeModel
    private lateinit var viewModel: GeminiViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init Gemini AI Model
        generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.apiKey
        )
        viewModel = GeminiViewModel(generativeModel = generativeModel)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.modelNameFlow.collect { modelName ->
                    updateGenerativeModel(modelName)
                }
            }
        }

        // init UI
        setContent {
            GeminiAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    GeminiScreenContainer(
                        geminiViewModel = viewModel,
                        onImageSelected = { selectImage() })
                }
            }
        }
    }

    override fun onModelNameChanged(targetModelName: String) {
        generativeModel = GenerativeModel(
            modelName = targetModelName,
            apiKey = BuildConfig.apiKey
        )
        viewModel.updateGenerativeModel(generativeModel)
    }

    private val selectImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmaps = result.data?.let { intent ->
                    extractBitmapsFromIntent(intent = intent)
                }.orEmpty()
                viewModel.clearSelectedImages()
                viewModel.setImageCount(imageBitmaps.size)
                viewModel.setImageBitmaps(imageBitmaps)
            }
        }

    private fun extractBitmapsFromIntent(intent: Intent): List<Bitmap> {
        return intent.clipData?.let { clipData ->
            (0 until clipData.itemCount).mapNotNull { index ->
                getBitmapFromUri(clipData.getItemAt(index).uri)
            }
        } ?: intent.data?.let { uri ->
            listOfNotNull(getBitmapFromUri(uri))
        }.orEmpty()
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: IOException) {
            null
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        selectImageResult.launch(intent)
    }

    private fun updateGenerativeModel(modelName: String) {
        generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = BuildConfig.apiKey
        )
    }
}