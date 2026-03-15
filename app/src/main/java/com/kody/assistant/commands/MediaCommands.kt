package com.kody.assistant.commands
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.SystemClock
import android.view.KeyEvent
class MediaCommands(private val context: Context) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    fun playMusic() { sendKey(KeyEvent.KEYCODE_MEDIA_PLAY); if (!audio.isMusicActive) openMusic() }
    fun pauseMusic() { sendKey(KeyEvent.KEYCODE_MEDIA_PAUSE) }
    fun nextTrack() { sendKey(KeyEvent.KEYCODE_MEDIA_NEXT) }
    fun volumeUp() { audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI) }
    fun volumeDown() { audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI) }
    private fun sendKey(code: Int) {
        val t = SystemClock.uptimeMillis()
        audio.dispatchMediaKeyEvent(KeyEvent(t, t, KeyEvent.ACTION_DOWN, code, 0))
        audio.dispatchMediaKeyEvent(KeyEvent(t, t, KeyEvent.ACTION_UP, code, 0))
    }
    private fun openMusic() {
        listOf("com.spotify.music","com.google.android.music","com.samsung.android.music").forEach { pkg ->
            context.packageManager.getLaunchIntentForPackage(pkg)?.let { context.startActivity(it.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }); return }
        }
    }
}
