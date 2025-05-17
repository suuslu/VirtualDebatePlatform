package com.example.virtualdebateplatform.model

data class FeedbackResponse(
    val feedback: FeedbackDetail
)

data class FeedbackDetail(
    val logic: String?,
    val clarity: String?,
    val impact: String?,
    val score: Float?
)