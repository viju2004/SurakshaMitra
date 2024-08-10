package com.example.surakshamitra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginSignupScreen : AppCompatActivity() {
    private lateinit var createAccount: Button
    private lateinit var loginUser: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup_screen)
        supportActionBar?.hide()

//        val shakeServiceIntent = Intent(this, ShakeService::class.java)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(shakeServiceIntent)
//        } else {
//            startService(shakeServiceIntent)
//        }

        createAccount = findViewById(R.id.createloginBtn)
        loginUser = findViewById(R.id.loginBtn)

        createAccount.setOnClickListener {
            intent = Intent(this, RegisterUsers::class.java)
            startActivity(intent)
        }

        loginUser.setOnClickListener {

            intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
//            intent = Intent(this, AlertSent::class.java)
//            startActivity(intent)
//            intent = Intent(this, NearbyAgencies::class.java)
//            startActivity(intent)
        }




    }
}