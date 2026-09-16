package com.screengpt.overlay

import org.junit.Assert.*
import org.junit.Test

class CaptureRequestGateTest {
    @Test fun simultaneousEntryPointsDoNotStartTwoShares() {
        val gate = CaptureRequestGate()
        assertTrue(gate.acquire())
        assertFalse(gate.acquire())
        assertFalse(gate.acquire())
    }
    @Test fun completionOrCancellationAllowsNextCapture() {
        val gate = CaptureRequestGate()
        assertTrue(gate.acquire())
        gate.release()
        assertTrue(gate.acquire())
    }
}
