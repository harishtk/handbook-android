package com.handbook.app.core.data.source.remote

import com.handbook.app.core.data.source.remote.model.UploaderResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface SellerApi {

    @POST("user/upload")
    @Multipart
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part type: MultipartBody.Part,
    ): Response<UploaderResponse>

}