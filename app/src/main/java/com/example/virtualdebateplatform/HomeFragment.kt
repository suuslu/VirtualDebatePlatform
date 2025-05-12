package com.example.virtualdebateplatform

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    companion object {
        fun newInstance(username: String, token: String): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString("username", username)
            args.putString("token", token)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bu fragment'ın layout dosyasını inflate ediyoruz
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        val createButton = rootView.findViewById<Button>(R.id.btn_create)

        // Create Debate Button Clicked
        createButton.setOnClickListener {
            val intent = Intent(activity, CreateDebateActivity::class.java)
            val username = arguments?.getString("username") ?: ""
            val token = arguments?.getString("token") ?: ""
            intent.putExtra("username", username)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        // Fragment'ın view'ını döndürüyoruz
        return rootView
    }
}