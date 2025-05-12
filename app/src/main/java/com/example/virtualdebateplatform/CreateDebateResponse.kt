package com.example.virtualdebateplatform

import com.google.gson.annotations.SerializedName

data class CreateDebateResponse(
    @SerializedName("debate_id")
    val debateId: Int
)