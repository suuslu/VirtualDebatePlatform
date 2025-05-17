package com.example.virtualdebateplatform

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.virtualdebateplatform.api.ApiService
import com.example.virtualdebateplatform.model.FeedbackResponse
import com.example.virtualdebateplatform.model.RatingResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class FeedbackFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feedback, container, false)

        val logicView = view.findViewById<TextView>(R.id.tv_logic)
        val clarityView = view.findViewById<TextView>(R.id.tv_clarity)
        val impactView = view.findViewById<TextView>(R.id.tv_impact)
        val scoreView = view.findViewById<TextView>(R.id.tv_score)
        val backButton = view.findViewById<Button>(R.id.btn_back_home)

        val audioFilePath = arguments?.getString("audioFilePath")
        val username = arguments?.getString("username") ?: "unknown_user"
        val debateId = arguments?.getString("debateId") ?: "-1"

        if (audioFilePath.isNullOrBlank()) {
            logicView.text = "Logic: No recording found"
            clarityView.text = "Clarity: No recording found"
            impactView.text = "Impact: No recording found"
            scoreView.text = "Score: --"
            return view
        }

        val file = File(audioFilePath)
        Log.d("UPLOAD_CHECK", "Path: ${file.absolutePath}, exists: ${file.exists()}, size: ${file.length()}")

        val requestFile = file.asRequestBody("audio/mp4".toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("audio", file.name, requestFile)
        val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val debateIdPart = debateId.toRequestBody("text/plain".toMediaTypeOrNull())

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5001/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }).build()
            )
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.analyzeAudio(audioPart, usernamePart, debateIdPart)
            .enqueue(object : Callback<FeedbackResponse> {
                override fun onResponse(call: Call<FeedbackResponse>, response: Response<FeedbackResponse>) {
                    if (response.isSuccessful) {
                        val fb = response.body()?.feedback
                        if (fb != null) {
                            logicView.text = "\uD83E\uDDE0 Logic: ${fb.logic}"
                            clarityView.text = "\uD83D\uDD0D Clarity: ${fb.clarity}"
                            impactView.text = "\uD83D\uDCAC Impact: ${fb.impact}"
                            scoreView.text = "\uD83C\uDFC5 Score: ${fb.score}/10"

                            try {
                                val jsonBody = JSONObject().apply {
                                    put("debateId", debateId.toInt())
                                    put("speaker", username)
                                    put("score", fb.score)
                                }
                                val jsonRequest = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

                                service.submitRatingJson(jsonRequest)
                                    .enqueue(object : Callback<RatingResponse> {
                                        override fun onResponse(call: Call<RatingResponse>, response: Response<RatingResponse>) {
                                            Log.d("AI_JSON", "AI score submitted")
                                        }
                                        override fun onFailure(call: Call<RatingResponse>, t: Throwable) {
                                            Log.e("AI_JSON", "Failed to submit AI score", t)
                                        }
                                    })
                            } catch (e: Exception) {
                                Log.e("AI_JSON", "JSON error: ${e.message}")
                            }

                            backButton.text = "Return to Home"
                            backButton.setOnClickListener {
                                val intent = Intent(requireContext(), MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        } else {
                            logicView.text = "Logic: Empty feedback"
                            clarityView.text = "Clarity: Empty feedback"
                            impactView.text = "Impact: Empty feedback"
                            scoreView.text = "Score: --"
                        }
                    } else {
                        logicView.text = "Logic: Server error"
                        clarityView.text = "Clarity: Server error"
                        impactView.text = "Impact: Server error"
                        scoreView.text = "Score: --"
                    }
                }

                override fun onFailure(call: Call<FeedbackResponse>, t: Throwable) {
                    logicView.text = "Logic: Failed to load"
                    clarityView.text = "Clarity: Failed to load"
                    impactView.text = "Impact: Failed to load"
                    scoreView.text = "Score: --"
                    Log.e("FEEDBACK_FAIL", "Error: ${t.message}", t)
                }
            })

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(username: String, path: String, debateId: String): FeedbackFragment {
            val fragment = FeedbackFragment()
            val args = Bundle()
            args.putString("username", username)
            args.putString("audioFilePath", path)
            args.putString("debateId", debateId)
            fragment.arguments = args
            return fragment
        }
    }
}