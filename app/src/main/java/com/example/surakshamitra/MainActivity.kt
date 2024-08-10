package com.example.surakshamitra

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null) {
            supportActionBar?.hide();
        }
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {
            // User is already logged in, redirect to the dashboard
            startDashboardActivity()
            return
        }

        val intent = Intent(this, LoginSignupScreen::class.java)
        startActivity(intent)

    }
    private fun startDashboardActivity() {
        val intent = Intent(applicationContext, DashBoard::class.java)
        startActivity(intent)
        finish() // Finish the current activity to prevent the user from coming back to the login screen
    }
}