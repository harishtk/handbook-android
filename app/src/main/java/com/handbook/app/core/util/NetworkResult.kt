package com.handbook.app.core.util

import javax.net.ssl.HttpsURLConnection

sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null,
    open val code: Int? = null,
    open val uiMessage: String? = null,
) {
    class Success<T>(
        data: T,
        message: String?,
        override val code: Int = HttpsURLConnection.HTTP_OK,
    ) : NetworkResult<T>(data, message)

    class Error<T>(
        val exception: Throwable?,
        message: String,
        override val uiMessage: String? = null,
        override val code: Int = ERROR_CODE_NONE,
    ) : NetworkResult<T>(code = code, message = message)
}

const val ERROR_CODE_NONE = -1