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
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.roundToInt

/** Optional overlay owned by the screenshot accessibility service. */
internal class CaptureWidget(private val service: AccessibilityService) {
    private val windows = service.getSystemService(WindowManager::class.java)
    private val preferences = preferences(service)
    private var button: ImageButton? = null
    private var capturing = false
    private var dismissTarget: TextView? = null
    private fun dp(value: Int) = (value * service.resources.displayMetrics.density).roundToInt()
    private val size = (56 * service.resources.displayMetrics.density).roundToInt()
    private val params = WindowManager.LayoutParams(
        size, size, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.LEFT; x = 0; y = 240 }
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == ENABLED) update()
    }

    fun connect() { preferences.registerOnSharedPreferenceChangeListener(listener); update() }

    private fun update() {
        if (!preferences.getBoolean(ENABLED, false)) { remove(); return }
        if (button != null) return
        val display = service.resources.displayMetrics
        params.x = (display.widthPixels - size - dp(16)).coerceAtLeast(0)
        params.y = (display.heightPixels / 3).coerceAtLeast(0)
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
                    if (!dragging && (abs(dx) > slop || abs(dy) > slop)) {
                        dragging = true
                        showDismissTarget()
                    }
                    if (dragging) {
                        val display = service.resources.displayMetrics
                        params.x = (startX + dx.roundToInt()).coerceIn(0, (display.widthPixels - size).coerceAtLeast(0))
                        params.y = (startY + dy.roundToInt()).coerceIn(0, (display.heightPixels - size * 2).coerceAtLeast(0))
                        windows.updateViewLayout(view, params)
                        highlightDismissTarget(isOverDismissTarget(event.rawX, event.rawY))
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dismiss = dragging && isOverDismissTarget(event.rawX, event.rawY)
                    hideDismissTarget()
                    if (dismiss) preferences.edit().putBoolean(ENABLED, false).apply()
                    else if (!dragging) view.performClick()
                    dragging = false
                    true
                }
                MotionEvent.ACTION_CANCEL -> { dragging = false; hideDismissTarget(); true }
                else -> false
            }
        }
        windows.addView(view, params)
        button = view
    }

    private fun showDismissTarget() {
        if (dismissTarget != null) return
        val target = TextView(service).apply {
            text = "×"
            textSize = 36f
            gravity = Gravity.CENTER
            contentDescription = "Drag here to hide floating button"
            setTextColor(0xFFFFFFFF.toInt())
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xEE30384A.toInt())
                setStroke(dp(2), 0xFF8794A8.toInt())
            }
        }
        val targetParams = WindowManager.LayoutParams(
            dp(80), dp(80), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; y = dp(32) }
        windows.addView(target, targetParams)
        dismissTarget = target
    }

    private fun isOverDismissTarget(x: Float, y: Float): Boolean {
        val target = dismissTarget ?: return false
        if (!target.isLaidOut) return false
        val location = IntArray(2)
        target.getLocationOnScreen(location)
        val centerX = location[0] + target.width / 2f
        val centerY = location[1] + target.height / 2f
        val radius = target.width / 2f + dp(16)
        return (x - centerX) * (x - centerX) + (y - centerY) * (y - centerY) <= radius * radius
    }

    private fun highlightDismissTarget(active: Boolean) {
        val target = dismissTarget ?: return
        (target.background as GradientDrawable).setColor(if (active) 0xFFE34F61.toInt() else 0xEE30384A.toInt())
        target.scaleX = if (active) 1.12f else 1f
        target.scaleY = target.scaleX
    }

    private fun hideDismissTarget() {
        dismissTarget?.let { windows.removeView(it) }
        dismissTarget = null
    }

    fun setCapturing(value: Boolean) {
        capturing = value
        if (value) hideDismissTarget()
        button?.visibility = if (value) View.INVISIBLE else View.VISIBLE
    }
    private fun remove() { hideDismissTarget(); button?.let { windows.removeView(it) }; button = null }
    fun close() { preferences.unregisterOnSharedPreferenceChangeListener(listener); remove() }

    companion object {
        const val ENABLED = "floating_widget_enabled"
        fun preferences(context: Context): SharedPreferences = context.getSharedPreferences("capture_widget", Context.MODE_PRIVATE)
    }
}
