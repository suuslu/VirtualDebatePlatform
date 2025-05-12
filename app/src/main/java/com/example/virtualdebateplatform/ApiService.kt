package com.example.virtualdebateplatform.api

import com.example.virtualdebateplatform.CreateDebateRequest
import com.example.virtualdebateplatform.CreateDebateResponse
import com.example.virtualdebateplatform.model.User
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("users")
    fun getUsers(@Header("Authorization") authHeader: String): Call<List<User>>

    @POST("create_debate")
    fun createDebate(
        @Body request: CreateDebateRequest,
        @Header("Authorization") authHeader: String
    ): Call<CreateDebateResponse>
}