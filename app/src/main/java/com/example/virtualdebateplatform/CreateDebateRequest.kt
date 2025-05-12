package com.example.virtualdebateplatform

data class CreateDebateRequest(
    val topic: String,
    val timeLimit: String,
    val participants: List<String>
)