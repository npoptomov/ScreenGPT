package com.screengpt.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.screengpt.overlay.bridge.ChatGPTBridgeHelper
import com.screengpt.overlay.capture.ScreenCaptureManager
import com.screengpt.overlay.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot

class FloatingWidgetService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var windowManager: WindowManager
    private lateinit var prefsManager: PreferencesManager
    private lateinit var captureManager: ScreenCaptureManager
    private lateinit var bridgeHelper: ChatGPTBridgeHelper

    // Overlay Views
    private var bubbleView: View? = null
    private var dismissView: View? = null
    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var dismissParams: WindowManager.LayoutParams

    private var screenWidth = 1080
    private var screenHeight = 2400
    private var isOverDismissTarget = false
    private var hasVibratedForDismiss = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        activeServiceInstance = this
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefsManager = PreferencesManager.getInstance(this)
        captureManager = ScreenCaptureManager(this)
        bridgeHelper = ChatGPTBridgeHelper(this)

        updateScreenDimensions()
        startAsForegroundService(isMediaProjection = false)
        initOverlayViews()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CAPTURE_PERMISSION) {
            val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
            @Suppress("DEPRECATION")
            val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
            if (resultCode != 0 && resultData != null) {
                savedResultCode = resultCode
                savedResultData = resultData

                startAsForegroundService(isMediaProjection = true)
                initMediaProjection(resultCode, resultData)

                if (isPendingCaptureOnPermission) {
                    isPendingCaptureOnPermission = false
                    performScreenCapture()
                }
            }
        }
        return START_STICKY
    }

    private fun updateScreenDimensions() {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
    }

    private fun startAsForegroundService(isMediaProjection: Boolean) {
        val channelId = NOTIFICATION_CHANNEL_ID
        val channelName = "ScreenGPT Floating Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the floating AI screen widget running"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.service_running))
            .setContentText(getString(R.string.service_running_desc))
            .setSmallIcon(R.drawable.ic_sparkle)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val serviceType = if (isMediaProjection) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
                startForeground(NOTIFICATION_ID, notification, serviceType)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initOverlayViews() {
        val inflater = LayoutInflater.from(this)

        // 1. Floating Bubble View
        bubbleView = inflater.inflate(R.layout.overlay_floating_bubble, null)
        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = screenWidth - 180
            y = screenHeight / 3
        }

        // 2. Dismiss Target View at Bottom Center
        dismissView = inflater.inflate(R.layout.overlay_dismiss_target, null)
        dismissParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 60
        }
        dismissView?.visibility = View.GONE

        setupBubbleTouchListener()

        try {
            windowManager.addView(dismissView, dismissParams)
            windowManager.addView(bubbleView, bubbleParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBubbleTouchListener() {
        val view = bubbleView ?: return

        view.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = false

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = bubbleParams.x
                        initialY = bubbleParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        isOverDismissTarget = false
                        hasVibratedForDismiss = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY

                        if (abs(dx) > 12 || abs(dy) > 12) {
                            if (isClick) {
                                isClick = false
                                showDismissTarget()
                            }
                        }

                        bubbleParams.x = (initialX + dx).toInt()
                        bubbleParams.y = (initialY + dy).toInt()

                        try {
                            if (bubbleView?.isAttachedToWindow == true) {
                                windowManager.updateViewLayout(bubbleView, bubbleParams)
                            }
                        } catch (e: Exception) {
                            // Ignore
                        }

                        if (!isClick) {
                            checkDismissHover(event.rawX, event.rawY)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        hideDismissTarget()
                        if (isClick) {
                            onBubbleClicked()
                        } else if (isOverDismissTarget) {
                            dismissWidget()
                        } else {
                            snapBubbleToEdge()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun showDismissTarget() {
        dismissView?.let { view ->
            view.visibility = View.VISIBLE
            view.alpha = 0f
            view.animate().alpha(1f).setDuration(150).start()
        }
    }

    private fun hideDismissTarget() {
        dismissView?.let { view ->
            view.animate().alpha(0f).setDuration(150).withEndAction {
                view.visibility = View.GONE
                resetDismissTargetState()
            }.start()
        }
    }

    private fun checkDismissHover(rawX: Float, rawY: Float) {
        val view = dismissView ?: return
        val ivIcon = view.findViewById<ImageView>(R.id.ivDismissIcon) ?: return

        // Accurately compute the physical on-screen center coordinates of the X icon
        val loc = IntArray(2)
        ivIcon.getLocationOnScreen(loc)

        val iconCenterX = loc[0] + ivIcon.width / 2f
        val iconCenterY = loc[1] + ivIcon.height / 2f

        // Robust Euclidean distance calculation to the actual visual icon center
        val dist = hypot((rawX - iconCenterX).toDouble(), (rawY - iconCenterY).toDouble())
        val threshold = (ivIcon.width * 1.5f).coerceAtLeast(200f)

        if (dist < threshold) {
            if (!isOverDismissTarget) {
                isOverDismissTarget = true
                highlightDismissTarget(true)
                triggerHaptic()
            }
        } else {
            if (isOverDismissTarget) {
                isOverDismissTarget = false
                highlightDismissTarget(false)
                hasVibratedForDismiss = false
            }
        }
    }

    private fun highlightDismissTarget(active: Boolean) {
        val view = dismissView ?: return
        val ivIcon = view.findViewById<ImageView>(R.id.ivDismissIcon) ?: return
        val tvLabel = view.findViewById<TextView>(R.id.tvDismissLabel) ?: return

        if (active) {
            ivIcon.setBackgroundResource(R.drawable.bg_dismiss_target_active)
            ivIcon.setColorFilter(ContextCompat.getColor(this, R.color.white))
            ivIcon.animate().scaleX(1.25f).scaleY(1.25f).setDuration(120).start()
            tvLabel.text = "Release to remove"
            tvLabel.setTextColor(ContextCompat.getColor(this, R.color.white))
            tvLabel.setBackgroundResource(R.drawable.bg_chip_selected)
        } else {
            resetDismissTargetState()
        }
    }

    private fun resetDismissTargetState() {
        val view = dismissView ?: return
        val ivIcon = view.findViewById<ImageView>(R.id.ivDismissIcon) ?: return
        val tvLabel = view.findViewById<TextView>(R.id.tvDismissLabel) ?: return

        ivIcon.setBackgroundResource(R.drawable.bg_dismiss_target_normal)
        ivIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_error))
        ivIcon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start()
        tvLabel.text = "Drag here to remove"
        tvLabel.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        tvLabel.setBackgroundResource(R.drawable.bg_chip_unselected)
    }

    private fun triggerHaptic() {
        if (!hasVibratedForDismiss) {
            hasVibratedForDismiss = true
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(45)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun dismissWidget() {
        bubbleView?.animate()?.scaleX(0f)?.scaleY(0f)?.alpha(0f)?.setDuration(180)?.withEndAction {
            Toast.makeText(this, "ScreenGPT widget removed", Toast.LENGTH_SHORT).show()
            stopSelf()
        }?.start()
    }

    private fun snapBubbleToEdge() {
        val currentX = bubbleParams.x
        val targetX = if (currentX + 100 > screenWidth / 2) screenWidth - 180 else 30

        val animator = ValueAnimator.ofInt(currentX, targetX).apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                bubbleParams.x = animation.animatedValue as Int
                try {
                    if (bubbleView?.isAttachedToWindow == true) {
                        windowManager.updateViewLayout(bubbleView, bubbleParams)
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
        animator.start()
    }

    private fun onBubbleClicked() {
        if (!captureManager.hasProjection()) {
            if (savedResultCode != 0 && savedResultData != null) {
                initMediaProjection(savedResultCode, savedResultData!!)
            } else {
                isPendingCaptureOnPermission = true
                CapturePermissionActivity.requestPermission(this)
                return
            }
        }

        performScreenCapture()
    }

    private fun initMediaProjection(resultCode: Int, data: Intent) {
        try {
            val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val projection: MediaProjection? = projectionManager.getMediaProjection(resultCode, data)
            if (projection != null) {
                captureManager.setMediaProjection(projection)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performScreenCapture() {
        serviceScope.launch {
            // Hide bubble briefly so it's not captured in screenshot
            bubbleView?.visibility = View.GONE
            delay(100)

            val captureResult = captureManager.captureScreen()
            bubbleView?.visibility = View.VISIBLE

            captureResult.onSuccess { data ->
                bridgeHelper.bridgeToChatGPT(data.fullBitmap)
            }.onFailure { error ->
                Toast.makeText(
                    this@FloatingWidgetService,
                    "${getString(R.string.error_capture_failed)}: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
                isPendingCaptureOnPermission = true
                CapturePermissionActivity.requestPermission(this@FloatingWidgetService)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        try {
            bubbleView?.let { if (it.isAttachedToWindow) windowManager.removeView(it) }
            dismissView?.let { if (it.isAttachedToWindow) windowManager.removeView(it) }
        } catch (e: Exception) {
            // Ignore
        }

        captureManager.release()
        activeServiceInstance = null
    }

    companion object {
        const val ACTION_CAPTURE_PERMISSION = "com.screengpt.overlay.ACTION_CAPTURE_PERMISSION"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"

        private const val NOTIFICATION_ID = 9001
        private const val NOTIFICATION_CHANNEL_ID = "screengpt_service_channel"

        @Volatile
        private var activeServiceInstance: FloatingWidgetService? = null

        private var savedResultCode: Int = 0
        private var savedResultData: Intent? = null
        private var isPendingCaptureOnPermission = false

        fun isRunning(): Boolean = activeServiceInstance != null
        fun hasCapturePermission(): Boolean = savedResultCode != 0 && savedResultData != null

        fun start(context: Context) {
            val intent = Intent(context, FloatingWidgetService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingWidgetService::class.java)
            context.stopService(intent)
        }

        fun onCapturePermissionReceived(context: Context, resultCode: Int, data: Intent) {
            savedResultCode = resultCode
            savedResultData = data

            activeServiceInstance?.let { service ->
                service.startAsForegroundService(isMediaProjection = true)
                service.initMediaProjection(resultCode, data)
                if (isPendingCaptureOnPermission) {
                    isPendingCaptureOnPermission = false
                    service.performScreenCapture()
                }
            } ?: run {
                val serviceIntent = Intent(context, FloatingWidgetService::class.java).apply {
                    action = ACTION_CAPTURE_PERMISSION
                    putExtra(EXTRA_RESULT_CODE, resultCode)
                    putExtra(EXTRA_RESULT_DATA, data)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
