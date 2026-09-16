package com.screengpt.overlay

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/** Observes volume keys only. Does not request access to window contents. */
class VolumeShortcutService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val chord = VolumeButtonChord()
    private val capture = Runnable {
        if (canCapture() && chord.fire(SystemClock.uptimeMillis())) {
            startActivity(Intent(this, ShortcutActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            chord.reset()
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
            event.keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) return false
        if (!canCapture() || event.isCanceled) {
            reset()
            return false
        }
        // Ignore key repeats: holding a key must not restart the timer.
        if (event.action == KeyEvent.ACTION_UP ||
            (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0)) {
            chord.update(event.keyCode == KeyEvent.KEYCODE_VOLUME_UP,
                event.action == KeyEvent.ACTION_DOWN, SystemClock.uptimeMillis())
            handler.removeCallbacks(capture)
            chord.deadline?.let { handler.postAtTime(capture, it) }
        }
        // Keep the complete key stream intact for Android's normal volume controls.
        return false
    }

    private fun canCapture(): Boolean =
        getSystemService(PowerManager::class.java).isInteractive &&
            !getSystemService(KeyguardManager::class.java).isKeyguardLocked

    private fun reset() {
        handler.removeCallbacks(capture)
        chord.reset()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = reset()
    override fun onUnbind(intent: Intent?): Boolean {
        reset()
        return super.onUnbind(intent)
    }
    override fun onDestroy() {
        reset()
        super.onDestroy()
    }
}
