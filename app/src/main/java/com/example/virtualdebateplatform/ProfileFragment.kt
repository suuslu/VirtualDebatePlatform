package com.example.virtualdebateplatform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    private var token: String? = null  // Token'ı burada alacağız

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            token = it.getString("token")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Burada token ile ilgili işlemler yapılabilir
        if (token != null) {
            // Token'ı kullanarak profil verilerini alabiliriz
            println("Token: $token")
        }

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(token: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("token", token)
                }
            }
    }
}