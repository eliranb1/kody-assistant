package com.kody.assistant.service
import android.app.*
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kody.assistant.commands.CommandRouter
import java.util.Locale
class KodyService : Service() {
    companion object { const val TAG = "KodyService"; const val CH = "kody_ch"; const val NID = 1 }
    private var sr: SpeechRecognizer? = null
    private lateinit var tts: TextToSpeech
    private lateinit var router: CommandRouter
    private var cmdMode = false; private var ttsOk = false; private var active = false
    override fun onCreate() {
        super.onCreate()
        createChannel(); startForeground(NID, buildNotif("מאזין..."))
        router = CommandRouter(this); initTTS(); mark(true)
    }
    override fun onDestroy() { super.onDestroy(); active = false; sr?.destroy(); tts.shutdown(); mark(false) }
    override fun onBind(i: Intent?): IBinder? = null
    private fun initTTS() {
        tts = TextToSpeech(this) { s ->
            if (s == TextToSpeech.SUCCESS) {
                tts.language = Locale("iw","IL"); tts.setSpeechRate(1.05f); ttsOk = true; active = true; initSR()
            }
        }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) { if (id?.startsWith("cmd") == true) restartListen(500) }
            override fun onError(id: String?) { restartListen(500) }
        })
    }
    fun speak(text: String, id: String = "cmd_r") { if (ttsOk) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) }
    private fun initSR() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        sr = SpeechRecognizer.createSpeechRecognizer(this); sr?.setRecognitionListener(listener); listenWake()
    }
    private fun listenWake() { cmdMode = false; updateNotif("מאזין... (היי קודי)"); startListen(true) }
    private fun listenCmd() { cmdMode = true; updateNotif("ממתין לפקודה..."); startListen(false) }
    private fun startListen(cont: Boolean) {
        if (!active) return
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "iw-IL")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            if (cont) putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
        }
        try { sr?.startListening(i) } catch (e: Exception) { restartListen(1000) }
    }
    fun restartListen(d: Long = 300) { android.os.Handler(mainLooper).postDelayed({ if (active) listenWake() }, d) }
    private val listener = object : RecognitionListener {
        override fun onResults(r: Bundle?) {
            val text = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim() ?: run { restartListen(); return }
            if (cmdMode) processCmd(text) else if (isWake(text)) onWake(text) else restartListen()
        }
        override fun onPartialResults(r: Bundle?) {
            val p = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: return
            if (!cmdMode && isWake(p)) onWake(p)
        }
        override fun onError(e: Int) { restartListen(if (e == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) 1500L else 500L) }
        override fun onReadyForSpeech(p: Bundle?) {}; override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(r: Float) {}; override fun onBufferReceived(b: ByteArray?) {}
        override fun onEndOfSpeech() {}; override fun onEvent(t: Int, p: Bundle?) {}
    }
    private fun isWake(t: String): Boolean {
        val l = t.lowercase()
        return l.contains("היי קודי") || l.contains("הי קודי") || l.contains("hey kody") || l.contains("קודי")
    }
    private fun onWake(text: String) {
        sr?.stopListening()
        val inline = extractInline(text)
        if (inline.isNotEmpty()) { speak("כן?","ack"); android.os.Handler(mainLooper).postDelayed({ processCmd(inline) }, 400) }
        else { speak("כן?","ack_l"); android.os.Handler(mainLooper).postDelayed({ listenCmd() }, 600) }
    }
    private fun extractInline(text: String): String {
        val t = text.lowercase()
        listOf("היי קודי ","הי קודי ","hey kody ","קודי ").forEach { p ->
            val i = t.indexOf(p); if (i >= 0) return t.substring(i + p.length).trim()
        }
        return ""
    }
    private fun processCmd(cmd: String) { Log.d(TAG,"cmd: $cmd"); updateNotif("מעבד..."); router.route(cmd) }
    private fun createChannel() {
        NotificationChannel(CH,"קודי",NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false) }
            .also { getSystemService(NotificationManager::class.java).createNotificationChannel(it) }
    }
    private fun buildNotif(t: String): Notification {
        val pi = PendingIntent.getActivity(this,0,packageManager.getLaunchIntentForPackage(packageName),PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this,CH).setContentTitle("קודי").setContentText(t)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentIntent(pi).setOngoing(true).setSilent(true).build()
    }
    private fun updateNotif(t: String) = getSystemService(NotificationManager::class.java).notify(NID, buildNotif(t))
    private fun mark(r: Boolean) = getSharedPreferences("kody",MODE_PRIVATE).edit().putBoolean("service_running",r).apply()
}
