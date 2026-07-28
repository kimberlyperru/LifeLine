package com.perru.lifeline.data.remote

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.perru.lifeline.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CloudinaryUploader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var initialized = false

    private fun ensureInitialized() {
        if (initialized) return
        val config = mapOf("cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME)
        MediaManager.init(context, config)
        initialized = true
    }

    suspend fun uploadImage(uri: Uri): Result<String> {
        ensureInitialized()
        return suspendCancellableCoroutine { continuation ->
            val requestId = MediaManager.get().upload(uri)
                .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                .option("folder", "lifeline_verification")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) = Unit
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) = Unit

                    override fun onSuccess(requestId: String, resultData: MutableMap<Any?, Any?>) {
                        val url = resultData["secure_url"] as? String
                        if (url != null) {
                            continuation.resume(Result.success(url))
                        } else {
                            continuation.resume(Result.failure(IllegalStateException("Upload succeeded but no URL returned")))
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resumeWithException(RuntimeException(error.description))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) = Unit
                })
                .dispatch()

            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
            }
        }
    }
}