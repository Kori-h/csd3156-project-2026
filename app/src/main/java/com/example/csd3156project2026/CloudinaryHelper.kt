package com.example.csd3156project2026

import android.graphics.Bitmap
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

fun uploadToCloudinary(
    bitmap: Bitmap,
    onResult: (String?) -> Unit
) {

    val cloudName = "dik62fiua"
    val uploadPreset = "sxb2wobj"

    val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file",
            "coffee.jpg",
            byteArray.toRequestBody("image/*".toMediaTypeOrNull())
        )
        .addFormDataPart("upload_preset", uploadPreset)
        .build()

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult(null)
        }

        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string()
            val json = JSONObject(body ?: "")
            val imageUrl = json.getString("secure_url")
            onResult(imageUrl)
        }
    })
}