package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.PlaybackParams
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.ContextCompat
import com.example.model.VoiceModPreset
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class ReadyVoiceDemoPhrase(
    val index: Int,
    val shortLabelAr: String,
    val arabicSpeech: String,
    val englishSpeech: String
)

val READY_VOICE_DEMO_PHRASES = listOf(
    ReadyVoiceDemoPhrase(
        index = 0,
        shortLabelAr = "🔥 هجوم وتغطية",
        arabicSpeech = "هجوم على العدو الآن يا شباب، غطوني أنا متقدم",
        englishSpeech = "Attack the enemy now team, cover me I am advancing"
    ),
    ReadyVoiceDemoPhrase(
        index = 1,
        shortLabelAr = "👋 ترحيب بالفريق",
        arabicSpeech = "مرحباً يا أصدقاء، هذا صوتي الجديد داخل اللعبة واضح وصافي",
        englishSpeech = "Hello friends, this is my new crystal clear voice inside the game"
    ),
    ReadyVoiceDemoPhrase(
        index = 2,
        shortLabelAr = "🚨 نداء فزعة",
        arabicSpeech = "أحتاج دعم سريع في موقعي، العدو قريب جداً مني",
        englishSpeech = "I need quick backup at my position, the enemy is very close"
    )
)

/**
 * Studio-Grade Real-Time Microphone & No-Mic Ready Voice Changer Engine.
 * Supports:
 * 1. Instant Ready-Made Voice Demo WITHOUT speaking (TTS-to-DSP PCM + Studio Formant Vocal Synthesizer)
 * 2. Crystal-Clear Push-To-Talk / Voice Clip Test (Record your voice -> Hear it transformed with zero feedback)
 * 3. Live Microphone Streaming (AudioRecord -> Noise Gate + Formant DSP + Pitch -> AudioTrack)
 */
class RealTimeVoiceChangerEngine(private val context: Context) {

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isLiveMicActive = MutableStateFlow(false)
    val isLiveMicActive: StateFlow<Boolean> = _isLiveMicActive.asStateFlow()

    private val _isRecordingClip = MutableStateFlow(false)
    val isRecordingClip: StateFlow<Boolean> = _isRecordingClip.asStateFlow()

    private val _isPlayingClip = MutableStateFlow(false)
    val isPlayingClip: StateFlow<Boolean> = _isPlayingClip.asStateFlow()

    private val _micInputLevel = MutableStateFlow(0f)
    val micInputLevel: StateFlow<Float> = _micInputLevel.asStateFlow()

    private val _activePreset = MutableStateFlow(VoiceModPreset.ORIGINAL)
    val activePreset: StateFlow<VoiceModPreset> = _activePreset.asStateFlow()

    private val _selectedPhraseIndex = MutableStateFlow(0)
    val selectedPhraseIndex: StateFlow<Int> = _selectedPhraseIndex.asStateFlow()

    private val _noiseGateEnabled = MutableStateFlow(true)
    val noiseGateEnabled: StateFlow<Boolean> = _noiseGateEnabled.asStateFlow()

    private val _voiceStatusText = MutableStateFlow("🔊 اضغط على أي صوت بالأسفل لسماعه فوراً بدون ما تتكلم!")
    val voiceStatusText: StateFlow<String> = _voiceStatusText.asStateFlow()

    private var liveLoopJob: Job? = null
    private var clipRecordJob: Job? = null
    private var clipPlayJob: Job? = null

    @Volatile
    private var lastRecordedPcm: ShortArray? = null

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var isArabicTtsSupported = false

