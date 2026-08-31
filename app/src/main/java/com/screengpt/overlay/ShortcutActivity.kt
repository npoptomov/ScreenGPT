package com.screengpt.overlay

import android.app.Activity
import android.os.Bundle

/**
 * 1-Tap Home Screen Shortcut Activity to capture and bridge to ChatGPT instantly.
 */
class ShortcutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FloatingWidgetService.triggerCapture(applicationContext)
        finish()
    }
}
