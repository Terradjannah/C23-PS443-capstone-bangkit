package com.bangkit.melathy.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bangkit.melathy.api.ApiService
import com.bangkit.melathy.databinding.ActivityMulaiDiagnosaBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MulaiDiagnosaActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMulaiDiagnosaBinding.inflate(layoutInflater) }
    private lateinit var currentPhotoPath: String
    private lateinit var imageCapture: ImageCapture
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))

        binding.captureButton.setOnClickListener {
            capturePhoto()
        }

        binding.switchCameraButton.setOnClickListener {
            switchCamera()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun capturePhoto() {
        val photoFile = createImageFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    binding.loadingProgressBar.visibility = View.VISIBLE

                    // Foto telah disimpan, lanjutkan dengan pengunggahan
                    uploadPhoto(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    // Handle error
                }
            })
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                // Restart activity to start camera
                recreate()
            } else {
                // Handle permission denied
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun uploadPhoto(photoFile: File) {
        val jwtToken = getTokenFromPrefs() // Ambil token dari shared preference

        // Create the request body for the image
        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), photoFile)

        // Create the MultipartBody.Part
        val imagePart = MultipartBody.Part.createFormData("image", photoFile.name, requestBody)

        val apiService = ApiService.create()
        val call = apiService.uploadImage("jwt_token=$jwtToken", imagePart)

        call.enqueue(object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {

                binding.loadingProgressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val uploadResponse = response.body()
                    if (uploadResponse != null) {
                        val imageUrl = uploadResponse.imageUrl
                        val prediction = uploadResponse.prediction

                        val intent = Intent(this@MulaiDiagnosaActivity, HasilDiagnosaActivity::class.java)
                        intent.putExtra("photoPath", currentPhotoPath)
                        intent.putExtra("imageUrl", imageUrl)
                        intent.putExtra("prediction", prediction)
                        startActivity(intent)
                    } else {
                        // Handle null response body
                    }
                } else {
                    // Handle unsuccessful response
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                binding.loadingProgressBar.visibility = View.GONE

                // Handle failure
            }
        })
    }

    private fun getTokenFromPrefs(): String {
        val sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        return sharedPreferences.getString(LoginActivity.KEY_TOKEN, "") ?: ""
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}

