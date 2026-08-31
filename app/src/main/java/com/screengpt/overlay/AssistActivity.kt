package com.screengpt.overlay

import android.app.Activity
import android.os.Bundle

/**
 * Transparent Assist Activity triggered by Android Digital Assistant gestures
 * (e.g. Holding Power button or Swiping diagonally from bottom corners).
 */
class AssistActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FloatingWidgetService.triggerCapture(applicationContext)
        finish()
    }
}
