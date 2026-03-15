package com.kody.assistant.commands
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
class AppCommands(private val context: Context) {
    fun openApp(pkg: String) {
        context.packageManager.getLaunchIntentForPackage(pkg)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            ?.let { context.startActivity(it) }
            ?: Log.w("AppCmd","Not found: $pkg")
    }
    fun openCamera() { context.startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) }
    fun openSettings() { context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) }
    fun openMaps(q: String = "") {
        val uri = if (q.isNotEmpty()) Uri.parse("geo:0,0?q=${Uri.encode(q)}") else Uri.parse("geo:0,0")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    }
    fun openGateApp() {
        val pkg = context.getSharedPreferences("kody", Context.MODE_PRIVATE).getString("gate_app_package","") ?: ""
        if (pkg.isNotEmpty()) openApp(pkg)
        else context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    }
}
