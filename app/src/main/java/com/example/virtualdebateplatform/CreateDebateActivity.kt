package com.example.virtualdebateplatform

import com.example.virtualdebateplatform.DebateService
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.virtualdebateplatform.api.ApiService
import com.example.virtualdebateplatform.model.User
import androidx.recyclerview.widget.RecyclerView

class CreateDebateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_create_debate)

        val username = intent.getStringExtra("username")
        val token = intent.getStringExtra("token") ?: ""

        val topicInput = findViewById<EditText>(R.id.et_topic)
        val createButton = findViewById<Button>(R.id.btn_create)
        val timeRadioGroup = findViewById<RadioGroup>(R.id.rg_time_limit)

        val selectedUsernames = mutableListOf<String>()
        val participantsRecyclerView = findViewById<RecyclerView>(R.id.rv_participants)
        participantsRecyclerView.layoutManager = LinearLayoutManager(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5001/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val userService = retrofit.create(ApiService::class.java)

        userService.getUsers("Bearer $token").enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val currentUsername = username ?: ""
                    val users = response.body()?.filter { it.username != currentUsername } ?: return
                    val adapter = UserAdapter(users, selectedUsernames)
                    participantsRecyclerView.adapter = adapter
                }
            }
            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@CreateDebateActivity, "Kullanıcılar alınamadı", Toast.LENGTH_SHORT).show()
            }
        })

        createButton.setOnClickListener {
            val selectedTimeLimit = when (timeRadioGroup.checkedRadioButtonId) {
                R.id.rb_1min -> "1"
                R.id.rb_2min -> "2"
                R.id.rb_3min -> "3"
                else -> null
            }

            if (selectedTimeLimit == null || selectedUsernames.isEmpty()) {
                Toast.makeText(this, "Tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val topic = topicInput.text.toString()
            if (topic.isNotEmpty()) {
                val service = retrofit.create(DebateService::class.java)
                val request = CreateDebateRequest(topic, selectedTimeLimit, selectedUsernames)

                service.createDebate(request).enqueue(object : Callback<CreateDebateResponse> {
                    override fun onResponse(call: Call<CreateDebateResponse>, response: Response<CreateDebateResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@CreateDebateActivity, "Münazara oluşturuldu", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@CreateDebateActivity, DebateRoomActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@CreateDebateActivity, "Oluşturulamadı", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CreateDebateResponse>, t: Throwable) {
                        Toast.makeText(this@CreateDebateActivity, "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}