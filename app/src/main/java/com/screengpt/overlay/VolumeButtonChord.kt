package com.screengpt.overlay

/** One activation per press of both buttons; both must be released to rearm. */
internal class VolumeButtonChord {
    private var up = false
    private var down = false
    private var fired = false
    var deadline: Long? = null
        private set

    fun update(isVolumeUp: Boolean, pressed: Boolean, now: Long) {
        if (isVolumeUp) up = pressed else down = pressed
        if (!up && !down) fired = false
        if (!up || !down) deadline = null
        else if (!fired && deadline == null) deadline = now + 900
    }

    fun fire(now: Long): Boolean {
        val due = deadline ?: return false
        if (now < due || !up || !down || fired) return false
        fired = true
        deadline = null
        return true
    }

    fun reset() {
        up = false
        down = false
        fired = false
        deadline = null
    }
}
