package com.kody.assistant.ui
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kody.assistant.databinding.ActivityMainBinding
import com.kody.assistant.service.KodyService
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO, Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ANSWER_PHONE_CALLS)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermissions(); setupUI()
    }
    override fun onResume() { super.onResume(); updateStatus() }
    private fun setupUI() {
        binding.btnToggle.setOnClickListener {
            if (isServiceRunning()) {
                stopService(Intent(this, KodyService::class.java))
                getSharedPreferences("kody", MODE_PRIVATE).edit().putBoolean("service_running", false).apply()
            } else { startForegroundService(Intent(this, KodyService::class.java)) }
            updateStatus()
        }
        binding.btnAccessibility.setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        updateStatus()
    }
    private fun isServiceRunning() = getSharedPreferences("kody", MODE_PRIVATE).getBoolean("service_running", false)
    private fun isAccessibilityEnabled(): Boolean {
        val service = "${packageName}/com.kody.assistant.service.KodyAccessibilityService"
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.contains(service)
    }
    private fun updateStatus() {
        binding.tvStatus.text = if (isServiceRunning()) "✅ קודי פעיל ומאזין" else "⭕ קודי כבוי"
        binding.btnToggle.text = if (isServiceRunning()) "כבה את קודי" else "הפעל את קודי"
        binding.tvAccessibilityStatus.text = if (isAccessibilityEnabled()) "✅ נגישות מופעלת" else "⚠️ נגישות נדרשת — לחץ להפעלה"
    }
    private fun requestPermissions() {
        val missing = PERMISSIONS.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
    }
}