    init {
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val arResult = tts?.setLanguage(Locale("ar"))
                    isArabicTtsSupported = arResult != TextToSpeech.LANG_MISSING_DATA &&
                        arResult != TextToSpeech.LANG_NOT_SUPPORTED
                    if (!isArabicTtsSupported) {
                        val enResult = tts?.setLanguage(Locale.US)
                        isTtsReady = enResult != TextToSpeech.LANG_MISSING_DATA &&
                            enResult != TextToSpeech.LANG_NOT_SUPPORTED
                    } else {
                        isTtsReady = true
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun setPreset(preset: VoiceModPreset) {
        _activePreset.value = preset
        _voiceStatusText.value = "الفلتر النشط: ${preset.arabicName} — ${preset.subtitleAr}"
    }

    fun setNoiseGateEnabled(enabled: Boolean) {
        _noiseGateEnabled.value = enabled
    }

    /**
     * Plays a ready-made voice sample WITHOUT needing the user to speak into the microphone!
     * Uses TTS-to-PCM synthesis if available on device, or falls back to our built-in Studio Human
     * Formant Vocal Synthesizer so audio ALWAYS plays through AudioTrack with the exact preset DSP.
     */
    fun playReadyVoiceDemoWithoutSpeaking(
        preset: VoiceModPreset = _activePreset.value,
        phraseIndex: Int = _selectedPhraseIndex.value
    ) {
        val safePhraseIdx = phraseIndex.coerceIn(0, READY_VOICE_DEMO_PHRASES.lastIndex)
        _selectedPhraseIndex.value = safePhraseIdx
        _activePreset.value = preset

        stopLiveMicStream()
        _isRecordingClip.value = false
        clipRecordJob?.cancel()
        clipPlayJob?.cancel()

        val phrase = READY_VOICE_DEMO_PHRASES[safePhraseIdx]
        clipPlayJob = engineScope.launch {
            _isPlayingClip.value = true
            _voiceStatusText.value = "🔊 جاري إسماعك (${preset.arabicName}) بدون مايك — عبارة: ${phrase.shortLabelAr}"

            val sampleRate = 44100
            // 1. Try synthesizing real spoken phrase via TTS to WAV PCM so it runs through our Studio DSP
            val ttsPcm = synthesizePhraseViaTtsToPcm44k(phrase, preset, sampleRate)
            val demoPcm = if (ttsPcm != null && ttsPcm.size > 3000) {
                ttsPcm
            } else {
                // 2. Guaranteed Built-in Studio Formant Vocal Synthesizer (100% works on all emulators/devices)
                generateStudioVocalDemoPcm(
                    preset = preset,
                    phraseIndex = safePhraseIdx,
                    sampleRate = sampleRate
                )
            }

            playRawPcmOnAudioTrack(
                rawPcm = demoPcm,
                preset = preset,
                sampleRate = sampleRate,
                isReadyDemo = true,
                phraseLabel = phrase.shortLabelAr
            )
        }
    }

    /**
     * Called when user taps a preset card: selects the preset and immediately plays a voice preview
     * (either their real recorded voice if they actually spoke earlier, or the ready-made no-mic voice demo).
     */
    fun previewVoicePreset(preset: VoiceModPreset) {
        setPreset(preset)
        val savedClip = lastRecordedPcm
        if (savedClip != null && savedClip.size > 4000 && !_isLiveMicActive.value && !_isRecordingClip.value) {
            playProcessedPcmBuffer(savedClip, preset)
        } else {
            playReadyVoiceDemoWithoutSpeaking(preset, _selectedPhraseIndex.value)
        }
    }

    /**
     * Toggles continuous real-time microphone voice changer (AudioRecord -> DSP -> AudioTrack).
     */
    @SuppressLint("MissingPermission")
    fun toggleLiveMicStream(): Boolean {
        if (_isLiveMicActive.value) {
            stopLiveMicStream()
            return false
        }
        if (!hasRecordAudioPermission()) {
            _voiceStatusText.value = "يرجى السماح بإذن المايكروفون لتشغيل مغير الصوت المباشر"
            return false
        }

        stopClipRecordingAndPlayback()
        _isLiveMicActive.value = true
        _voiceStatusText.value = "🔴 المايك المباشر يعمل الآن بفلتر: ${_activePreset.value.arabicName}"

        liveLoopJob = engineScope.launch {
            val sampleRate = 44100
            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            var recorder: AudioRecord? = null
            var player: AudioTrack? = null
            var ns: NoiseSuppressor? = null
            var aec: AcousticEchoCanceler? = null
            var agc: AutomaticGainControl? = null

            try {
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuf * 2
                )
                if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                    _isLiveMicActive.value = false
                    _voiceStatusText.value = "تعذر فتح المايكروفون على هذا الجهاز"
                    return@launch
                }

                val sessionId = recorder.audioSessionId
                if (NoiseSuppressor.isAvailable()) {
                    ns = NoiseSuppressor.create(sessionId)?.apply { enabled = true }
                }
                if (AcousticEchoCanceler.isAvailable()) {
                    aec = AcousticEchoCanceler.create(sessionId)?.apply { enabled = true }
                }
                if (AutomaticGainControl.isAvailable()) {
                    agc = AutomaticGainControl.create(sessionId)?.apply { enabled = true }
                }

                player = createCleanAudioTrack(sampleRate, minBuf * 2, _activePreset.value)
                recorder.startRecording()
                player.play()

                val inBuffer = ShortArray(1024)
                val outBuffer = ShortArray(1024)
                var appliedPreset = _activePreset.value
                val dspState = DspFilterState()

                while (isActive && _isLiveMicActive.value) {
                    val currentPreset = _activePreset.value
                    if (currentPreset != appliedPreset) {
                        appliedPreset = currentPreset
                        applyHardwarePitchParams(player, appliedPreset)
                    }

                    val readCount = recorder.read(inBuffer, 0, inBuffer.size)
                    if (readCount > 0) {
                        val level = processCleanVoiceBuffer(
                            input = inBuffer,
                            output = outBuffer,
                            count = readCount,
                            sampleRate = sampleRate,
                            preset = appliedPreset,
                            useNoiseGate = _noiseGateEnabled.value,
                            state = dspState
                        )
                        _micInputLevel.value = level
                        player.write(outBuffer, 0, readCount)
                    }
                }
            } catch (_: Exception) {
            } finally {
                _micInputLevel.value = 0f
                _isLiveMicActive.value = false
                try { ns?.release() } catch (_: Exception) {}
                try { aec?.release() } catch (_: Exception) {}
                try { agc?.release() } catch (_: Exception) {}
                try { recorder?.stop() } catch (_: Exception) {}
                try { recorder?.release() } catch (_: Exception) {}
                try { player?.stop() } catch (_: Exception) {}
                try { player?.release() } catch (_: Exception) {}
            }
        }
        return true
    }

