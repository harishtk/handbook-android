package com.handbook.app.core.util

import com.handbook.app.core.net.ApiException
import com.handbook.app.core.net.BadResponseException
import com.handbook.app.core.net.EmptyResponseException
import com.handbook.app.core.net.NoInternetException
import timber.log.Timber

interface NetworkResultParser {

    /**
     * Abstracts the boilerplate to parse error results
     */
    fun <T, R> parseErrorNetworkResult(networkResult: NetworkResult<T>): Result<R> {
        return when (networkResult) {
            is NetworkResult.Success -> {
                Timber.w("Not an error result")
                val cause = IllegalStateException("Parsing non error result!")
                Result.Error(ApiException(cause))
            }

            is NetworkResult.Error -> {
                when (networkResult.exception) {
                    is NoInternetException -> {
                        Result.Error(networkResult.exception)
                    }
                    else -> {
                        val cause = networkResult.exception
                            ?: IllegalStateException(networkResult.message ?: "Something went wrong")
                        Result.Error(ApiException(networkResult.message, cause))
                    }
                }
            }
        }
    }

    /**
     * Abstracts the boilerplate to ack. bad response or unexpected response code
     */
    fun badResponse(networkResult: NetworkResult<*>): Result.Error {
        val cause = BadResponseException("Unexpected response code ${networkResult.code}")
        return Result.Error(ApiException(cause))
    }

    /**
     * Abstracts the boilerplate to ack. empty response i.e. data is 'null'
     */
    fun emptyResponse(networkResult: NetworkResult<*>): Result.Error {
        val cause = EmptyResponseException("No data")
        return Result.Error(ApiException(cause))
    }
}