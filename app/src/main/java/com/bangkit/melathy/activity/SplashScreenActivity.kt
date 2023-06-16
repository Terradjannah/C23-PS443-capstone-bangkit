package com.bangkit.melathy.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.bangkit.melathy.R

class SplashScreenActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT:Long = 1500
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        },SPLASH_TIME_OUT)
    }
}