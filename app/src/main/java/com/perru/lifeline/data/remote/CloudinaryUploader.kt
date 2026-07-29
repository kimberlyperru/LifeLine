package com.perru.lifeline.data.remote

import android.content.Context
import android.net.Uri
import com.perru.lifeline.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uploads images to Cloudinary via a plain HTTPS multipart POST to the unsigned
 * upload endpoint — deliberately NOT using the cloudinary-android SDK. That SDK
 * bundles an old copy of Fresco (native image-processing libraries) that isn't
 * built for 16 KB memory pages, which trips Android's compatibility warning on
 * newer devices. A REST call needs no native code at all, so the issue can't
 * occur here.
 *
 * Configure an UNSIGNED upload preset in the Cloudinary console (Settings ->
 * Upload -> Upload presets -> Add upload preset, Signing Mode: Unsigned) and
 * put its name in BuildConfig.CLOUDINARY_UPLOAD_PRESET.
 */
@Singleton
class CloudinaryUploader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient()

    suspend fun uploadImage(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tempFile = copyUriToTempFile(uri)
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                .addFormDataPart("folder", "lifeline_verification")
                .addFormDataPart(
                    "file",
                    tempFile.name,
                    tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/${BuildConfig.CLOUDINARY_CLOUD_NAME}/image/upload")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                tempFile.delete()
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val message = runCatching { JSONObject(responseBody).getJSONObject("error").getString("message") }
                        .getOrDefault("Upload failed (HTTP ${response.code})")
                    return@withContext Result.failure(IllegalStateException(message))
                }
                val secureUrl = JSONObject(responseBody).getString("secure_url")
                Result.success(secureUrl)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyUriToTempFile(uri: Uri): File {
        val tempFile = File.createTempFile("lifeline_upload", ".tmp", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
        } ?: error("Could not read the selected image")
        return tempFile
    }
}
