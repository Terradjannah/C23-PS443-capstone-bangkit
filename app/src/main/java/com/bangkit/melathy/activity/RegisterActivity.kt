package com.bangkit.melathy.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bangkit.melathy.api.ApiClient
import com.bangkit.melathy.api.ApiService
import com.bangkit.melathy.databinding.ActivityRegisterBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiClient.getApiService()

        binding.btRegister.setOnClickListener {
            val nama = binding.edRegisterName.text.toString()
            val password = binding.edRegisterPassword.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val telepon = binding.edTelepon.text.toString()
            val jenis_kelamin = binding.edKelamin.text.toString()
            val berat = binding.edBerat.text.toString()
            val tinggi = binding.edTinggi.text.toString()
            val umur = binding.edUmur.text.toString()

            registerUser(nama, password, email, telepon, jenis_kelamin, berat, tinggi, umur)
        }

        binding.tvAlreadyHaveAnAccount.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }
        playPropertyAnimation()
    }

    private fun playPropertyAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.tvBuatAkun, View.ALPHA, 1f).setDuration(300)
        val tv1 = ObjectAnimator.ofFloat(binding.tv1, View.ALPHA, 1f).setDuration(300)
        val edNama = ObjectAnimator.ofFloat(binding.edRegisterName, View.ALPHA, 1f).setDuration(300)
        val tv2 = ObjectAnimator.ofFloat(binding.tv2, View.ALPHA, 1f).setDuration(300)
        val edtEmail = ObjectAnimator.ofFloat(binding.edRegisterEmail, View.ALPHA, 1f).setDuration(300)
        val tv3 = ObjectAnimator.ofFloat(binding.tv3, View.ALPHA, 1f).setDuration(300)
        val edtPass = ObjectAnimator.ofFloat(binding.edRegisterPassword, View.ALPHA, 1f).setDuration(300)
        val btnSignIn = ObjectAnimator.ofFloat(binding.btRegister, View.ALPHA, 1f).setDuration(300)
        val register = ObjectAnimator.ofFloat(binding.tvAlreadyHaveAnAccount, View.ALPHA, 1f).setDuration(300)

        AnimatorSet().apply {
            playSequentially(title, tv1, edNama, tv2, edtEmail, tv3, edtPass, btnSignIn, register)
            start()
        }
    }

    private fun registerUser(nama: String, password: String, email: String, telepon: String, jenis_kelamin: String, berat: String, tinggi: String, umur: String) {
        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("password", password)
        jsonObject.put("no_telp", telepon)
        jsonObject.put("jenis_kelamin", jenis_kelamin)
        jsonObject.put("berat", berat)
        jsonObject.put("tinggi", tinggi)
        jsonObject.put("umur", umur)
        jsonObject.put("nama", nama)

        val requestBody = RequestBody.create("application/json".toMediaType(), jsonObject.toString())
        val call = apiService.register(requestBody)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val message = "Registrasi berhasil"
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                    Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()

                    // Lakukan sesuatu setelah registrasi berhasil
                } else {
                    Toast.makeText(this@RegisterActivity, "Registrasi gagal", Toast.LENGTH_SHORT).show()
                    // Lakukan sesuatu jika registrasi gagal
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }
}
