package com.screengpt.overlay

import android.app.KeyguardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.widget.Toast
import com.screengpt.overlay.bridge.ChatGPTBridgeHelper
import kotlinx.coroutines.*

class ScreenAssistantSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession = ScreenAssistantSession(this)
}

/** Android supplies the current screen; no microphone or projection session is needed. */
private class ScreenAssistantSession(private val appContext: Context) : VoiceInteractionSession(appContext) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var processing = false
    private var requestedScreenshot = false
    private var timeout: Job? = null

    override fun onPrepareShow(args: Bundle?, showFlags: Int) {
        super.onPrepareShow(args, showFlags)
        setUiEnabled(false)
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        if (appContext.getSystemService(KeyguardManager::class.java).isKeyguardLocked) {
            fail("Unlock your phone before capturing the screen.")
            return
        }
        if (showFlags and SHOW_WITH_SCREENSHOT == 0 && !requestedScreenshot) {
            requestedScreenshot = true
            show(args, showFlags or SHOW_WITH_SCREENSHOT)
        }
        timeout?.cancel()
        timeout = scope.launch {
            delay(10000)
            if (!processing) fail("No screenshot received. Enable screenshot access in your default assistant settings.")
        }
    }

    override fun onHandleScreenshot(screenshot: Bitmap?) {
        if (processing) return
        timeout?.cancel()
        if (screenshot == null) {
            fail("Android did not allow this screenshot. Check assistant screenshot access; protected screens cannot be captured.")
            return
        }
        if (appContext.getSystemService(KeyguardManager::class.java).isKeyguardLocked) {
            fail("Unlock your phone before capturing the screen.")
            return
        }
        processing = true
        scope.launch {
            try {
                ChatGPTBridgeHelper(appContext).bridgeToChatGPT(screenshot, launchActivity = { intent ->
                    startAssistantActivity(intent)
                })
            } finally {
                finish()
            }
        }
    }

    private fun fail(message: String) {
        Toast.makeText(appContext, message, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
