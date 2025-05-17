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
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        token = intent.getStringExtra("token")
        username = intent.getStringExtra("username")
        email = intent.getStringExtra("email")

        bottomNav = findViewById(R.id.bottom_nav)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment.newInstance(username ?: "", token ?: ""))
            .commit()

        bottomNav.setOnItemSelectedListener { item ->
<<<<<<< HEAD
            val selectedFragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment.newInstance(username ?: "", token ?: "")
                R.id.nav_debates -> DebateFragment()
                R.id.nav_create -> CreateFragment.newInstance(username ?: "")
                R.id.nav_rankings -> RankingsFragment()
                R.id.nav_profile -> ProfileFragment.newInstance(token ?: "")
                else -> null
=======
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment.newInstance(username ?: "", token ?: ""))
                        .commit()
                    true
                }
                R.id.nav_debates -> {
                    val fragment = DebateFragment()
                    val bundle = Bundle().apply {
                        putString("hint", "To create a debate, please go to the Create tab.")
                    }
                    fragment.arguments = bundle
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                    true
                }
                R.id.nav_create -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CreateFragment.newInstance(username ?: ""))
                        .commit()
                    true
                }
                R.id.nav_rankings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, RankingsFragment())
                        .commit()
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment.newInstance(username ?: "", token ?: "", email ?: ""))
                        .commit()
                    true
                }
                else -> false
>>>>>>> cb88bd6 (Pre-restart backup)
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