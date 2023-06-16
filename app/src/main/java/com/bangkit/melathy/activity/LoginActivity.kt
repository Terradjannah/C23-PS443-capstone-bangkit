package com.bangkit.melathy.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bangkit.melathy.api.ApiClient
import com.bangkit.melathy.databinding.ActivityLoginBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val apiService = ApiClient.getApiService()

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            login(email, password)
        }

        playPropertyAnimation()
    }

    private fun playPropertyAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.tvJudul, View.ALPHA, 1f).setDuration(300)
        val tv1 = ObjectAnimator.ofFloat(binding.tv1, View.ALPHA, 1f).setDuration(300)
        val edtEmail = ObjectAnimator.ofFloat(binding.edLoginEmail, View.ALPHA, 1f).setDuration(300)
        val tv2 = ObjectAnimator.ofFloat(binding.tv2, View.ALPHA, 1f).setDuration(300)
        val edtPass = ObjectAnimator.ofFloat(binding.edLoginPassword, View.ALPHA, 1f).setDuration(300)
        val btnSignIn = ObjectAnimator.ofFloat(binding.btLogin, View.ALPHA, 1f).setDuration(300)
        val register = ObjectAnimator.ofFloat(binding.tvRegister, View.ALPHA, 1f).setDuration(300)

        AnimatorSet().apply {
            playSequentially(title, tv1, edtEmail, tv2, edtPass, btnSignIn, register)
            start()
        }
    }

    private fun login(email: String, password: String) {
        val requestBody = RequestBody.create(
            "application/json".toMediaType(),
            "{\"email\":\"$email\",\"password\":\"$password\"}"
        )

        val call = apiService.login(requestBody)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val token = loginResponse?.token
                    val userId = loginResponse?.id

                    if (token != null && userId != null) {
                        saveTokenToPrefs(token)
                        saveUserIdToPrefs(userId)
                        openMainActivity()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveTokenToPrefs(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_TOKEN, token)
        editor.apply()
    }

    private fun saveUserIdToPrefs(userId: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_ID, userId)
        editor.apply()
    }

    private fun openMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        const val PREFS_NAME = "MyPrefs"
        private const val KEY_USER_ID = "user_id"
        const val KEY_TOKEN = "token"
    }
}
