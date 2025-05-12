package com.example.virtualdebateplatform

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.util.Log
import android.view.View
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)  // activity_register.xml layout'ını kullanıyoruz

        val emailInput = findViewById<EditText>(R.id.et_email)
        val usernameInput = findViewById<EditText>(R.id.et_username)
        val passwordInput = findViewById<EditText>(R.id.et_password)
        val validationText = findViewById<TextView>(R.id.tv_validation)
        val registerButton = findViewById<Button>(R.id.btn_register)

        validationText.visibility = View.GONE

        // "Register" butonuna tıklama olayını ekliyoruz
        registerButton.setOnClickListener {
            Log.d("REGISTER_DEBUG", "Register butonuna basıldı")
            val email = emailInput.text.toString()
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            // Eğer email, kullanıcı adı veya şifre boşsa hata mesajı gösteriyoruz
            if (email.isBlank() || username.isBlank() || password.isBlank()) {
                validationText.text = "Lütfen tüm alanları doldurun"
                validationText.visibility = View.VISIBLE
            } else {
                // Retrofit ile backend'e istek gönderiyoruz
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:5001/")  // Flask API URL'yi burada belirtin
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(AuthService::class.java)  // AuthService ile backend'e bağlanıyoruz
                val request = RegisterRequest(email, username, password)  // Kayıt için request

                // API'ye istek gönderiyoruz
                service.register(request).enqueue(object : Callback<RegisterResponse> {
                    override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                        if (response.isSuccessful) {
                            val message = response.body()?.message ?: "Kayıt başarılı!"

                            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                            prefs.edit().putString("username", username).apply()

                            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMessage = response.errorBody()?.string()
                            Toast.makeText(this@RegisterActivity, "Hata: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                        Toast.makeText(this@RegisterActivity, "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}