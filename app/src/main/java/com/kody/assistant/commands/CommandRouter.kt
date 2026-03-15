package com.kody.assistant.commands
import android.content.Context
import android.util.Log
import com.kody.assistant.service.KodyService
class CommandRouter(private val context: Context) {
    private val device = DeviceCommands(context)
    private val phone = PhoneCommands(context)
    private val media = MediaCommands(context)
    private val app = AppCommands(context)
    private fun speak(t: String) = (context as? KodyService)?.speak(t)
    fun route(cmd: String) {
        val c = cmd.lowercase().trim()
        Log.d("Router","cmd: $c")
        when {
            c.contains("הדלק פנס") || (c.contains("פנס") && c.contains("הדלק")) -> { device.setFlashlight(true); speak("פנס הודלק") }
            c.contains("כבה פנס") || (c.contains("פנס") && c.contains("כבה")) -> { device.setFlashlight(false); speak("פנס כובה") }
            c.contains("התקשר") || c.contains("חייג") -> {
                val name = extractName(c)
                if (name.isNotEmpty()) phone.callContact(name) { ok, msg -> speak(msg) }
                else speak("למי להתקשר?")
            }
            c.contains("נתק") || c.contains("סיים שיחה") -> { phone.endCall(); speak("שיחה הסתיימה") }
            c.contains("הפעל מוזיקה") || c.contains("נגן") -> { media.playMusic(); speak("מפעיל מוזיקה") }
            c.contains("עצור מוזיקה") || c.contains("הפסק") || c.contains("פוז") -> { media.pauseMusic(); speak("עוצר") }
            c.contains("הבא שיר") || c.contains("שיר הבא") -> { media.nextTrack(); speak("שיר הבא") }
            c.contains("הגבר") || c.contains("עלה עוצמה") -> { media.volumeUp(); speak("מגביר") }
            c.contains("הנמך") || c.contains("הורד עוצמה") -> { media.volumeDown(); speak("מנמיך") }
            c.contains("וואטסאפ") || c.contains("ווטסאפ") -> { app.openApp("com.whatsapp"); speak("פותח וואטסאפ") }
            c.contains("מפות") -> { app.openApp("com.google.android.apps.maps"); speak("פותח מפות") }
            c.contains("מצלמה") -> { app.openCamera(); speak("פותח מצלמה") }
            c.contains("הגדרות") -> { app.openSettings(); speak("פותח הגדרות") }
            c.contains("ספוטיפיי") || c.contains("ספוטיפי") -> { app.openApp("com.spotify.music"); speak("פותח ספוטיפיי") }
            c.contains("כרום") -> { app.openApp("com.android.chrome"); speak("פותח כרום") }
            c.contains("שער") || c.contains("דלת") -> { app.openGateApp(); speak("פותח שער") }
            c.contains("מסך הבית") || c.contains("עמוד הבית") || c.contains("בית") -> { device.goHome(); speak("חוזר הביתה") }
            c.contains("חזור") -> { device.goBack(); speak("חוזר") }
            c.contains("מה השעה") || c.contains("כמה שעה") -> speak("השעה ${device.getCurrentTime()}")
            else -> { speak("לא הבנתי, נסה שוב"); Log.d("Router","unknown: $c") }
        }
    }
    private fun extractName(c: String): String {
        listOf("התקשר ל","חייג ל","התקשר אל","חייג אל").forEach { p ->
            val i = c.indexOf(p); if (i >= 0) return c.substring(i + p.length).trim()
        }
        return ""
    }
}
