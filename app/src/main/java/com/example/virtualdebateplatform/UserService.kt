package com.example.virtualdebateplatform.api

import com.example.virtualdebateplatform.model.User
import retrofit2.Call
import retrofit2.http.GET

interface UserService {
    @GET("/users")
    fun getUsers(): Call<List<User>>
}