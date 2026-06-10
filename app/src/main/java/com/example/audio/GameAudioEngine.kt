package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.sin

class GameAudioEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var musicJob: Job? = null

    @Volatile
    var musicVolume: Float = 0.5f
    @Volatile
    var sfxVolume: Float = 0.7f

    private val sampleRate = 22050

    // Sound effect generator
    fun playSfx(type: SfxType) {
        if (sfxVolume <= 0.05f) return
        scope.launch {
            try {
                val samples = when (type) {
                    SfxType.JUMP -> generateSweep(300f, 600f, 0.15f)
                    SfxType.CROUCH -> generateSweep(400f, 150f, 0.15f)
                    SfxType.KEY_PICKUP -> generateChime(listOf(660f, 880f, 1320f), 0.25f)
                    SfxType.UNLOCK -> generateNoiseClick(0.1f)
                    SfxType.CAUGHT -> generateSiren(600f, 150f, 0.5f)
                    SfxType.LEVEL_WIN -> generateChime(listOf(523.25f, 659.25f, 783.99f, 1046.50f), 0.4f)
                }
                playBuffer(samples, sfxVolume)
            } catch (e: Exception) {
                Log.e("GameAudioEngine", "Error playing SFX", e)
            }
        }
    }

    // Suspenseful Background Music loop
    fun startMusic() {
        if (musicJob?.isActive == true) return
        musicJob = scope.launch {
            // Suspenseful minimalist bass sequence for stealth gameplay
            val bassLine = listOf(73.42f, 82.41f, 87.31f, 82.41f, 65.41f, 73.42f) // D2, E2, F2, E2, C2, D2
            var noteIndex = 0
            while (isActive) {
                val volume = musicVolume
                if (volume > 0.05f) {
                    val freq = bassLine[noteIndex]
                    // Play a soft pulsing bass wave
                    val buffer = generatePulsedBass(freq, 0.6f)
                    playBuffer(buffer, volume * 0.4f) // softer background volume
                } else {
                    delay(300)
                }
                noteIndex = (noteIndex + 1) % bassLine.size
                delay(800) // Timing between notes
            }
        }
    }

    fun stopMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    fun release() {
        stopMusic()
        scope.cancel()
    }

    private fun playBuffer(samples: ShortArray, vol: Float) {
        val bufferSize = samples.size * 2
        val audioTrack = AudioTrack.Builder()
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
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(samples, 0, samples.size)
        // Convert general float volume to AudioTrack volume limits
        val targetVolume = vol.coerceIn(0f, 1f)
        audioTrack.setVolume(targetVolume)
        audioTrack.play()

        // Schedule release when sound completes
        val durationMs = (samples.size.toFloat() / sampleRate * 1000).toLong()
        scope.launch {
            delay(durationMs + 100)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    private fun generateSweep(startFreq: Float, endFreq: Float, duration: Float): ShortArray {
        val numSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / numSamples
            val freq = startFreq + (endFreq - startFreq) * progress
            val angle = 2.0 * Math.PI * freq * t
            // Add slight fade out to prevent pop
            val envelope = if (i > numSamples - 1000) (numSamples - i).toFloat() / 1000f else 1.0f
            samples[i] = (sin(angle) * 32767 * 0.5f * envelope).toInt().toShort()
        }
        return samples
    }

    private fun generateChime(frequencies: List<Float>, durationSec: Float): ShortArray {
        // Compose multiple notes layered or sequential. Let's make sequential notes.
        val totalSamples = (sampleRate * durationSec).toInt()
        val noteSamples = totalSamples / frequencies.size
        val samples = ShortArray(totalSamples)

        for (n in frequencies.indices) {
            val freq = frequencies[n]
            val offset = n * noteSamples
            for (i in 0 until noteSamples) {
                val t = i.toFloat() / sampleRate
                val angle = 2.0 * Math.PI * freq * t
                // Clean envelope: rapid decay
                val envelope = (1.0f - (i.toFloat() / noteSamples)).coerceIn(0f, 1f)
                val value = (sin(angle) * 32767 * 0.4f * envelope).toInt()
                samples[offset + i] = value.toShort()
            }
        }
        return samples
    }

    private fun generateNoiseClick(durationSec: Float): ShortArray {
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        var randomState = 123456789
        for (i in 0 until numSamples) {
            // Linear congruential generator for white-ish noise
            randomState = randomState * 1103515245 + 12345
            val noise = (randomState shr 16) / 32768.0f
            // Low pass/band band feel by integrating some frequency
            val envelope = (1.0f - (i.toFloat() / numSamples)).coerceIn(0f, 1f)
            samples[i] = (noise * 32767 * 0.25f * envelope).toInt().toShort()
        }
        return samples
    }

    private fun generateSiren(startFreq: Float, endFreq: Float, durationSec: Float): ShortArray {
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            // repeating sweep back and forth
            val phase = (t * 8f) % 2.0f // 8 sweeps per second
            val sweepProgress = if (phase < 1.0f) phase else 2.0f - phase
            val freq = startFreq + (endFreq - startFreq) * sweepProgress
            val angle = 2.0 * Math.PI * freq * t
            val envelope = (1.0f - (i.toFloat() / numSamples) * 0.3f).coerceIn(0f, 1f) // slight fade
            samples[i] = (sin(angle) * 32767 * 0.6f * envelope).toInt().toShort()
        }
        return samples
    }

    private fun generatePulsedBass(freq: Float, durationSec: Float): ShortArray {
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val angle = 2.0 * Math.PI * freq * t
            // pulse envelope: soft build up and decay
            val progress = i.toFloat() / numSamples
            val envelope = if (progress < 0.2f) {
                progress / 0.2f
            } else {
                ((1.0f - progress) / 0.8f).coerceIn(0f, 1f)
            }
            samples[i] = (sin(angle) * 32767 * 0.3f * envelope).toInt().toShort()
        }
        return samples
    }

    enum class SfxType {
        JUMP,
        CROUCH,
        KEY_PICKUP,
        UNLOCK,
        CAUGHT,
        LEVEL_WIN
    }
}
