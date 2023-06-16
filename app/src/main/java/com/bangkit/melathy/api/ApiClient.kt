package com.bangkit.melathy.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://api-melathy-umyc4436xa-et.a.run.app/" // Ganti dengan URL base API Anda

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getApiService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}



