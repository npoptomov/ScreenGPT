package com.screengpt.overlay

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.ImageButton
import kotlin.math.abs
import kotlin.math.roundToInt

/** Optional overlay owned by the screenshot accessibility service. */
internal class CaptureWidget(private val service: AccessibilityService) {
    private val windows = service.getSystemService(WindowManager::class.java)
    private val preferences = preferences(service)
    private var button: ImageButton? = null
    private var capturing = false
    private val size = (56 * service.resources.displayMetrics.density).roundToInt()
    private val params = WindowManager.LayoutParams(
        size, size, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.LEFT; x = 16; y = 240 }
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == ENABLED) update()
    }

    fun connect() { preferences.registerOnSharedPreferenceChangeListener(listener); update() }

    private fun update() {
        if (!preferences.getBoolean(ENABLED, false)) { remove(); return }
        if (button != null) return
        val view = ImageButton(service).apply {
            contentDescription = "Capture screen with ScreenGPT"
            setImageResource(R.drawable.ic_launcher_foreground)
            setPadding(0, 0, 0, 0)
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(0xFF10182D.toInt()) }
            elevation = 8 * resources.displayMetrics.density
            visibility = if (capturing) View.INVISIBLE else View.VISIBLE
            setOnClickListener {
                service.startActivity(Intent(service, CapturePermissionActivity::class.java)
                    .putExtra(CapturePermissionActivity.EXTRA_CAPTURE_AFTER_GRANT, true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
        var downX = 0f; var downY = 0f; var startX = 0; var startY = 0; var dragging = false
        val slop = ViewConfiguration.get(service).scaledTouchSlop
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX; downY = event.rawY; startX = params.x; startY = params.y; dragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX; val dy = event.rawY - downY
                    if (abs(dx) > slop || abs(dy) > slop) dragging = true
                    if (dragging) {
                        val display = service.resources.displayMetrics
                        params.x = (startX + dx.roundToInt()).coerceIn(0, (display.widthPixels - size).coerceAtLeast(0))
                        params.y = (startY + dy.roundToInt()).coerceIn(0, (display.heightPixels - size * 2).coerceAtLeast(0))
                        windows.updateViewLayout(view, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> { if (!dragging) view.performClick(); true }
                MotionEvent.ACTION_CANCEL -> true
                else -> false
            }
        }
        windows.addView(view, params)
        button = view
    }

    fun setCapturing(value: Boolean) {
        capturing = value
        button?.visibility = if (value) View.INVISIBLE else View.VISIBLE
    }
    private fun remove() { button?.let { windows.removeView(it) }; button = null }
    fun close() { preferences.unregisterOnSharedPreferenceChangeListener(listener); remove() }

    companion object {
        const val ENABLED = "floating_widget_enabled"
        fun preferences(context: Context): SharedPreferences = context.getSharedPreferences("capture_widget", Context.MODE_PRIVATE)
    }
}
