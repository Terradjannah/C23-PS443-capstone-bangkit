package com.bangkit.melathy.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.melathy.R
import com.bangkit.melathy.databinding.ActivityHasilDiagnosaBinding
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso

class HasilDiagnosaActivity : AppCompatActivity() {
    private val binding by lazy { ActivityHasilDiagnosaBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val photoPath = intent.getStringExtra("photoPath")
        val imageUrl = intent.getStringExtra("imageUrl")
        val prediction = intent.getStringExtra("prediction")

        // Tampilkan foto yang diambil
        Glide.with(this)
            .load(photoPath)
            .into(binding.ivPhoto)

        // Tampilkan informasi respons API
//        binding.imageUrlTextView.text = imageUrl
        binding.tvPrediction.text = prediction
    }
}

