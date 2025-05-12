package com.example.virtualdebateplatform

import com.example.virtualdebateplatform.model.User
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.virtualdebateplatform.api.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CreateFragment : Fragment() {

    companion object {
        private const val ARG_USERNAME = "username"

        fun newInstance(username: String): CreateFragment {
            val fragment = CreateFragment()
            val args = Bundle()
            args.putString(ARG_USERNAME, username)
            fragment.arguments = args
            return fragment
        }
    }

    private var currentUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentUsername = arguments?.getString(ARG_USERNAME)
    }

    private lateinit var api: ApiService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_debate, container, false)

        api = Retrofit.Builder()
            .baseUrl("http://192.168.1.13:5001/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_participants)
        val selectedUsernames = mutableListOf<String>()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val usernameToFilter = currentUsername ?: ""
        Log.d("CREATE_FRAGMENT", "Current logged-in username: $usernameToFilter")

        val token = "Bearer " + (activity?.intent?.getStringExtra("token") ?: "")
        api.getUsers(token).enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val users = response.body() ?: return
                    val filteredUsers = users.filter { user ->
                        user.username.trim().isNotEmpty() && user.username.trim() != usernameToFilter?.trim()
                    }
                    Log.d("CREATE_FRAGMENT", "Filtered user list: ${filteredUsers.map { it.username }}")
                    val adapter = UserAdapter(filteredUsers, selectedUsernames)
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(requireContext(), "Kullanıcılar alınamadı", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(requireContext(), "Bağlantı hatası", Toast.LENGTH_SHORT).show()
                Log.e("API_DEBUG", "HATA: ${t.message}")
            }
        })

        return view
    }
}