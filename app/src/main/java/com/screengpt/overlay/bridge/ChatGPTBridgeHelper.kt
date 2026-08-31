package com.screengpt.overlay.bridge

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class ChatGPTBridgeHelper(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun bridgeToChatGPT(
        bitmap: Bitmap,
        presetPrompt: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Save screenshot to cache file
            val imageFile = File(context.cacheDir, "screen_capture.jpg")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }

            // 2. Obtain FileProvider content URI
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "com.screengpt.overlay.fileprovider",
                imageFile
            )

            // 3. Save to MediaStore (so it's registered as the latest gallery item)
            var mediaStoreUri: Uri? = null
            try {
                mediaStoreUri = saveToMediaStore(bitmap)
            } catch (e: Exception) {
                // Ignore
            }

            val targetUri = mediaStoreUri ?: contentUri

            // 4. Extract text from screen via on-device ML Kit OCR
            var extractedText = ""
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val task = recognizer.process(inputImage)
                val visionText = Tasks.await(task, 1500, TimeUnit.MILLISECONDS)
                extractedText = visionText.text.trim()
            } catch (e: Exception) {
                // Ignore
            }

            // 5. Copy image directly to clipboard with explicit image MIME type for Gboard / Samsung keyboard
            withContext(Dispatchers.Main) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                
                val clip = ClipData(
                    ClipDescription("Screen Capture", arrayOf("image/jpeg", "image/png", "text/plain")),
                    ClipData.Item(targetUri)
                )
                clipboard.setPrimaryClip(clip)
            }

            // 6. Grant URI permission to ChatGPT package
            val chatGptPackage = "com.openai.chatgpt"
            try {
                context.grantUriPermission(
                    chatGptPackage,
                    targetUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                context.grantUriPermission(
                    chatGptPackage,
                    contentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore
            }

            // 7. Launch ChatGPT with image attachment intent
            val launched = launchChatGPTWithImage(targetUri, presetPrompt)
            
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "📸 Screenshot captured & copied to clipboard!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            launched
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error bridging to ChatGPT: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
            false
        }
    }

    private fun saveToMediaStore(bitmap: Bitmap): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "ScreenGPT_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ScreenGPT")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null
        resolver.openOutputStream(imageUri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)
        }

        return imageUri
    }

    private fun launchChatGPTWithImage(imageUri: Uri, presetPrompt: String): Boolean {
        val chatGptPackage = "com.openai.chatgpt"
        val mainActivityName = "com.openai.chatgpt.MainActivity"

        // Build ACTION_SEND intent with image attachment
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            component = ComponentName(chatGptPackage, mainActivityName)
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            clipData = ClipData(ClipDescription("Screenshot", arrayOf("image/jpeg")), ClipData.Item(imageUri))
            if (presetPrompt.isNotBlank()) {
                putExtra(Intent.EXTRA_TEXT, presetPrompt)
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }

        // Method 1: PendingIntent send
        try {
            val pendingIntent = PendingIntent.getActivity(
                context,
                1003,
                sendIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            pendingIntent.send()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Method 2: Direct startActivity
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val options = ActivityOptions.makeBasic().apply {
                    setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                }
                context.startActivity(sendIntent, options.toBundle())
            } else {
                context.startActivity(sendIntent)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }
}
