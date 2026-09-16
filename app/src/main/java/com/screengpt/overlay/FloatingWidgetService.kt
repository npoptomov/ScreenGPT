package com.screengpt.overlay

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.screengpt.overlay.capture.ScreenCaptureManager

/** Legacy component name retained for upgrades. Only hosts an optional projection session. */
class FloatingWidgetService : Service() {
    private lateinit var captureManager: ScreenCaptureManager
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        captureManager = ScreenCaptureManager(this) { stopSelf() }
        activeServiceInstance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        @Suppress("DEPRECATION")
        val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
        if (resultCode != Activity.RESULT_OK || resultData == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        try {
            showNotification()
            val projection = getSystemService(MediaProjectionManager::class.java)
                .getMediaProjection(resultCode, resultData)
            captureManager.setMediaProjection(projection)
            if (!captureManager.hasProjection()) error("Screen capture could not start.")
        } catch (e: Exception) {
            Toast.makeText(this, e.message ?: "Screen capture could not start.", Toast.LENGTH_LONG).show()
            stopSelf()
        }
        // A dead process cannot restore a projection token. Never restart with an expired token.
        return START_NOT_STICKY
    }

    private fun showNotification() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Screen capture session", NotificationManager.IMPORTANCE_LOW)
        )
        val settings = PendingIntent.getActivity(this, 0, Intent(this, SettingsActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 1, Intent(this, FloatingWidgetService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_screen_gpt_status)
            .setContentTitle("ScreenGPT capture ready")
            .setContentText("Double-press power to capture. Tap for settings.")
            .setContentIntent(settings).setOngoing(true)
            .addAction(0, "Stop capture", stop).build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(9001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        else startForeground(9001, notification)
    }

    override fun onDestroy() {
        captureManager.release()
        if (activeServiceInstance === this) activeServiceInstance = null
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL = "screengpt_capture_session"
        private const val ACTION_STOP = "com.screengpt.overlay.STOP_CAPTURE"
        private const val EXTRA_RESULT_CODE = "extra_result_code"
        private const val EXTRA_RESULT_DATA = "extra_result_data"
        private var activeServiceInstance: FloatingWidgetService? = null
        fun hasCapturePermission() = activeServiceInstance?.captureManager?.hasProjection() == true
        suspend fun capture() = activeServiceInstance?.captureManager?.captureScreen()
            ?: Result.failure(IllegalStateException("Screen capture session ended. Start a new session in Settings."))
        fun stop(context: Context) { context.stopService(Intent(context, FloatingWidgetService::class.java)) }
        fun onCapturePermissionReceived(context: Context, resultCode: Int, data: Intent) {
            context.startForegroundService(Intent(context, FloatingWidgetService::class.java)
                .putExtra(EXTRA_RESULT_CODE, resultCode).putExtra(EXTRA_RESULT_DATA, data))
        }
    }
}
