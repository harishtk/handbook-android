package com.handbook.app.core.util

import com.handbook.app.BuildConfig
import com.handbook.app.Constant
import com.handbook.app.core.data.source.remote.model.UploaderResponse
import com.handbook.app.core.designsystem.component.forms.SellerMediaFile
import com.handbook.app.core.net.JwtInterceptor
import com.handbook.app.core.net.PlatformInterceptor
import com.handbook.app.core.net.ProgressRequestBody
import com.handbook.app.core.net.UserAgentInterceptor
import com.handbook.app.defaultJsonParser
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

data class FileUploaderCallback(
    val request: SellerMediaFile,
    val response: UploaderResponse?
)

class FileUploaderCallable(
    private val request: SellerMediaFile,
    private val type: String,
    private val file: File,
    private val onProgress: (progress: Int) -> Unit
) : Callable<FileUploaderCallback> {

    private val client: OkHttpClient = OkHttpClient.Builder()
        // .addNetworkInterceptor(progressInterceptor)
        .addInterceptor(PlatformInterceptor())
        .addInterceptor(UserAgentInterceptor())
        .addInterceptor(JwtInterceptor())
        .addInterceptor(
            HttpLoggingInterceptor()
                .apply {
                    if (BuildConfig.DEBUG) {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    }
                },
        )
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(15, TimeUnit.MINUTES)
        .build()

    override fun call(): FileUploaderCallback {
        Timber.d("Init upload type=$type file=${file.name}")
        val progressListener = object : ProgressRequestBody.ProgressCallback {
            override fun onProgressUpdate(percentage: Int) {
                Timber.d("onProgressUpdate() called with: percentage = [$percentage] file=${file.name}")
                onProgress(percentage)
            }

            override fun onError() {
                // Noop
            }
        }

        val progressRequestBody = ProgressRequestBody(file, request.mediaType.mimeType, progressListener)

        val filePart = MultipartBody.Part.createFormData(
            "file",
            file.name,
            progressRequestBody
        )

        val typePart = MultipartBody.Part.createFormData(
            "type",
            type
        )

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(typePart)
            .addPart(filePart)
            .build()

        val requestBuilder = Request.Builder()
            .url(BuildConfig.API_URL + "user/upload")
            .post(requestBody)

        return try {
            val response = client.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                response.body?.string()?.let { jsonString ->
                    Timber.d("Upload completed for ${file.name} response=$jsonString")
                    val model = defaultJsonParser.fromJson(jsonString, UploaderResponse::class.java)
                    FileUploaderCallback(
                        request = request,
                        response = model
                    )
                }
                    ?: error("Unable to parse server response")
            } else {
                FileUploaderCallback(
                    request = request,
                    UploaderResponse(
                        statusCode = response.code,
                        message = response.message,
                        data = null
                    )
                )
            }
        } catch (e: IOException) {
            Timber.e(e)
            FileUploaderCallback(
                request = request,
                response = UploaderResponse(
                    statusCode = HttpURLConnection.HTTP_INTERNAL_ERROR,
                    message = e.message ?: "Unable to upload the file",
                    data = null
                )
            )
        } catch (e: Exception) {
            Timber.e(e)
            FileUploaderCallback(
                request = request,
                response = UploaderResponse(
                    statusCode = HttpURLConnection.HTTP_INTERNAL_ERROR,
                    message = e.message ?: "Something went wrong",
                    data = null
                )
            )
        }
    }
}