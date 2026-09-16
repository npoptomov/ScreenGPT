package com.screengpt.overlay

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.screengpt.overlay.bridge.ChatGPTBridgeHelper
import kotlinx.coroutines.*

open class CapturePermissionActivity : AppCompatActivity() {
    private var waitingForConsent = false
    private var ownsCapture = false
    private val captureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        waitingForConsent = false
        if (result.resultCode != Activity.RESULT_OK || result.data == null) {
            finish()
        } else {
            FloatingWidgetService.onCapturePermissionReceived(this, result.resultCode, result.data!!)
            lifecycleScope.launch {
                try {
                    withTimeout(5000) {
                        while (!FloatingWidgetService.hasCapturePermission()) delay(50)
                    }
                    if (intent.getBooleanExtra(EXTRA_CAPTURE_AFTER_GRANT, false)) shareProjection()
                } catch (e: TimeoutCancellationException) {
                    showError("Screen capture could not start. Please try again.")
                } finally { finish() }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getSystemService(KeyguardManager::class.java).isKeyguardLocked) {
            showError("Unlock your phone before capturing the screen.")
            finish()
            return
        }
        waitingForConsent = savedInstanceState?.getBoolean("waiting_for_consent") ?: false
        if (waitingForConsent) return
        val captureNow = intent.getBooleanExtra(EXTRA_CAPTURE_AFTER_GRANT, false)
        if (captureNow) {
            ownsCapture = requestGate.acquire()
            if (!ownsCapture) { finish(); return }
        }
        if (captureNow && Build.VERSION.SDK_INT >= 30 && ScreenCaptureAccessibilityService.isEnabled(this)) {
            // A foreground transparent activity can launch ChatGPT without overlay permission.
            lifecycleScope.launch {
                try {
                    val service = withTimeout(3000) {
                        while (ScreenCaptureAccessibilityService.instance == null) delay(50)
                        ScreenCaptureAccessibilityService.instance!!
                    }
                    FloatingWidgetService.stop(this@CapturePermissionActivity)
                    delay(350)
                    val bitmap = withTimeout(5000) { service.capture() }
                    try {
                        ChatGPTBridgeHelper(this@CapturePermissionActivity).bridgeToChatGPT(bitmap, launchActivity = { startActivity(it) })
                    } finally { bitmap.recycle() }
                } catch (e: TimeoutCancellationException) {
                    showError("Screen capture service is not responding. Re-enable it in Accessibility settings.")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    showError(e.message ?: "Screenshot failed.")
                } finally { finish() }
            }
        } else if (FloatingWidgetService.hasCapturePermission()) {
            lifecycleScope.launch {
                try { if (captureNow) shareProjection() } finally { finish() }
            }
        } else if (Build.VERSION.SDK_INT >= 30 && !intent.getBooleanExtra(EXTRA_TEMPORARY_SESSION, false)) {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        } else {
            try {
                val manager = getSystemService(MediaProjectionManager::class.java)
                val consent = if (Build.VERSION.SDK_INT >= 34) manager.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay())
                    else manager.createScreenCaptureIntent()
                waitingForConsent = true
                captureLauncher.launch(consent)
            } catch (e: Exception) {
                showError(e.message ?: "Could not request screen capture.")
                finish()
            }
        }
    }

    override fun onDestroy() {
        if (ownsCapture) requestGate.release()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("waiting_for_consent", waitingForConsent)
        super.onSaveInstanceState(outState)
    }

    private suspend fun shareProjection() {
        delay(350)
        val result = FloatingWidgetService.capture()
        result.onSuccess { data ->
            try {
                ChatGPTBridgeHelper(this).bridgeToChatGPT(data.fullBitmap, launchActivity = { startActivity(it) })
            } finally {
                data.fullBitmap.recycle()
                if (data.thumbnailBitmap !== data.fullBitmap) data.thumbnailBitmap.recycle()
            }
        }.onFailure { showError(it.message ?: "Screenshot failed.") }
    }

    private fun showError(message: String) { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }

    companion object {
        private val requestGate = CaptureRequestGate()
        const val EXTRA_CAPTURE_AFTER_GRANT = "capture_after_grant"
        const val EXTRA_TEMPORARY_SESSION = "temporary_session"
        fun requestPermission(context: Context) {
            context.startActivity(Intent(context, CapturePermissionActivity::class.java)
                .putExtra(EXTRA_TEMPORARY_SESSION, true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
