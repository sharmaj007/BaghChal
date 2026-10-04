package com.games.baghchal.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.sin

object SoundManager {
    var isMuted = false

    fun playMoveSound() {
        if (isMuted) return
        thread { playSineTone(freq = 440.0, durationMs = 90, volume = 0.15f) }
    }

    fun playCaptureSound() {
        if (isMuted) return
        thread {
            playSineTone(freq = 180.0, durationMs = 140, volume = 0.25f)
            Thread.sleep(60)
            playSineTone(freq = 120.0, durationMs = 160, volume = 0.25f)
        }
    }

    fun playWinSound() {
        if (isMuted) return
        thread {
            val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
            for (f in freqs) {
                playSineTone(freq = f, durationMs = 120, volume = 0.2f)
                Thread.sleep(90)
            }
        }
    }

    fun playLoseSound() {
        if (isMuted) return
        thread {
            val freqs = doubleArrayOf(400.0, 350.0, 300.0, 250.0)
            for (f in freqs) {
                playSineTone(freq = f, durationMs = 120, volume = 0.2f)
                Thread.sleep(110)
            }
        }
    }

    private fun playSineTone(freq: Double, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val sample = ByteArray(numSamples * 2)

            for (i in 0 until numSamples) {
                val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                val decay = (numSamples - i).toDouble() / numSamples
                val value = (sin(angle) * 32767 * volume * decay).toInt().toShort()

                sample[i * 2] = (value.toInt() and 0x00ff).toByte()
                sample[i * 2 + 1] = ((value.toInt() and 0xff00) shr 8).toByte()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(sample.size)
                .build()

            track.write(sample, 0, sample.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 30)
            track.release()
        } catch (_: Exception) {}
    }
}
