package com.screengpt.overlay

import org.junit.Assert.*
import org.junit.Test

class VolumeButtonChordTest {
    @Test fun singleButtonNeverCaptures() {
        val chord = VolumeButtonChord()
        chord.update(true, true, 0)
        assertNull(chord.deadline)
        assertFalse(chord.fire(2000))
    }

    @Test fun eitherOrderRequiresFullHoldAndFiresOnce() {
        for (firstUp in listOf(true, false)) {
            val chord = VolumeButtonChord()
            chord.update(firstUp, true, 0)
            chord.update(!firstUp, true, 100)
            assertFalse(chord.fire(999))
            assertTrue(chord.fire(1000))
            assertFalse(chord.fire(2000))
            chord.update(firstUp, false, 2100)
            chord.update(firstUp, true, 2200)
            assertFalse(chord.fire(4000))
            chord.update(true, false, 4100)
            chord.update(false, false, 4101)
            chord.update(true, true, 4200)
            chord.update(false, true, 4300)
            assertTrue(chord.fire(5200))
        }
    }

    @Test fun earlyReleaseCancelsAndNewHoldRestartsDeadline() {
        val chord = VolumeButtonChord()
        chord.update(true, true, 0)
        chord.update(false, true, 10)
        chord.update(false, false, 500)
        assertFalse(chord.fire(1000))
        chord.update(false, true, 1100)
        assertFalse(chord.fire(1999))
        assertTrue(chord.fire(2000))
    }

    @Test fun resetCancelsPendingCapture() {
        val chord = VolumeButtonChord()
        chord.update(true, true, 0)
        chord.update(false, true, 10)
        chord.reset()
        assertNull(chord.deadline)
        assertFalse(chord.fire(1000))
    }
}
