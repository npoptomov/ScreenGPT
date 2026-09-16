package com.screengpt.overlay

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.screengpt.overlay.databinding.ActivityMainBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.switchFloatingWidget.isChecked = CaptureWidget.preferences(this).getBoolean(CaptureWidget.ENABLED, false)
        binding.switchFloatingWidget.isEnabled = Build.VERSION.SDK_INT >= 30
        binding.switchFloatingWidget.setOnCheckedChangeListener { _, checked ->
            CaptureWidget.preferences(this).edit().putBoolean(CaptureWidget.ENABLED, checked).apply()
            if (checked && !ScreenCaptureAccessibilityService.isEnabled(this)) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        binding.btnEnableCapture.setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        binding.btnBatterySettings.setOnClickListener {
            try { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
            catch (_: Exception) { startActivity(Intent(Settings.ACTION_SETTINGS)) }
        }
        binding.btnTemporarySession.setOnClickListener { CapturePermissionActivity.requestPermission(this) }
        binding.btnStopSession.setOnClickListener { FloatingWidgetService.stop(this); binding.root.postDelayed({ updateState() }, 200) }
        binding.btnSetAssistant.setOnClickListener {
            try {
                val roles = if (Build.VERSION.SDK_INT >= 29) getSystemService(android.app.role.RoleManager::class.java) else null
                val intent = if (Build.VERSION.SDK_INT >= 29 && roles?.isRoleAvailable(android.app.role.RoleManager.ROLE_ASSISTANT) == true && !roles.isRoleHeld(android.app.role.RoleManager.ROLE_ASSISTANT))
                    roles.createRequestRoleIntent(android.app.role.RoleManager.ROLE_ASSISTANT)
                else Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
                startActivity(intent)
            } catch (_: Exception) { startActivity(Intent(Settings.ACTION_SETTINGS)) }
        }
    }
    override fun onResume() { super.onResume(); updateState() }
    private fun updateState() {
        val enabled = ScreenCaptureAccessibilityService.isEnabled(this)
        if (enabled) FloatingWidgetService.stop(this)
        binding.tvServiceStatus.text = when {
            enabled && ScreenCaptureAccessibilityService.instance != null -> "READY — double-press power to capture"
            enabled -> "ENABLED — waiting for Android to connect the service"
            FloatingWidgetService.hasCapturePermission() -> "Temporary capture session active"
            else -> "Setup required — enable ScreenGPT screen capture below"
        }
        binding.btnEnableCapture.isEnabled = Build.VERSION.SDK_INT >= 30
        binding.btnEnableCapture.text = if (enabled) "Manage screenshot access" else "Enable screenshot access"
        binding.btnTemporarySession.isEnabled = !enabled && !FloatingWidgetService.hasCapturePermission()
        binding.btnStopSession.isEnabled = FloatingWidgetService.hasCapturePermission()
    }
}
