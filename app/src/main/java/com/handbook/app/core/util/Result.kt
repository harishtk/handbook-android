package com.handbook.app.core.util

import kotlinx.coroutines.flow.*
import java.util.Optional
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A generic class that holds data and it's state
 *
 * @param <T>
 */
sealed class Result<out R> {

    data class Success<R>(val data: R): Result<R>()
    data class Error(val exception: Throwable): Result<Nothing>()
    object Loading : Result<Nothing>()

    public val isSuccess: Boolean get() = this is Success
    public val isFailure: Boolean get() = this is Error

    override fun toString(): String {
        return when (this) {
            is Success<*>   -> "Success[data=$data]"
            is Error        -> "Error[exception=$exception]"
            Loading         -> "Loading"
        }
    }
}

/**
 * `true` if [Result] is of type [Result.Success] & holds a non-null [Result.Success.data].
 */
val Result<*>.succeeded
    get() = this is Result.Success && this.data != null

val Result<*>.succeededResult get() = (this as? Result.Success)?.data

fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> {
            Result.Success(it)
        }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it as Exception)) }
}

@OptIn(ExperimentalContracts::class)
inline fun <R> Result<R>.fold(
    onSuccess: (value: R) -> Unit,
    onFailure: (exception: Throwable) -> Unit
) {
    contract {
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }
    when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Error -> onFailure(exception)
        Result.Loading -> {}
    }
}

inline fun <T, R> Result<T>.map(transform: (value: T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
        Result.Loading -> Result.Loading
    }
}

public inline fun <T> Result<T>.onSuccess(action: (value: T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

public inline fun <T> Result<T>.onFailure(action: (exception: Throwable) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(exception)
    }
    return this
}