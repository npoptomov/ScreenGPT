package com.screengpt.overlay

import android.os.Bundle

/** Honor's launch-app gesture resolves this same entry as tapping the app icon. */
class MainActivity : CapturePermissionActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        intent.putExtra(EXTRA_CAPTURE_AFTER_GRANT, true)
        super.onCreate(savedInstanceState)
    }
}
