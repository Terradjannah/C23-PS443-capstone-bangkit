package com.bangkit.melathy.activity

import com.google.gson.annotations.SerializedName

data class ProfilePhotoResponse(
    @SerializedName("imageUrl")
    val imageUrl: String?
)