    fun stopLiveMicStream() {
        _isLiveMicActive.value = false
        liveLoopJob?.cancel()
        liveLoopJob = null
        _micInputLevel.value = 0f
        _voiceStatusText.value = "تم إيقاف المايك المباشر"
    }

    /**
     * Records up to 4 seconds of the user's voice (or stops early when called again) and immediately
     * plays it back with crystal-clear studio pitch & formant transformation and zero echo feedback.
     */
    @SuppressLint("MissingPermission")
    fun startOrStopVoiceTestRecording() {
        if (_isRecordingClip.value) {
            _isRecordingClip.value = false
            return
        }
        if (!hasRecordAudioPermission()) {
            _voiceStatusText.value = "يرجى السماح بإذن المايكروفون لتجربة صوتك"
            return
        }

        stopLiveMicStream()
        clipPlayJob?.cancel()
        _isPlayingClip.value = false
        _isRecordingClip.value = true
        _voiceStatusText.value = "🎙️ تحدث الآن بالمايك... (اضغط إيقاف أو انتظر 4 ثوانٍ لسماع صوتك المفلتر)"

        clipRecordJob = engineScope.launch {
            val sampleRate = 44100
            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            var recorder: AudioRecord? = null
            var ns: NoiseSuppressor? = null
            val recordedShorts = ArrayList<Short>(sampleRate * 4)

            try {
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuf * 2
                )
                if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                    _isRecordingClip.value = false
                    _voiceStatusText.value = "تعذر الوصول للمايكروفون"
                    return@launch
                }

                if (NoiseSuppressor.isAvailable()) {
                    ns = NoiseSuppressor.create(recorder.audioSessionId)?.apply { enabled = true }
                }

                recorder.startRecording()
                val chunk = ShortArray(1024)
                val startTime = System.currentTimeMillis()
                var maxPeakAbs = 0

                while (isActive && _isRecordingClip.value && (System.currentTimeMillis() - startTime < 4200L)) {
                    val read = recorder.read(chunk, 0, chunk.size)
                    if (read > 0) {
                        var sumSq = 0.0
                        for (i in 0 until read) {
                            val s = chunk[i]
                            recordedShorts.add(s)
                            val absVal = abs(s.toInt())
                            if (absVal > maxPeakAbs) maxPeakAbs = absVal
                            sumSq += (s.toDouble() * s.toDouble())
                        }
                        val rms = sqrt(sumSq / read) / 12000.0
                        _micInputLevel.value = rms.toFloat().coerceIn(0f, 1f)
                    }
                }
                if (recordedShorts.size > 4000 && maxPeakAbs >= 850) {
                    val pcmArray = ShortArray(recordedShorts.size) { recordedShorts[it] }
                    lastRecordedPcm = pcmArray
                } else if (maxPeakAbs < 850) {
                    recordedShorts.clear()
                }
            } catch (_: Exception) {
            } finally {
                _micInputLevel.value = 0f
                _isRecordingClip.value = false
                try { ns?.release() } catch (_: Exception) {}
                try { recorder?.stop() } catch (_: Exception) {}
                try { recorder?.release() } catch (_: Exception) {}
            }

