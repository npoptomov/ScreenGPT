package com.screengpt.overlay

/** Main-thread gate: two shortcut entry points must not share the same cache image concurrently. */
internal class CaptureRequestGate {
    private var busy = false
    fun acquire(): Boolean {
        if (busy) return false
        busy = true
        return true
    }
    fun release() { busy = false }
}
