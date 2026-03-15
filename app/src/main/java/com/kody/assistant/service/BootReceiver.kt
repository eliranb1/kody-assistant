package com.kody.assistant.service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (context.getSharedPreferences("kody", Context.MODE_PRIVATE).getBoolean("service_running", false))
                context.startForegroundService(Intent(context, KodyService::class.java))
        }
    }
}
