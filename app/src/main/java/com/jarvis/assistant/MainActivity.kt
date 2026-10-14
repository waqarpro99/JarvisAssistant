package com.jarvis.assistant

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private lateinit var statusView: TextView
    private lateinit var logView: TextView
    private var listening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        initTts()
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            initRecognizer()
        } else {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
            setBackgroundColor(0xFF0D1B2A.toInt())
        }
        val title = TextView(this).apply {
            text = "🎙 JARVIS VOICE ASSISTANT"
            textSize = 22f
            setTextColor(0xFF00E5FF.toInt())
        }
        statusView = TextView(this).apply {
            text = "Microphone ki permission dein"
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
        }
        val btn = Button(this).apply {
            text = "DABAYEIN AUR BOLEN"
            setOnClickListener { startListening() }
        }
        logView = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFFBBDEFB.toInt())
        }
        val scroll = ScrollView(this).apply { addView(logView) }
        root.addView(title)
        root.addView(statusView)
        root.addView(btn)
        root.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(this) { s ->
            if (s == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("en", "IN")
            }
        }
    }

    private fun initRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status("Is phone mein speech recognition nahi hai. Google app install karein.")
            return
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(Listener())
        }
        status("Ready! Button dabayen aur bolen")
        speak("Jarvis ready hai")
    }

    private fun startListening() {
        val rec = speechRecognizer
        if (rec == null) {
            status("Pehle microphone permission dein")
            return
        }
        if (listening) {
            rec.stopListening()
            listening = false
            status("Ruk gaya")
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur-PK")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Bolein...")
        }
        listening = true
        status("Sun raha hoon...")
        try {
            rec.startListening(intent)
        } catch (e: Exception) {
            listening = false
            status("Speech service available nahi: " + e.message)
        }
    }

    private fun speak(text: String) {
        log("Jarvis: $text")
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis")
    }

    private fun log(text: String) {
        logView.append(text + "\n\n")
    }

    private fun status(text: String) {
        statusView.text = text
    }

    private fun handleCommand(cmd: String) {
        when {
            cmd.contains("time") || cmd.contains("waqt") || cmd.contains("samay") -> {
                val t = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                speak("Abhi ka time hai $t")
            }
            cmd.contains("date") || cmd.contains("tarikh") || cmd.contains("tareekh") || cmd.contains("din") -> {
                val d = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())
                speak("Aaj hai $d")
            }
            cmd.contains("whatsapp") && (cmd.contains("kholo") || cmd.contains("open") || cmd.contains("chalao")) ->
                openApp("com.whatsapp", "WhatsApp khol raha hoon")
            cmd.contains("youtube") && (cmd.contains("kholo") || cmd.contains("open") || cmd.contains("chalao")) ->
                openApp("com.google.android.youtube", "YouTube khol raha hoon")
            cmd.contains("camera") && (cmd.contains("kholo") || cmd.contains("open")) -> {
                startActivity(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA))
                speak("Camera khol diya")
            }
            cmd.contains("call") || cmd.contains("dial") || cmd.contains("phone lagao") -> {
                startActivity(Intent(Intent.ACTION_DIAL))
                speak("Dialer khol diya")
            }
            cmd.contains("search") -> {
                val q = cmd.substringAfter("search").trim()
                if (q.isNotEmpty()) {
                    startActivity(Intent(Intent.ACTION_WEB_SEARCH).putExtra(SearchManager.QUERY, q))
                    speak("Search kar raha hoon: $q")
                } else speak("Kya search karoon?")
            }
            cmd.contains("kaun ho") || cmd.contains("your name") || cmd.contains("pehchano") ->
                speak("Main Jarvis hoon, aapka voice assistant")
            cmd.contains("band") || cmd.contains("stop") || cmd.contains("chup") -> {
                textToSpeech?.stop()
                speak("Theek hai")
            }
            else -> speak("Mujhe samajh nahi aaya. Phir se koshish karein.")
        }
    }

    private fun openApp(pkg: String, msg: String) {
        val intent = packageManager.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            startActivity(intent)
            speak(msg)
        } else {
            speak("Yeh app is phone mein installed nahi hai")
        }
    }

    private inner class Listener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() { status("Bolein...") }
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { listening = false }
        override fun onError(error: Int) {
            listening = false
            status("Error code $error. Dobara button dabayen.")
        }
        override fun onResults(results: Bundle?) {
            listening = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull()
            if (text.isNullOrBlank()) {
                status("Kuch sunai nahi diya. Dobara koshish karein.")
                return
            }
            log("Aap: $text")
            handleCommand(text.lowercase(Locale.ROOT))
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            initRecognizer()
        } else {
            status("Permission nahi mili to app kaam nahi karega")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
