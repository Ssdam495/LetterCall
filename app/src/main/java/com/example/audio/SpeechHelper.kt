package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class SpeechHelper(
    private val context: Context,
    private val onTtsSpeakingChanged: (Boolean) -> Unit,
    private val onSpeechRecognized: (String) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit,
    private val onErrorOccurred: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false
    private var toneGenerator: ToneGenerator? = null
    private var isListening = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        mainHandler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createRecognitionListener())
                    }
                } else {
                    Log.w("SpeechHelper", "SpeechRecognizer not available on device")
                }
            } catch (e: Exception) {
                Log.e("SpeechHelper", "Error initializing speech recognizer", e)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val arabic = Locale("ar")
                val result = engine.setLanguage(arabic)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.language = Locale.getDefault()
                }
                // Cute cartoon voice tuning: slightly higher pitch, friendly tempo
                engine.setPitch(1.35f)
                engine.setSpeechRate(0.95f)

                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        mainHandler.post { onTtsSpeakingChanged(true) }
                    }

                    override fun onDone(utteranceId: String?) {
                        mainHandler.post {
                            onTtsSpeakingChanged(false)
                            // After the letter finishes talking, automatically listen to the child!
                            startListening()
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        mainHandler.post {
                            onTtsSpeakingChanged(false)
                            startListening()
                        }
                    }
                })
                isTtsReady = true
            }
        }
    }

    fun playRingingTone(onFinished: () -> Unit) {
        Thread {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 80)
                // Ring 1
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 1200)
                Thread.sleep(1800)
                // Ring 2
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 1200)
                Thread.sleep(1400)
            } catch (e: Exception) {
                Log.e("SpeechHelper", "Tone error", e)
            } finally {
                toneGenerator?.release()
                toneGenerator = null
                mainHandler.post { onFinished() }
            }
        }.start()
    }

    fun speak(text: String) {
        stopListening()
        mainHandler.post {
            if (isTtsReady && tts != null) {
                val params = Bundle()
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "letter_speech_${System.currentTimeMillis()}")
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "letter_speech")
            } else {
                // If TTS isn't available, simulate brief delay then listen
                mainHandler.postDelayed({ startListening() }, 2000)
            }
        }
    }

    fun startListening() {
        mainHandler.post {
            if (isListening) return@post
            try {
                if (speechRecognizer == null) {
                    initSpeechRecognizer()
                }
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar")
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                isListening = true
                onListeningStateChanged(true)
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                isListening = false
                onListeningStateChanged(false)
                Log.e("SpeechHelper", "Error starting listening", e)
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                isListening = false
                onListeningStateChanged(false)
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e("SpeechHelper", "Error stopping listening", e)
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                onListeningStateChanged(true)
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
                onListeningStateChanged(false)
            }

            override fun onError(error: Int) {
                isListening = false
                onListeningStateChanged(false)
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "لم أسمعك جيداً يا بطل، حاول مرة أخرى!"
                    SpeechRecognizer.ERROR_AUDIO -> "خطأ في الميكروفون"
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "تأكد من الاتصال بالإنترنت"
                    else -> "أنا بانتظارك لتتحدث يا بطل!"
                }
                onErrorOccurred(errorMessage)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                onListeningStateChanged(false)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull()?.trim().orEmpty()
                if (spokenText.isNotBlank()) {
                    onSpeechRecognized(spokenText)
                } else {
                    onErrorOccurred("لم أسمعك بوضوح، حاول ثانية!")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
            tts?.stop()
            tts?.shutdown()
            tts = null
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
        } catch (e: Exception) {
            Log.e("SpeechHelper", "Error releasing speech helper", e)
        }
    }
}
