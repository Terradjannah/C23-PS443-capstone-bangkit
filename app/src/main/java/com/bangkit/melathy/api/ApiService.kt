package com.bangkit.melathy.api

import com.bangkit.melathy.activity.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    @POST("registrasi")
    fun register(@Body requestBody: RequestBody): Call<ResponseBody>

    // Tambahkan endpoint-endpoint lain yang Anda butuhkan
    // ...

    // Contoh endpoint lain:

    @POST("login") // Ganti dengan endpoint login yang sesuai
    fun login(
        @Body requestBody: RequestBody
    ): Call<LoginResponse>

    @GET("user")
    fun getUserData(@Header("Cookie") token: String): Call<UserDataResponse>

    @Multipart
    @POST("uploadImage")
    fun uploadImage(
        @Header("Cookie") token: String,
        @Part image: MultipartBody.Part
    ): Call<UploadResponse>

    @Multipart
    @POST("uploadProfilePhoto")
    fun uploadProfilePhoto(
        @Header("Cookie") token: String,
        @Part photo: MultipartBody.Part
    ): Call<UploadPhotoResponse>

    @Headers("Content-Type: application/json")
    @GET("getProfilePhoto")
    fun getProfilePhoto(@Header("Cookie") token: String): Call<ProfilePhotoResponse>

    companion object {
        private const val BASE_URL = "https://api-melathy-umyc4436xa-et.a.run.app/"

        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }


    /*
    @Multipart
    @POST("upload") // Ganti dengan endpoint upload foto yang sesuai
    fun uploadPhoto(
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>
    */

    /*
    @POST("logout") // Ganti dengan endpoint logout yang sesuai
    fun logout(): Call<ResponseBody>
    */
}

