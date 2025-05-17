package com.example.virtualdebateplatform

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.virtualdebateplatform.api.ApiService
import com.example.virtualdebateplatform.api.PastDebatesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var pastDebatesTextView: TextView
    private lateinit var editProfileButton: Button

    private val PICK_IMAGE_REQUEST = 1

    private lateinit var usernameArg: String
    private lateinit var emailArg: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = arguments?.getString("username") ?: ""
        val email = arguments?.getString("email") ?: ""

        this.usernameArg = username
        this.emailArg = email
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileImageView = view.findViewById(R.id.profile_image)
        usernameTextView = view.findViewById(R.id.profile_username)
        editProfileButton = view.findViewById(R.id.edit_profile_button)
        val themeButton = view.findViewById<Button>(R.id.theme_button)
        pastDebatesTextView = view.findViewById(R.id.past_debates)

        usernameTextView.text = "Username: @$usernameArg"

        themeButton.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Dark mode isn't available yet :)", android.widget.Toast.LENGTH_SHORT).show()
        }

        editProfileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5001/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)
        val request = mapOf("username" to usernameArg)

        api.getUserPastDebates(request).enqueue(object : Callback<PastDebatesResponse> {
            override fun onResponse(
                call: Call<PastDebatesResponse>,
                response: Response<PastDebatesResponse>
            ) {
                if (response.isSuccessful) {
                    val debates = response.body()?.past_debates ?: emptyList()
                    val formatted = debates.mapIndexed { i, t -> "• Debate ${i + 1}: '$t'" }.joinToString("\n")
                    pastDebatesTextView.text = "📚 Debates You Participated In (Not Created):\n$formatted"
                }
            }

            override fun onFailure(call: Call<PastDebatesResponse>, t: Throwable) {
                pastDebatesTextView.text = "📚 Debates You Participated In (Not Created):\nError loading debates."
            }
        })

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                profileImageView.setImageURI(imageUri)
            }
        }
    }

    companion object {
        fun newInstance(username: String, token: String, email: String): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString("username", username)
            args.putString("token", token)
            args.putString("email", email)
            fragment.arguments = args
            return fragment
        }
    }
}