package com.kody.assistant.commands
import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Log
import com.kody.assistant.service.KodyAccessibilityService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class DeviceCommands(private val context: Context) {
    fun setFlashlight(on: Boolean) {
        try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cm.setTorchMode(cm.cameraIdList[0], on)
        } catch (e: Exception) { Log.e("DeviceCmd","Flash: ${e.message}") }
    }
    fun goHome() { KodyAccessibilityService.instance?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME) }
    fun goBack() { KodyAccessibilityService.instance?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK) }
    fun getCurrentTime(): String = SimpleDateFormat("HH:mm", Locale("iw","IL")).format(Date())
}
