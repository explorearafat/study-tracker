package com.example.util

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import kotlinx.coroutines.*

enum class AmbientSoundType(val displayName: String, val iconName: String) {
    OFF("Mute / Off", "volume_off"),
    RAIN("Soft Rain", "water_drop"),
    OCEAN("Deep Ocean Waves", "waves"),
    WHITE_NOISE("Pure White Noise", "graphic_eq"),
    CAFE("Cozy Cafe & Fire", "local_cafe"),
    ALPHA_WAVE("Alpha Focus Beat (10Hz)", "psychology"),
    WIND("Gentle Forest Wind", "air")
}

object AmbientSoundPlayer {

    private const val SAMPLE_RATE = 44100
    private var audioTrack: AudioTrack? = null
    private var playJob: Job? = null
    private val playerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var currentType: AmbientSoundType = AmbientSoundType.OFF
    private var volumeLevel: Float = 0.5f
    private var isPlaying: Boolean = false

    fun getCurrentType(): AmbientSoundType = currentType
    fun getVolume(): Float = volumeLevel
    fun isAudioActive(): Boolean = isPlaying && currentType != AmbientSoundType.OFF

    fun setVolume(volume: Float) {
        volumeLevel = volume.coerceIn(0f, 1f)
        try {
            audioTrack?.setVolume(volumeLevel)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSound(type: AmbientSoundType) {
        if (type == currentType && isPlaying) return
        stopSound()
        currentType = type
        if (type == AmbientSoundType.OFF) return

        isPlaying = true
        playJob = playerScope.launch {
            generateAndPlayAudio(type)
        }
    }

    fun stopSound() {
        isPlaying = false
        playJob?.cancel()
        playJob = null
        try {
            audioTrack?.let { track ->
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioTrack = null
    }

    private suspend fun generateAndPlayAudio(type: AmbientSoundType) = withContext(Dispatchers.Default) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

        try {
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )

            setVolume(volumeLevel)
            audioTrack?.play()

            val pcmBuffer = ShortArray(bufferSize)
            var phase1 = 0.0
            var phase2 = 0.0
            var lastSample = 0.0
            var pinkState0 = 0.0
            var pinkState1 = 0.0
            var pinkState2 = 0.0
            var windPhase = 0.0

            while (isPlaying && isActive) {
                for (i in pcmBuffer.indices) {
                    val sample: Double = when (type) {
                        AmbientSoundType.RAIN -> {
                            val white = (Math.random() * 2.0) - 1.0
                            pinkState0 = 0.99886 * pinkState0 + white * 0.0555179
                            pinkState1 = 0.99332 * pinkState1 + white * 0.0750759
                            pinkState2 = 0.96900 * pinkState2 + white * 0.1538520
                            val pink = pinkState0 + pinkState1 + pinkState2 + white * 0.5362
                            val filtered = 0.92 * lastSample + 0.08 * (pink * 0.15)
                            lastSample = filtered

                            val drop = if (Math.random() < 0.0003) (Math.random() * 0.8) else 0.0
                            filtered + drop
                        }

                        AmbientSoundType.OCEAN -> {
                            windPhase += 2.0 * Math.PI / (SAMPLE_RATE * 6.0)
                            val lfo = (Math.sin(windPhase) + 1.0) / 2.0
                            val white = (Math.random() * 2.0) - 1.0
                            val alpha = 0.98 - (lfo * 0.04)
                            lastSample = alpha * lastSample + (1.0 - alpha) * white
                            lastSample * (0.3 + 0.7 * lfo) * 0.6
                        }

                        AmbientSoundType.WHITE_NOISE -> {
                            ((Math.random() * 2.0) - 1.0) * 0.25
                        }

                        AmbientSoundType.CAFE -> {
                            val white = ((Math.random() * 2.0) - 1.0) * 0.12
                            lastSample = 0.95 * lastSample + 0.05 * white
                            val crackle = if (Math.random() < 0.0008) (Math.random() * 0.6 - 0.3) else 0.0
                            lastSample + crackle
                        }

                        AmbientSoundType.ALPHA_WAVE -> {
                            val freqBase = 180.0
                            val freqBeat = 10.0
                            phase1 += 2.0 * Math.PI * freqBase / SAMPLE_RATE
                            phase2 += 2.0 * Math.PI * (freqBase + freqBeat) / SAMPLE_RATE

                            val tone1 = Math.sin(phase1)
                            val tone2 = Math.sin(phase2)
                            (tone1 + tone2) * 0.2
                        }

                        AmbientSoundType.WIND -> {
                            windPhase += 2.0 * Math.PI / (SAMPLE_RATE * 8.0)
                            val modulation = (Math.sin(windPhase) + Math.sin(windPhase * 0.3)) * 0.5 + 0.5
                            val white = (Math.random() * 2.0) - 1.0
                            val alpha = 0.93 - (modulation * 0.05)
                            lastSample = alpha * lastSample + (1.0 - alpha) * white
                            lastSample * (0.2 + 0.8 * modulation) * 0.5
                        }

                        AmbientSoundType.OFF -> 0.0
                    }

                    pcmBuffer[i] = (sample.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
                }

                audioTrack?.write(pcmBuffer, 0, pcmBuffer.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
