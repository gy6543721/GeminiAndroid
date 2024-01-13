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
import com.google.ai.client.generativeai.GenerativeModel
import levi.lin.gemini.android.ui.screen.GeminiScreenContainer
import levi.lin.gemini.android.ui.theme.GeminiAndroidTheme

class MainActivity : ComponentActivity() {
    // Gemini AI Model
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro-vision",
        apiKey = BuildConfig.apiKey
    )
    private val viewModel = GeminiViewModel(generativeModel)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private val selectImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageBitmaps = mutableListOf<Bitmap>()
                val clipData = data?.clipData
                if (clipData != null) {
                    // Multiple Images
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        val bitmap = getBitmapFromUri(uri)
                        imageBitmaps.add(bitmap)
                    }
                    viewModel.setImageCount(clipData.itemCount)
                } else {
                    // Single Image
                    data?.data?.let { uri ->
                        val bitmap = getBitmapFromUri(uri)
                        imageBitmaps.add(bitmap)
                    }
                    viewModel.setImageCount(1)
                }
                viewModel.setImageBitmaps(imageBitmaps)
            }
        }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        selectImageResult.launch(intent)
    }
}