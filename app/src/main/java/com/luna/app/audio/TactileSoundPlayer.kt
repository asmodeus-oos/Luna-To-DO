package com.luna.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TactileSoundPlayer {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 44100
    private val audioMutex = Mutex()

    fun playComplete() {
        scope.launch {
            // Sweet harmonic chime: 660 Hz (E5) ascending to 880 Hz (A5)
            val durationMs = 130
            val numSamples = (durationMs * sampleRate) / 1000
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val freq = 660.0 + 220.0 * progress
                val envelope = exp(-progress * 6.0)
                val wave = (sin(2.0 * PI * freq * t) * envelope * 0.7 +
                        sin(2.0 * PI * (freq * 2.0) * t) * envelope * 0.25)
                samples[i] = (wave * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(samples)
        }
    }

    fun playUncheck() {
        scope.launch {
            // Subtle downward soft pop: 440 Hz -> 330 Hz
            val durationMs = 70
            val numSamples = (durationMs * sampleRate) / 1000
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val freq = 440.0 - 110.0 * progress
                val envelope = exp(-progress * 7.5)
                val wave = sin(2.0 * PI * freq * t) * envelope * 0.6
                samples[i] = (wave * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(samples)
        }
    }

    fun playAdd() {
        scope.launch {
            // Delicate tactile click: 750 Hz
            val durationMs = 45
            val numSamples = (durationMs * sampleRate) / 1000
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val freq = 750.0
                val envelope = exp(-progress * 9.0)
                val wave = sin(2.0 * PI * freq * t) * envelope * 0.5
                samples[i] = (wave * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(samples)
        }
    }

    fun playCelebrationFanfare() {
        scope.launch {
            // Triumphant 4-note celebration fanfare arpeggio: C5 (523Hz) -> E5 (659Hz) -> G5 (784Hz) -> C6 (1046Hz)
            val notes = listOf(
                Pair(523.25, 75),  // C5
                Pair(659.25, 75),  // E5
                Pair(783.99, 85),  // G5
                Pair(1046.50, 320) // High C6 with rich ringing decay
            )
            val totalDurationMs = notes.sumOf { it.second }
            val totalSamples = (totalDurationMs * sampleRate) / 1000
            val samples = ShortArray(totalSamples)

            var sampleOffset = 0
            for ((freq, noteMs) in notes) {
                val noteSamples = (noteMs * sampleRate) / 1000
                for (i in 0 until noteSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / noteSamples
                    val envelope = exp(-progress * 3.5)
                    // Fundamental + 2nd harmonic + 3rd harmonic for bright shimmer
                    val wave = (sin(2.0 * PI * freq * t) * envelope * 0.55 +
                            sin(2.0 * PI * (freq * 2.0) * t) * envelope * 0.30 +
                            sin(2.0 * PI * (freq * 3.0) * t) * envelope * 0.15)
                    val globalIdx = sampleOffset + i
                    if (globalIdx < totalSamples) {
                        samples[globalIdx] = (wave * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                }
                sampleOffset += noteSamples
            }
            playPcm(samples)
        }
    }

    private suspend fun playPcm(samples: ShortArray) {
        audioMutex.withLock {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
                delay((samples.size * 1000L / sampleRate) + 40)
                track.release()
            } catch (_: Exception) {
                // Audio output suppressed or unavailable
            }
        }
    }
}
