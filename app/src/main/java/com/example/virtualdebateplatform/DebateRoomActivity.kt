package com.example.virtualdebateplatform

import android.util.Log

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat

class DebateRoomActivity : AppCompatActivity() {

    private var timer: CountDownTimer? = null
    private var timeRemaining: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debate_room)

        // Get the debate name, participants, and time from the Intent
        val debateName = intent.getStringExtra("DEBATE_NAME") ?: "Default Debate"
        Log.d("DEBATE_ROOM", "debateName = $debateName")
        val participantsString = intent.getStringExtra("participants") ?: "Participant 1,Participant 2"
        val participants = participantsString.split(",")
        val timeLimit = intent.getIntExtra("TIME_LIMIT", 1)  // Default time limit to 1 minute

        val debateNameText = findViewById<TextView>(R.id.debate_name_text)
        val participant1Text = findViewById<TextView>(R.id.participant_1_text)
        val participant2Text = findViewById<TextView>(R.id.participant_2_text)
        val timerText = findViewById<TextView>(R.id.timer_text)

        // Set the debate name and participants dynamically
        debateNameText.text = HtmlCompat.fromHtml(
            "🎤 Debate Name: '${debateName.replace("<", "&lt;").replace(">", "&gt;")}'",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        participant1Text.text = "👤 Participant 1: ${participants.getOrNull(0) ?: "N/A"}"
        participant2Text.text = "👤 Participant 2: ${participants.getOrNull(1) ?: "N/A"}"

        // Initialize the countdown timer
        timeRemaining = (timeLimit * 60 * 1000).toLong()  // Convert minutes to milliseconds

        // Start the countdown timer
        startTimer(timeRemaining)

        // End debate button click
        val endDebateButton = findViewById<Button>(R.id.btn_start_speech)
        endDebateButton.setOnClickListener {
            stopTimer()
            Toast.makeText(this, "Debate Ended!", Toast.LENGTH_SHORT).show()
            finish()  // End debate activity and return to the previous screen
        }
    }

    private fun startTimer(timeInMillis: Long) {
        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                val timeLeft = String.format("%02d:%02d", minutes, seconds)
                findViewById<TextView>(R.id.timer_text).text = "⏳ $timeLeft remaining"
            }

            override fun onFinish() {
                findViewById<TextView>(R.id.timer_text).text = "⏳ Time's up!"
                Toast.makeText(this@DebateRoomActivity, "Time is up!", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
    }
}