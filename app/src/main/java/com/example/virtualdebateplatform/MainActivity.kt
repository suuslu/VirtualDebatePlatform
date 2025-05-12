package com.example.virtualdebateplatform

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.example.virtualdebateplatform.HomeFragment
import com.example.virtualdebateplatform.DebateFragment
import com.example.virtualdebateplatform.CreateFragment
import com.example.virtualdebateplatform.RankingsFragment
import com.example.virtualdebateplatform.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var token: String? = null
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        token = intent.getStringExtra("token")
        username = intent.getStringExtra("username")

        bottomNav = findViewById(R.id.bottom_nav)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment.newInstance(username ?: "", token ?: ""))
            .commit()

        bottomNav.setOnItemSelectedListener { item ->
            val selectedFragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment.newInstance(username ?: "", token ?: "")
                R.id.nav_debates -> DebateFragment()
                R.id.nav_create -> CreateFragment.newInstance(username ?: "")
                R.id.nav_rankings -> RankingsFragment()
                R.id.nav_profile -> ProfileFragment.newInstance(token ?: "")
                else -> null
            }

            selectedFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it)
                    .commit()
            }
            true
        }
    }
}