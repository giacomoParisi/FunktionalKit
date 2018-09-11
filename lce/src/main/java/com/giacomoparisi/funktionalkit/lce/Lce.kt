package com.giacomoparisi.funktionalkit.lce

import arrow.core.None
import arrow.core.Option
import arrow.core.toOption
import retrofit2.HttpException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * Created by Giacomo Parisi on 16/07/18.
 * https://github.com/giacomoParisi
 */
sealed class Lce<out T> {

    abstract fun <R> map(f: (Option<T>) -> R): Lce<R>

    open val data: Option<T> get() = None

    data class Success<out T>(override val data: Option<T>) : Lce<T>() {
        override fun <R> map(f: (Option<T>) -> R): Lce<R> = Success(f(data).toOption())
    }

    data class Error(val message: String, val connectionError: Boolean = false) : Lce<Nothing>() {
        constructor(t: Throwable) : this(t.message ?: "", t.isConnectionError())

        override fun <R> map(f: (Option<Nothing>) -> R): Lce<R> = this
    }

    object Loading : Lce<Nothing>() {
        override fun <R> map(f: (Option<Nothing>) -> R): Lce<R> = this
    }

    object Idle : Lce<Nothing>() {
        override fun <R> map(f: (Option<Nothing>) -> R): Lce<R> = this
    }
}

fun Throwable.isConnectionError(): Boolean =
        this is UnknownHostException ||
                this is SocketException ||
                this is SocketTimeoutException ||
                this is TimeoutException ||
                this is HttpException
