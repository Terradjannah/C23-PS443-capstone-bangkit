package com.bangkit.melathy.activity

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.bangkit.melathy.api.ApiClient
import com.bangkit.melathy.databinding.ActivityProfilBinding
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProfilActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfilBinding
    private val apiService = ApiClient.getApiService()
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengambil token dari SharedPreferences
        val token = getTokenFromPrefs()

        // Melakukan request untuk mendapatkan data pengguna
        getUserData(token)

        // Menambahkan OnClickListener pada tombol edit
        binding.btEdit.setOnClickListener {
            openGallery()
        }
    }

    private fun getTokenFromPrefs(): String {
        val sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        return sharedPreferences.getString(LoginActivity.KEY_TOKEN, "") ?: ""
    }

    private fun getUserData(token: String) {
        val call = apiService.getUserData("jwt_token=$token")
        call.enqueue(object : Callback<UserDataResponse> {
            override fun onResponse(call: Call<UserDataResponse>, response: Response<UserDataResponse>) {
                if (response.isSuccessful) {
                    val userDataResponse = response.body()
                    val userData = userDataResponse?.data

                    if (userData != null) {
                        displayUserData(userData)
                        displayProfilePhoto(userData.profilePhotoUrl)
                    } else {
                        Toast.makeText(this@ProfilActivity, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ProfilActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserDataResponse>, t: Throwable) {
                Toast.makeText(this@ProfilActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun displayUserData(userData: UserData) {
        // Menampilkan data pengguna ke komponen tampilan
        binding.tvName.text = userData.nama
        binding.tvEmail.text = userData.email
        binding.tvAge.text = userData.umur
        binding.tvNoHp.text = userData.no_telp
        binding.tvGender.text = userData.jenis_kelamin
        binding.tvHeight.text = userData.tinggi
        binding.tvWeight.text = userData.berat
    }

    private fun displayProfilePhoto(photoUrl: String) {
        Glide.with(this)
            .load(photoUrl)
            .into(binding.imgUser)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                selectedImageUri = uri
                uploadProfilePhoto()
            }
        }
    }

    private fun uploadProfilePhoto() {
        val token = getTokenFromPrefs()
        val file = File(selectedImageUri?.path)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val photoBody = MultipartBody.Part.createFormData("photo", file.name, requestFile)

        val call = apiService.uploadProfilePhoto("jwt_token=$token", photoBody)
        call.enqueue(object : Callback<UploadPhotoResponse> {
            override fun onResponse(call: Call<UploadPhotoResponse>, response: Response<UploadPhotoResponse>) {
                if (response.isSuccessful) {
                    val uploadPhotoResponse = response.body()
                    val photoUrl = uploadPhotoResponse?.photoUrl

                    if (!photoUrl.isNullOrEmpty()) {
                        // Tampilkan foto baru setelah diunggah
                        displayProfilePhoto(photoUrl)
                        Toast.makeText(this@ProfilActivity, "Profile photo updated", Toast.LENGTH_SHORT).show()

                        // Refresh activity
                        recreate()
                    } else {
                        Toast.makeText(this@ProfilActivity, "Failed to upload profile photo", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ProfilActivity, "${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {
                Toast.makeText(this@ProfilActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
