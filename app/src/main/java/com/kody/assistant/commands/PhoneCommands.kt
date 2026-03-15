package com.kody.assistant.commands
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.util.Log
class PhoneCommands(private val context: Context) {
    fun callContact(name: String, cb: (Boolean, String) -> Unit) {
        val number = findNumber(name)
        if (number != null) {
            try {
                context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                cb(true, "מתקשר ל$name")
            } catch (e: Exception) { cb(false, "שגיאה בחיוג") }
        } else cb(false, "לא מצאתי את $name")
    }
    fun endCall() {
        try { (context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager).endCall() }
        catch (e: Exception) { com.kody.assistant.service.KodyAccessibilityService.instance?.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK) }
    }
    private fun findNumber(name: String): String? {
        return try {
            val cursor = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?", arrayOf("%$name%"), null)
            cursor?.use { if (it.moveToFirst()) it.getString(0) else null }
        } catch (e: Exception) { null }
    }
}
