package com.screengpt.overlay

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Screenshot access only: no UI-tree reading, gestures, key filtering or event monitoring. */
class ScreenCaptureAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() { instance = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    @RequiresApi(30)
    suspend fun capture(): Bitmap = suspendCancellableCoroutine { continuation ->
        if (getSystemService(KeyguardManager::class.java).isKeyguardLocked) {
            continuation.resumeWithException(IllegalStateException("Unlock your phone first."))
            return@suspendCancellableCoroutine
        }
        takeScreenshot(Display.DEFAULT_DISPLAY, mainExecutor, object : TakeScreenshotCallback {
            override fun onSuccess(result: ScreenshotResult) {
                val buffer = result.hardwareBuffer
                try {
                    if (!continuation.isActive) return
                    val hardwareBitmap = Bitmap.wrapHardwareBuffer(buffer, result.colorSpace)
                        ?: error("Android returned an unreadable screenshot.")
                    val bitmap = try {
                        hardwareBitmap.copy(Bitmap.Config.ARGB_8888, false)
                            ?: error("Could not copy the screenshot.")
                    } finally { hardwareBitmap.recycle() }
                    continuation.resume(bitmap)
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resumeWithException(e)
                } finally { buffer.close() }
            }
            override fun onFailure(errorCode: Int) {
                val message = when (errorCode) {
                    ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT -> "Please wait a moment before capturing again."
                    ERROR_TAKE_SCREENSHOT_NO_ACCESSIBILITY_ACCESS -> "Enable ScreenGPT screen capture in Accessibility settings."
                    6 -> "This app protects its screen and cannot be captured."
                    else -> "Android could not capture this screen (error $errorCode)."
                }
                if (continuation.isActive) continuation.resumeWithException(IllegalStateException(message))
            }
        })
    }

    companion object {
        var instance: ScreenCaptureAccessibilityService? = null
            private set

        fun isEnabled(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < 30) return false
            val component = ComponentName(context, ScreenCaptureAccessibilityService::class.java)
            return context.getSystemService(AccessibilityManager::class.java)
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                .any { ComponentName(it.resolveInfo.serviceInfo.packageName, it.resolveInfo.serviceInfo.name) == component }
        }
    }
}
