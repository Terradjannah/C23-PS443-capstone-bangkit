package com.bangkit.melathy.activity

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    val imageUrl: String,
    val prediction: String
)
