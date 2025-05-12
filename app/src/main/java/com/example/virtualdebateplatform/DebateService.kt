
package com.example.virtualdebateplatform
import com.example.virtualdebateplatform.CreateDebateResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface DebateService {
    @POST("createDebate")
    fun createDebate(@Body request: CreateDebateRequest): Call<CreateDebateResponse>
}