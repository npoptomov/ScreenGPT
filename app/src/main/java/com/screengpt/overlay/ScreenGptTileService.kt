package com.screengpt.overlay

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class ScreenGptTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        tile.state = Tile.STATE_ACTIVE
        tile.label = "ScreenGPT"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = "Ask Screen"
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        // Collapse notification shade and trigger screen capture bridge
        val collapseAndTrigger = {
            FloatingWidgetService.triggerCapture(applicationContext)
        }

        try {
            if (isLocked) {
                unlockAndRun { collapseAndTrigger() }
            } else {
                collapseAndTrigger()
                // Close status bar shade
                val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                sendBroadcast(closeIntent)
            }
        } catch (e: Exception) {
            collapseAndTrigger()
        }
    }
}
