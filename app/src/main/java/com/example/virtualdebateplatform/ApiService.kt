package com.example.virtualdebateplatform.api

import com.example.virtualdebateplatform.CreateDebateRequest
import com.example.virtualdebateplatform.CreateDebateResponse
<<<<<<< HEAD
=======
import com.example.virtualdebateplatform.model.FeedbackResponse
>>>>>>> cb88bd6 (Pre-restart backup)
import com.example.virtualdebateplatform.model.User
import retrofit2.Call
import retrofit2.http.*

<<<<<<< HEAD
=======
data class PastDebatesResponse(
    val past_debates: List<String>
)

>>>>>>> cb88bd6 (Pre-restart backup)
interface ApiService {

    @GET("users")
    fun getUsers(@Header("Authorization") authHeader: String): Call<List<User>>

    @POST("create_debate")
    fun createDebate(
        @Body request: CreateDebateRequest,
        @Header("Authorization") authHeader: String
    ): Call<CreateDebateResponse>
<<<<<<< HEAD
=======

    @POST("login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @Multipart
    @POST("analyze_audio")
    fun analyzeAudio(
        @Part audio: MultipartBody.Part,
        @Part("username") username: RequestBody,
        @Part("debateId") debateId: RequestBody
    ): Call<FeedbackResponse>

    @FormUrlEncoded
    @POST("submit_rating")
    fun submitRating(
        @Field("debateId") debateId: Int,
        @Field("speaker") speaker: String,
        @Field("score") score: Float
    ): Call<RatingResponse>

    @POST("get_speakers")
    fun getSpeakers(@Body body: Map<String, Int>): Call<SpeakerListResponse>

    // ✅ AI puanını JSON olarak göndermek için
    @POST("submit_rating_json")
    fun submitRatingJson(@Body body: RequestBody): Call<RatingResponse>

    @GET("rankings")
    fun getRankings(): Call<List<Ranking>>

    @POST("user_past_debates")
    fun getUserPastDebates(@Body request: Map<String, String>): Call<PastDebatesResponse>
>>>>>>> cb88bd6 (Pre-restart backup)
}