package com.kody.assistant.service
import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
class KodyAccessibilityService : AccessibilityService() {
    companion object { var instance: KodyAccessibilityService? = null }
    override fun onServiceConnected() { super.onServiceConnected(); instance = this; Log.d("KodyA11y","connected") }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { super.onDestroy(); instance = null }
}
