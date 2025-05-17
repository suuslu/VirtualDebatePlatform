package com.example.virtualdebateplatform

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class SpeechTimerActivity : AppCompatActivity() {

    private var isMicrophoneOn = true
    private lateinit var microphoneStatusText: TextView
    private lateinit var timerCircle: View

    private var recorder: MediaRecorder? = null
    private var recordedFilePath: String? = null
    private var startTime = 0L
    private var timeLimit = 60000L
    private var isRecordingStopped = false
    private var recordingStartedAt: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_speech_timer)

        microphoneStatusText = findViewById(R.id.btn_microphone)
        timerCircle = findViewById(R.id.tv_timer)

        val isActuallySpeaker = intent.getBooleanExtra("isSpeaker", false)
        isMicrophoneOn = isActuallySpeaker
        updateMicrophoneStatus()

        val speakerName = intent.getStringExtra("currentSpeaker") ?: "Unknown"
        findViewById<TextView>(R.id.tv_speaker).text = "🎤 Speaker: @$speakerName"

        val millis = getSharedPreferences("debate_prefs", MODE_PRIVATE).getLong("time_remaining", 60000L)
        timeLimit = millis
        startTime = System.currentTimeMillis()
        startCountdown(millis)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1001)
        } else {
            if (isMicrophoneOn) startRecording()
        }

        microphoneStatusText.setOnClickListener {
            onMicrophoneToggle()
        }

        timerCircle.setOnClickListener {
            onReturnToDebateRoom()
        }
    }

    private fun updateMicrophoneStatus() {
        val status = if (isMicrophoneOn) "ON" else "OFF"
        microphoneStatusText.text = "🎙️ Microphone: $status"
        Log.d("MIC_STATUS", "Microphone status updated: $status")
    }

    private fun onMicrophoneToggle() {
        val currentUsername = intent.getStringExtra("username") ?: ""
        val currentSpeaker = intent.getStringExtra("currentSpeaker") ?: ""

        if (currentUsername != currentSpeaker) {
            Toast.makeText(this, "You are not the current speaker", Toast.LENGTH_SHORT).show()
            Log.w("MIC_BLOCKED", "Current user $currentUsername is not the speaker $currentSpeaker")
            return
        }

        isMicrophoneOn = !isMicrophoneOn
        updateMicrophoneStatus()

        if (isMicrophoneOn) {
            startRecording()
        } else {
            stopRecording()
        }

        Toast.makeText(this, "Microphone turned ${if (isMicrophoneOn) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
    }

    private fun startRecording() {
        val username = intent.getStringExtra("username") ?: "anon"
        val fileName = "${username}_${System.currentTimeMillis()}.m4a"

        val dir = File(getExternalFilesDir(null), "recordings")
        if (!dir.exists()) {
            val created = dir.mkdirs()
            Log.d("RECORD_DIR", "recordings klasörü oluşturuldu mu? $created")
        }

        val file = File(dir, fileName)
        recordedFilePath = file.absolutePath
        recordingStartedAt = System.currentTimeMillis()

        Log.d("AUDIO_PATH", "Recording to: $recordedFilePath")

        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(recordedFilePath)

                setOnErrorListener { _, what, extra ->
                    Log.e("RECORDER", "Unhandled MediaRecorder error — what: $what, extra: $extra")
                }

                prepare()
                start()
            }

            isRecordingStopped = false
            Log.d("RECORD_OK", "Recording started successfully.")
        } catch (e: Exception) {
            Log.e("RECORD_ERROR", "Recording failed: ${e.message}", e)
        }
    }

    private fun stopRecording() {
        if (isRecordingStopped) return
        isRecordingStopped = true

        try {
            val now = System.currentTimeMillis()
            val elapsed = now - recordingStartedAt
            if (elapsed < 1500) {
                Thread.sleep(1500 - elapsed)
            }

            recorder?.stop()
            Log.d("AUDIO_RECORDING", "Stopped. File saved to: $recordedFilePath")
            Log.d("AUDIO_RECORDING", "File exists: ${File(recordedFilePath ?: "").exists()}")
            Log.d("AUDIO_RECORDING", "File length: ${File(recordedFilePath ?: "").length()} bytes")
        } catch (e: Exception) {
            Log.e("RECORDER", "Stop error: ${e.message}", e)
        } finally {
            try {
                recorder?.release()
            } catch (e: Exception) {
                Log.e("RECORDER", "Release error: ${e.message}", e)
            }
            recorder = null
            getSharedPreferences("debate_prefs", MODE_PRIVATE).edit()
                .putString("last_recorded_file_path", recordedFilePath)
                .apply()
            Log.d("STOP_RECORDING", "Recorded path saved to SharedPreferences: $recordedFilePath")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (isMicrophoneOn) startRecording()
        } else {
            Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show()
            isMicrophoneOn = false
            updateMicrophoneStatus()
        }
    }

    fun setSpeakerStatus(isSpeaker: Boolean) {
        isMicrophoneOn = isSpeaker
        updateMicrophoneStatus()
    }

    private fun onReturnToDebateRoom() {
        if (isMicrophoneOn) stopRecording()

        val elapsed = System.currentTimeMillis() - startTime
        val remainingTime = if (elapsed < timeLimit) timeLimit - elapsed else 0L

        val resultIntent = Intent().apply {
            putExtra("audioFilePath", recordedFilePath)
            putExtra("updatedRemainingTime", remainingTime)
            putExtra("username", intent.getStringExtra("username"))
            putExtra("DEBATE_ID", intent.getIntExtra("DEBATE_ID", -1))
        }
        setResult(RESULT_OK, resultIntent)
        Log.d("RETURN_INTENT", "Returning with audio path: $recordedFilePath and time left: $remainingTime ms")
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    private fun startCountdown(duration: Long) {
        object : android.os.CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                (findViewById<View>(R.id.tv_timer) as? TextView)?.text =
                    String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                if (isMicrophoneOn) stopRecording()
                Toast.makeText(this@SpeechTimerActivity, "Time's up!", Toast.LENGTH_SHORT).show()
                onReturnToDebateRoom()
            }
        }.start()
    }
}