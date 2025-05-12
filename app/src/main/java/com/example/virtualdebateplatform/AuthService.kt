package com.example.virtualdebateplatform

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// AuthService interface'ini oluşturuyoruz
interface AuthService {

    @POST("login")  // Login için API'ye POST isteği gönderiyoruz
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("register")  // Register için API'ye POST isteği gönderiyoruz
    fun register(@Body registerRequest: RegisterRequest): Call<RegisterResponse>
}