package com.bangkit.melathy.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.melathy.R
import com.bangkit.melathy.api.ApiClient
import com.bangkit.melathy.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val apiService = ApiClient.getApiService()
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = getTokenFromPrefs()
        getUserData(token)

        sharedPreferences = getSharedPreferences("YOUR_SHARED_PREFS_NAME", Context.MODE_PRIVATE)
        userId = getUserIdFromPrefs()

        binding.diagnoseLayout.setOnClickListener {
            startActivity(Intent(this, MulaiDiagnosaActivity::class.java))
        }


        binding.menuButton.setOnClickListener { view ->
            showPopupMenu(view)
        }

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
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserDataResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getTokenFromPrefs(): String {
        val sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        return sharedPreferences.getString(LoginActivity.KEY_TOKEN, "") ?: ""
    }

    private fun getUserIdFromPrefs(): String? {
        return sharedPreferences.getString("USER_ID_KEY", null)
    }

    private fun logout() {
        // Remove the stored user ID from SharedPreferences
        val editor = sharedPreferences.edit()
        editor.remove("USER_ID_KEY")
        editor.apply()

        // Start LoginActivity and clear all previous activities
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.bottom_nav_menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_profile -> {
                    // Handle menu item 3 click (intent to ProfilActivity)
                    startActivity(
                        Intent(this, ProfilActivity::class.java)
                            .putExtra("userId", userId)
                    )
                    true
                }
                R.id.about_us -> {
                    // Handle menu item 3 click (intent to ProfilActivity)
                    startActivity(
                        Intent(this, TeamActivity::class.java)
                    )
                    true
                }
                R.id.logout -> {
                    // Handle menu item 4 click (logout)
                    logout()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun displayUserData(userData: UserData) {
        // Menampilkan data pengguna ke komponen tampilan
        binding.profileName.text = userData.nama
    }


}