            val validSaved = lastRecordedPcm
            if (recordedShorts.size > 4000 && validSaved != null) {
                playProcessedPcmBuffer(validSaved, _activePreset.value)
            } else {
                // User didn't speak into the mic (or emulator has no mic input): automatically play ready-made voice demo!
                playReadyVoiceDemoWithoutSpeaking(_activePreset.value, _selectedPhraseIndex.value)
            }
        }
    }

    fun replayLastRecordedVoice() {
        val pcm = lastRecordedPcm
        if (pcm == null || pcm.size < 4000) {
            playReadyVoiceDemoWithoutSpeaking(_activePreset.value, _selectedPhraseIndex.value)
            return
        }
        playProcessedPcmBuffer(pcm, _activePreset.value)
    }

    private fun playProcessedPcmBuffer(rawPcm: ShortArray, preset: VoiceModPreset) {
        clipPlayJob?.cancel()
        clipPlayJob = engineScope.launch {
            playRawPcmOnAudioTrack(
                rawPcm = rawPcm,
                preset = preset,
                sampleRate = 44100,
                isReadyDemo = false,
                phraseLabel = ""
            )
        }
    }

    private suspend fun playRawPcmOnAudioTrack(
        rawPcm: ShortArray,
        preset: VoiceModPreset,
        sampleRate: Int,
        isReadyDemo: Boolean,
        phraseLabel: String
    ) {
        _isPlayingClip.value = true
        _voiceStatusText.value = if (isReadyDemo) {
            "🔊 جاري إسماعك (${preset.arabicName}) بدون مايك — $phraseLabel"
        } else {
            "🔊 جاري تشغيل صوتك بفلتر: ${preset.arabicName} (صوت نقي HD)"
        }

        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        var player: AudioTrack? = null
        try {
            player = createCleanAudioTrack(sampleRate, minBuf * 2, preset)
            player.play()

            val dspState = DspFilterState()
            val chunkSize = 1024
            val inChunk = ShortArray(chunkSize)
            val outChunk = ShortArray(chunkSize)
            var offset = 0

            while ( kotlinx.coroutines.currentCoroutineContext().isActive && _isPlayingClip.value && offset < rawPcm.size) {
                val count = minOf(chunkSize, rawPcm.size - offset)
                for (i in 0 until count) {
                    inChunk[i] = rawPcm[offset + i]
                }
                val level = processCleanVoiceBuffer(
                    input = inChunk,
                    output = outChunk,
                    count = count,
                    sampleRate = sampleRate,
                    preset = preset,
                    useNoiseGate = if (isReadyDemo) false else _noiseGateEnabled.value,
                    state = dspState
                )
                _micInputLevel.value = level.coerceAtLeast(0.18f)
                player.write(outChunk, 0, count)
                offset += count
            }
            delay(120L)
        } catch (_: Exception) {
        } finally {
            _micInputLevel.value = 0f
            _isPlayingClip.value = false
            _voiceStatusText.value = if (isReadyDemo) {
                "✓ سمعت الآن (${preset.arabicName}) بدون مايك! اضغط على أي صوت آخر أو غيّر الجملة للتجربة"
            } else {
                "✓ تم تشغيل صوتك بفلتر ${preset.arabicName}! يمكنك تبديل الفلتر لسماع نفس تسجيلك بنبرة أخرى"
            }
            try { player?.stop() } catch (_: Exception) {}
            try { player?.release() } catch (_: Exception) {}
        }
    }

    /**
     * Attempts to synthesize the phrase using Android TTS into a temporary WAV file and extract
     * 44.1kHz 16-bit mono PCM samples so they pass through our full Studio Formant & Robot DSP.
     */
    private suspend fun synthesizePhraseViaTtsToPcm44k(
        phrase: ReadyVoiceDemoPhrase,
        preset: VoiceModPreset,
        targetSampleRate: Int
    ): ShortArray? {
        val engine = tts ?: return null
        if (!isTtsReady) return null

        val tempFile = File(context.cacheDir, "tts_voice_demo_${preset.id}.wav")
        return try {
            if (tempFile.exists()) tempFile.delete()
            val deferred = CompletableDeferred<Boolean>()
            val utteranceId = "demo_utt_${System.currentTimeMillis()}"

            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) {
                    if (id == utteranceId) deferred.complete(true)
                }
                @Deprecated("Deprecated in Java")
                override fun onError(id: String?) {
                    if (id == utteranceId) deferred.complete(false)
                }
                override fun onError(id: String?, errorCode: Int) {
                    if (id == utteranceId) deferred.complete(false)
                }
            })

            engine.setPitch(1.0f)
            engine.setSpeechRate(1.0f)
            val text = if (isArabicTtsSupported) phrase.arabicSpeech else phrase.englishSpeech
            val res = engine.synthesizeToFile(text, Bundle(), tempFile, utteranceId)
            if (res != TextToSpeech.SUCCESS) return null

            val completed = withTimeoutOrNull(480L) { deferred.await() } ?: false
            if (!completed || !tempFile.exists() || tempFile.length() <= 512L) return null

            val bytes = tempFile.readBytes()
            tempFile.delete()
            parseWavBytesToMono44kPcm(bytes, targetSampleRate)
        } catch (_: Exception) {
            try { if (tempFile.exists()) tempFile.delete() } catch (_: Exception) {}
            null
        }
    }

    private fun parseWavBytesToMono44kPcm(wavBytes: ByteArray, targetSampleRate: Int): ShortArray? {
        if (wavBytes.size < 128) return null
        val bb = ByteBuffer.wrap(wavBytes).order(ByteOrder.LITTLE_ENDIAN)
        val channels = bb.getShort(22).toInt().coerceIn(1, 2)
        val srcRate = bb.getInt(24).coerceIn(8000, 48000)

        var dataOffset = 44
        for (i in 12 until minOf(wavBytes.size - 8, 256)) {
            if (wavBytes[i] == 'd'.code.toByte() &&
                wavBytes[i + 1] == 'a'.code.toByte() &&
                wavBytes[i + 2] == 't'.code.toByte() &&
                wavBytes[i + 3] == 'a'.code.toByte()
            ) {
                dataOffset = i + 8
                break
            }
        }
        if (dataOffset >= wavBytes.size - 4) return null

        val frameBytes = 2 * channels
        val frameCount = (wavBytes.size - dataOffset) / frameBytes
        if (frameCount < 800) return null

        val monoSrc = ShortArray(frameCount)
        var maxPeak = 1
        var bytePos = dataOffset
        for (f in 0 until frameCount) {
            val s0 = bb.getShort(bytePos).toInt()
            val sample = if (channels == 2 && bytePos + 3 < wavBytes.size) {
                val s1 = bb.getShort(bytePos + 2).toInt()
                (s0 + s1) / 2
            } else {
                s0
            }
            val absVal = abs(sample)
            if (absVal > maxPeak) maxPeak = absVal
            monoSrc[f] = sample.coerceIn(-32768, 32767).toShort()
            bytePos += frameBytes
        }

        // If TTS produced silence, return null so the Built-in Formant Synthesizer takes over
        if (maxPeak < 500) return null

        val normGain = (22000f / maxPeak.toFloat()).coerceIn(0.8f, 2.8f)
        val outSize = ((frameCount.toLong() * targetSampleRate) / srcRate).toInt().coerceAtLeast(1)
        val resampled = ShortArray(outSize)
        val ratio = srcRate.toDouble() / targetSampleRate.toDouble()

        for (i in 0 until outSize) {
            val srcPos = i * ratio
            val idx0 = srcPos.toInt().coerceIn(0, frameCount - 1)
            val idx1 = (idx0 + 1).coerceIn(0, frameCount - 1)
            val frac = (srcPos - idx0).toFloat()
            val interp = monoSrc[idx0] * (1f - frac) + monoSrc[idx1] * frac
            resampled[i] = (interp * normGain).roundToInt().coerceIn(-30000, 30000).toShort()
        }
        return resampled
    }

    /**
     * Built-in Studio Formant Vocal Synthesizer (مولّد الصوت البشري الجاهز بدون مايك).
     * Generates a rich, expressive multi-syllable human vocal phrase using glottal harmonics +
     * 3 moving vowel formants (F1, F2, F3) + natural speech cadence so the user ALWAYS hears
     * crystal-clear voice audio even on emulators/devices without TTS or Microphone!
     */
    private fun generateStudioVocalDemoPcm(
        preset: VoiceModPreset,
        phraseIndex: Int,
        sampleRate: Int
    ): ShortArray {
        data class VocalSyllable(
            val durationSec: Float,
            val gapSec: Float,
            val startPitchHz: Float,
            val endPitchHz: Float,
            val f1Hz: Float,
            val f2Hz: Float,
            val f3Hz: Float,
            val intensity: Float
        )

        // Base fundamental pitch centered around natural human speech (145 Hz) because
        // createCleanAudioTrack + PlaybackParams will apply preset.pitchFactor & preset.speechRate
        // AND processCleanVoiceBuffer will apply Female/Male/Squirrel/Robot DSP!
        // We also tailor base formants slightly per preset so the vocal character is unmistakable.
        val basePitchScale = when (preset) {
            VoiceModPreset.FEMALE_NATURAL -> 1.28f
            VoiceModPreset.MALE_DEEP -> 0.78f
            VoiceModPreset.SQUIRREL_FUN -> 1.48f
            VoiceModPreset.CYBER_ROBOT -> 0.88f
            VoiceModPreset.ORIGINAL -> 1.0f
        }

        val formantScale = when (preset) {
            VoiceModPreset.FEMALE_NATURAL -> 1.18f
            VoiceModPreset.MALE_DEEP -> 0.86f
            VoiceModPreset.SQUIRREL_FUN -> 1.28f
            VoiceModPreset.CYBER_ROBOT -> 0.95f
            VoiceModPreset.ORIGINAL -> 1.0f
        }

        val syllables = when (phraseIndex % 3) {
            0 -> listOf(
                // "🔥 هجوم وتغطية": Energetic tactical command cadence (7 syllables)
                VocalSyllable(0.24f, 0.03f, 150f, 172f, 720f, 1240f, 2550f, 0.92f),
                VocalSyllable(0.30f, 0.05f, 172f, 185f, 360f, 920f, 2420f, 1.00f),
                VocalSyllable(0.22f, 0.03f, 165f, 155f, 680f, 1380f, 2600f, 0.88f),
                VocalSyllable(0.28f, 0.06f, 158f, 178f, 310f, 2180f, 2850f, 0.95f),
                VocalSyllable(0.25f, 0.04f, 176f, 162f, 520f, 1680f, 2520f, 0.90f),
                VocalSyllable(0.26f, 0.03f, 162f, 148f, 750f, 1320f, 2480f, 0.94f),
                VocalSyllable(0.36f, 0.06f, 152f, 134f, 440f, 1120f, 2400f, 0.96f)
            )
            1 -> listOf(
                // "👋 ترحيب بالفريق": Warm welcoming conversational cadence (7 syllables)
                VocalSyllable(0.26f, 0.03f, 145f, 168f, 700f, 1300f, 2550f, 0.94f),
                VocalSyllable(0.32f, 0.06f, 168f, 182f, 320f, 2220f, 2900f, 1.00f),
                VocalSyllable(0.24f, 0.04f, 162f, 174f, 540f, 1720f, 2620f, 0.90f),
                VocalSyllable(0.28f, 0.04f, 174f, 158f, 460f, 1080f, 2450f, 0.95f),
                VocalSyllable(0.24f, 0.03f, 158f, 166f, 740f, 1260f, 2520f, 0.92f),
                VocalSyllable(0.26f, 0.04f, 166f, 152f, 340f, 2140f, 2800f, 0.94f),
                VocalSyllable(0.38f, 0.06f, 152f, 132f, 620f, 1420f, 2500f, 0.96f)
            )
            else -> listOf(
                // "🚨 نداء فزعة": Urgent high-alert squad callout cadence (7 syllables)
                VocalSyllable(0.22f, 0.03f, 168f, 192f, 760f, 1360f, 2640f, 0.98f),
                VocalSyllable(0.28f, 0.04f, 192f, 178f, 340f, 2250f, 2920f, 1.00f),
                VocalSyllable(0.24f, 0.04f, 175f, 188f, 520f, 1180f, 2480f, 0.95f),
                VocalSyllable(0.30f, 0.05f, 188f, 164f, 710f, 1420f, 2580f, 0.96f),
                VocalSyllable(0.22f, 0.03f, 164f, 176f, 380f, 2080f, 2780f, 0.92f),
                VocalSyllable(0.26f, 0.04f, 176f, 158f, 580f, 1580f, 2540f, 0.94f),
                VocalSyllable(0.34f, 0.06f, 158f, 138f, 450f, 1050f, 2420f, 0.95f)
            )
        }

        val totalSec = syllables.sumOf { (it.durationSec + it.gapSec).toDouble() }.toFloat()
        val totalSamples = (totalSec * sampleRate).toInt().coerceAtLeast(sampleRate)
        val out = ShortArray(totalSamples)

        var sampleCursor = 0
        var phase = 0.0
        val twoPi = 2.0 * PI

        for (syl in syllables) {
            val sylSamples = (syl.durationSec * sampleRate).toInt()
            val gapSamples = (syl.gapSec * sampleRate).toInt()
            val f1 = syl.f1Hz * formantScale
            val f2 = syl.f2Hz * formantScale
            val f3 = syl.f3Hz * formantScale
            val bw1 = 95f
            val bw2 = 135f
            val bw3 = 185f

            for (n in 0 until sylSamples) {
                if (sampleCursor >= totalSamples) break
                val progress = n.toFloat() / sylSamples.coerceAtLeast(1).toFloat()
                val tSec = sampleCursor.toDouble() / sampleRate.toDouble()

                // Natural syllable envelope (smooth 18ms vocal attack, sustained vowel core, 35ms release)
                val attackEnv = (progress / 0.12f).coerceIn(0f, 1f)
                val releaseEnv = ((1f - progress) / 0.20f).coerceIn(0f, 1f)
                val syllableEnv = attackEnv * releaseEnv * syl.intensity

                // Fundamental pitch contour with natural human micro-vibrato (5.5 Hz)
                val vibrato = if (preset == VoiceModPreset.CYBER_ROBOT) {
                    0f
                } else {
                    (sin(twoPi * 5.5 * tSec) * 2.8).toFloat()
                }
                val f0 = (syl.startPitchHz + (syl.endPitchHz - syl.startPitchHz) * progress) * basePitchScale + vibrato

                // Multi-harmonic vocal tract synthesis weighted by vowel formants (F1, F2, F3)
                var sampleVal = 0.0
                val maxHarmonic = minOf(16, ((sampleRate * 0.42f) / f0).toInt().coerceAtLeast(4))
                for (h in 1..maxHarmonic) {
                    val freq = h * f0
                    val d1 = (freq - f1) / bw1
                    val d2 = (freq - f2) / bw2
                    val d3 = (freq - f3) / bw3
                    val formantGain = 0.22f +
                        1.35f * exp((-0.5f * d1 * d1).toDouble()).toFloat() +
                        0.95f * exp((-0.5f * d2 * d2).toDouble()).toFloat() +
                        0.52f * exp((-0.5f * d3 * d3).toDouble()).toFloat()

                    val harmonicTilt = 1.0 / Math.pow(h.toDouble(), 1.12)
                    sampleVal += sin(phase * h) * harmonicTilt * formantGain
                }

                phase += (twoPi * f0) / sampleRate.toDouble()
                if (phase > twoPi * 64.0) phase -= twoPi * 64.0

                val pcm = (sampleVal * syllableEnv * 7600.0).roundToInt().coerceIn(-29500, 29500).toShort()
                out[sampleCursor++] = pcm
            }

            // Inter-syllable breathing gap
            for (g in 0 until gapSamples) {
                if (sampleCursor >= totalSamples) break
                out[sampleCursor++] = 0
            }
        }

        return out
    }

    private fun stopClipRecordingAndPlayback() {
        _isRecordingClip.value = false
        _isPlayingClip.value = false
        clipRecordJob?.cancel()
        clipPlayJob?.cancel()
    }

    fun stopAllVoiceActivity() {
        stopLiveMicStream()
        stopClipRecordingAndPlayback()
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
    }

    fun release() {
        stopAllVoiceActivity()
        try {
            tts?.shutdown()
        } catch (_: Exception) {
        }
    }

    private fun createCleanAudioTrack(
        sampleRate: Int,
        bufferSizeBytes: Int,
        preset: VoiceModPreset
    ): AudioTrack {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSizeBytes)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        applyHardwarePitchParams(track, preset)
        return track
    }

    private fun applyHardwarePitchParams(track: AudioTrack, preset: VoiceModPreset) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = PlaybackParams()
                    .allowDefaults()
                    .setPitch(preset.pitchFactor.coerceIn(0.60f, 1.80f))
                    .setSpeed(preset.speechRate.coerceIn(0.85f, 1.25f))
                track.playbackParams = params
            } catch (_: Exception) {
            }
        }
    }

    private class DspFilterState {
        var prevIn: Float = 0f
        var prevHpOut: Float = 0f
        var lowPassOut: Float = 0f
        var envelope: Float = 0f
        var robotPhase: Double = 0.0
        val delayRing = FloatArray(512)
        var ringIdx: Int = 0
    }

    /**
     * Studio-clean DSP voice processor:
     * 1. High-pass DC & rumble removal (removes mic thumps below 85Hz)
     * 2. Smooth envelope Noise Gate (removes background hiss without clipping words)
     * 3. Preset-specific Formant EQ & Robot Ring Modulation
     * 4. Soft knee limiter to prevent any distortion
     */
    private fun processCleanVoiceBuffer(
        input: ShortArray,
        output: ShortArray,
        count: Int,
        sampleRate: Int,
        preset: VoiceModPreset,
        useNoiseGate: Boolean,
        state: DspFilterState
    ): Float {
        var peakAbs = 0f
        val twoPi = 2.0 * Math.PI
        // 58Hz clean carrier for Cyber Robot voice
        val robotPhaseInc = (twoPi * 58.0) / sampleRate.toDouble()

        for (i in 0 until count) {
            val raw = input[i].toFloat() / 32768f

            // 1. High-pass filter at ~85Hz to eliminate DC offset & breath pops
            val hpOut = 0.988f * (state.prevHpOut + raw - state.prevIn)
            state.prevIn = raw
            state.prevHpOut = hpOut

            // 2. Smooth envelope detector for Noise Gate
            val absSample = abs(hpOut)
            state.envelope = if (absSample > state.envelope) {
                0.92f * state.envelope + 0.08f * absSample
            } else {
                0.997f * state.envelope + 0.003f * absSample
            }
            if (state.envelope > peakAbs) {
                peakAbs = state.envelope
            }

            // Soft noise gate (silences static when user isn't speaking, opens smoothly when speaking)
            val gateGain = if (!useNoiseGate) {
                1.0f
            } else {
                when {
                    state.envelope < 0.006f -> 0.05f
                    state.envelope < 0.018f -> ((state.envelope - 0.006f) / 0.012f).coerceIn(0.05f, 1.0f)
                    else -> 1.0f
                }
            }

            val gated = hpOut * gateGain

            // 3. Formant & timbre shaping per preset
            state.lowPassOut = 0.80f * state.lowPassOut + 0.20f * gated
            val highFreqComponent = gated - state.lowPassOut

            val shaped = when (preset) {
                VoiceModPreset.FEMALE_NATURAL -> {
                    // Soften heavy chest bass & enhance clear upper vocal formants for natural female timbre
                    (state.lowPassOut * 0.72f + highFreqComponent * 1.48f) * 1.18f
                }
                VoiceModPreset.MALE_DEEP -> {
                    // Boost warm chest resonance & smooth harsh highs for rich deep male timbre
                    (state.lowPassOut * 1.45f + highFreqComponent * 0.85f) * 1.15f
                }
                VoiceModPreset.SQUIRREL_FUN -> {
                    // Crisp, bright vocal presence so fast squirrel pitch stays 100% intelligible
                    (state.lowPassOut * 0.78f + highFreqComponent * 1.42f) * 1.18f
                }
                VoiceModPreset.CYBER_ROBOT -> {
                    // Phase-continuous cosine ring modulation + short metallic comb reflection
                    state.robotPhase += robotPhaseInc
                    if (state.robotPhase > twoPi) state.robotPhase -= twoPi
                    val ringMod = (0.58f + 0.42f * cos(state.robotPhase).toFloat())
                    val delayed = state.delayRing[state.ringIdx]
                    val comb = gated * ringMod + delayed * 0.28f
                    state.delayRing[state.ringIdx] = comb
                    state.ringIdx = (state.ringIdx + 1) % 340
                    comb * 1.22f
                }
                VoiceModPreset.ORIGINAL -> {
                    // Studio HD clarity boost
                    (state.lowPassOut * 1.05f + highFreqComponent * 1.22f) * 1.12f
                }
            }

            // 4. Soft-knee studio limiter (prevents any harsh digital clipping)
            val limited = when {
                shaped > 0.85f -> 0.85f + (shaped - 0.85f) * 0.22f
                shaped < -0.85f -> -0.85f + (shaped + 0.85f) * 0.22f
                else -> shaped
            }.coerceIn(-0.99f, 0.99f)

            output[i] = (limited * 32767f).roundToInt().coerceIn(-32768, 32767).toShort()
        }

        return (peakAbs * 3.2f).coerceIn(0f, 1f)
    }
}
