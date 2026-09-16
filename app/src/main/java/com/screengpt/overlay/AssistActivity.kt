package com.screengpt.overlay

import android.os.Bundle

class AssistActivity : CapturePermissionActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        intent.putExtra(EXTRA_CAPTURE_AFTER_GRANT, true)
        super.onCreate(savedInstanceState)
    }
}
