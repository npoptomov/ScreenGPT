package com.screengpt.overlay.bridge

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ChatGPTBridgeHelper(private val context: Context) {


    suspend fun bridgeToChatGPT(
        bitmap: Bitmap,
        presetPrompt: String = "",
        launchActivity: ((Intent) -> Unit)? = null
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
            val launched = withContext(Dispatchers.Main) {
                launchChatGPTWithImage(targetUri, presetPrompt, launchActivity)
            }
            
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    if (launched) "Screenshot shared with ChatGPT." else "Screenshot saved. Install or update ChatGPT to receive images.",
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

    private fun launchChatGPTWithImage(imageUri: Uri, presetPrompt: String, launchActivity: ((Intent) -> Unit)?): Boolean {
        val chatGptPackage = "com.openai.chatgpt"

        // Build ACTION_SEND intent with image attachment
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            setPackage(chatGptPackage)
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            clipData = ClipData(ClipDescription("Screenshot", arrayOf("image/jpeg")), ClipData.Item(imageUri))
            if (presetPrompt.isNotBlank()) {
                putExtra(Intent.EXTRA_TEXT, presetPrompt)
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }

        return try {
            val receiver = sendIntent.resolveActivity(context.packageManager) ?: return false
            sendIntent.component = receiver
            if (launchActivity != null) launchActivity(sendIntent) else context.startActivity(sendIntent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
