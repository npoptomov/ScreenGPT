package com.screengpt.overlay

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.screengpt.overlay.data.PreferencesManager
import com.screengpt.overlay.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updatePermissionStates()
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        updatePermissionStates()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager.getInstance(this)

        CapturePermissionActivity.onPermissionGrantedListener = {
            runOnUiThread {
                updatePermissionStates()
            }
        }

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStates()
        updateServiceStateUI()
    }

    private fun setupListeners() {
        // Permission 1: Overlay
        binding.btnGrantOverlay.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                overlayPermissionLauncher.launch(intent)
            }
        }

        // Permission 2: Screen Capture
        binding.btnGrantCapture.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please grant Overlay permission first.", Toast.LENGTH_SHORT).show()
                binding.btnGrantOverlay.performClick()
                return@setOnClickListener
            }

            if (!FloatingWidgetService.isRunning()) {
                FloatingWidgetService.start(this)
            }

            CapturePermissionActivity.requestPermission(this)
        }

        // Permission 3: Notification
        binding.btnGrantNotification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Set Digital Assistant Button
        binding.btnSetAssistant.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                    startActivity(intent)
                } catch (e2: Exception) {
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    startActivity(intent)
                }
            }
        }

        // Toggle Floating Service
        binding.btnToggleService.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please grant Overlay Permission first.", Toast.LENGTH_SHORT).show()
                binding.btnGrantOverlay.performClick()
                return@setOnClickListener
            }

            if (FloatingWidgetService.isRunning()) {
                FloatingWidgetService.stop(this)
                prefsManager.isServiceEnabled = false
            } else {
                FloatingWidgetService.start(this)
                prefsManager.isServiceEnabled = true
                if (!FloatingWidgetService.hasCapturePermission()) {
                    CapturePermissionActivity.requestPermission(this)
                }
            }

            binding.root.postDelayed({
                updateServiceStateUI()
                updatePermissionStates()
            }, 400)
        }
    }

    private fun updatePermissionStates() {
        // Overlay Permission
        val hasOverlay = Settings.canDrawOverlays(this)
        if (hasOverlay) {
            binding.btnGrantOverlay.text = getString(R.string.btn_granted)
            binding.btnGrantOverlay.isEnabled = false
            binding.btnGrantOverlay.setTextColor(ContextCompat.getColor(this, R.color.status_active))
        } else {
            binding.btnGrantOverlay.text = getString(R.string.btn_grant)
            binding.btnGrantOverlay.isEnabled = true
            binding.btnGrantOverlay.setTextColor(ContextCompat.getColor(this, R.color.brand_emerald))
        }

        // Screen Capture Permission
        val hasCapture = FloatingWidgetService.hasCapturePermission()
        if (hasCapture) {
            binding.btnGrantCapture.text = getString(R.string.btn_granted)
            binding.btnGrantCapture.isEnabled = false
            binding.btnGrantCapture.setTextColor(ContextCompat.getColor(this, R.color.status_active))
        } else {
            binding.btnGrantCapture.text = getString(R.string.btn_grant)
            binding.btnGrantCapture.isEnabled = true
            binding.btnGrantCapture.setTextColor(ContextCompat.getColor(this, R.color.brand_emerald))
        }

        // Notification Permission (Android 13+)
        val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (hasNotification) {
            binding.btnGrantNotification.text = getString(R.string.btn_granted)
            binding.btnGrantNotification.isEnabled = false
            binding.btnGrantNotification.setTextColor(ContextCompat.getColor(this, R.color.status_active))
        } else {
            binding.btnGrantNotification.text = getString(R.string.btn_grant)
            binding.btnGrantNotification.isEnabled = true
            binding.btnGrantNotification.setTextColor(ContextCompat.getColor(this, R.color.brand_emerald))
        }
    }

    private fun updateServiceStateUI() {
        val isRunning = FloatingWidgetService.isRunning()
        if (isRunning) {
            binding.tvServiceStatus.text = "ACTIVE"
            binding.tvServiceStatus.setTextColor(ContextCompat.getColor(this, R.color.status_active))
            binding.tvServiceStatus.setBackgroundResource(R.drawable.bg_chip_selected)
            binding.btnToggleService.text = getString(R.string.btn_stop_service)
        } else {
            binding.tvServiceStatus.text = "INACTIVE"
            binding.tvServiceStatus.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
            binding.tvServiceStatus.setBackgroundResource(R.drawable.bg_chip_unselected)
            binding.btnToggleService.text = getString(R.string.btn_start_service)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CapturePermissionActivity.onPermissionGrantedListener = null
    }
}
