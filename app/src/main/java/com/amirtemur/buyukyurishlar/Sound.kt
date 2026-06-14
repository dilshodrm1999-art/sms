package com.amirtemur.buyukyurishlar

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Tashqi audio fayllarsiz — barcha effektlar dasturiy ravishda (PCM) generatsiya qilinadi.
 * Bu APK hajmini kichik saqlaydi va copyright muammosini bartaraf etadi.
 * Har bir chaqiruv try/catch bilan himoyalangan: ovoz hech qachon o'yinni buzmaydi.
 */
class Sound(var enabled: Boolean) {

    private val rate = 22050
    private val exec = Executors.newSingleThreadExecutor()

    fun click() = play { tone(660f, 0.06f, 0.3f) }
    fun confirm() = play { tone(523f, 0.09f, 0.35f) + tone(784f, 0.12f, 0.35f) }
    fun march() = play { drum(0.16f) + silence(0.06f) + drum(0.16f) + silence(0.06f) + drum(0.2f) }
    fun clash() = play { noise(0.18f, 0.45f) + tone(180f, 0.18f, 0.25f) }
    fun horn() = play { tone(196f, 0.22f, 0.45f) + tone(262f, 0.34f, 0.5f) }      // hujum karnayi
    fun volley() = play { noise(0.05f, 0.3f) + silence(0.04f) + noise(0.05f, 0.3f) + silence(0.04f) + noise(0.07f, 0.35f) } // o'q yog'di
    fun gong() = play { tone(330f, 0.28f, 0.4f) + tone(247f, 0.4f, 0.32f) }       // bosqich
    fun victory() = play {
        tone(523f, 0.13f, 0.4f) + silence(0.03f) + tone(659f, 0.13f, 0.4f) + silence(0.03f) +
        tone(784f, 0.16f, 0.42f) + silence(0.03f) + tone(1047f, 0.34f, 0.48f)
    }
    fun fanfare() = play {
        tone(392f, 0.16f, 0.4f) + silence(0.04f) + tone(523f, 0.16f, 0.42f) + silence(0.04f) +
        tone(659f, 0.16f, 0.42f) + silence(0.04f) + tone(784f, 0.2f, 0.45f) + silence(0.05f) +
        tone(1047f, 0.5f, 0.5f)
    }
    fun defeat() = play { tone(392f, 0.18f, 0.4f) + tone(294f, 0.3f, 0.4f) + tone(220f, 0.4f, 0.38f) }

    private fun play(gen: () -> ShortArray) {
        if (!enabled) return
        try {
            exec.execute {
                try {
                    val data = gen()
                    val track = AudioTrack(
                        AudioManager.STREAM_MUSIC, rate,
                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        maxOf(data.size * 2, 2048), AudioTrack.MODE_STATIC
                    )
                    track.write(data, 0, data.size)
                    track.setNotificationMarkerPosition(data.size)
                    track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                        override fun onMarkerReached(t: AudioTrack?) { try { t?.release() } catch (_: Exception) {} }
                        override fun onPeriodicNotification(t: AudioTrack?) {}
                    })
                    track.play()
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }

    /** Sinus toni, eksponensial so'nish bilan */
    private fun tone(freq: Float, dur: Float, vol: Float): ShortArray {
        val n = (rate * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i / rate.toFloat()
            val env = exp(-3.5f * t / dur)
            val s = sin(2.0 * PI * freq * t).toFloat() * env * vol
            out[i] = (s * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    /** Sukunat (pauza) */
    private fun silence(dur: Float): ShortArray = ShortArray((rate * dur).toInt())

    /** Shovqin (qilich/jang tovushi) */
    private fun noise(dur: Float, vol: Float): ShortArray {
        val n = (rate * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i / rate.toFloat()
            val env = exp(-6f * t / dur)
            val s = (Math.random().toFloat() * 2f - 1f) * env * vol
            out[i] = (s * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    /** Nog'ora (urush marshi) */
    private fun drum(dur: Float): ShortArray {
        val n = (rate * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i / rate.toFloat()
            val env = exp(-9f * t / dur)
            val s = (sin(2.0 * PI * 90f * t).toFloat() * 0.7f +
                    (Math.random().toFloat() * 2f - 1f) * 0.3f) * env * 0.5f
            out[i] = (s * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private operator fun ShortArray.plus(o: ShortArray): ShortArray {
        val r = ShortArray(this.size + o.size)
        System.arraycopy(this, 0, r, 0, this.size)
        System.arraycopy(o, 0, r, this.size, o.size)
        return r
    }

    fun release() { try { exec.shutdownNow() } catch (_: Throwable) {} }
}
