package com.giacomoparisi.lce.core

import arrow.core.None
import arrow.core.Option
import arrow.core.toOption

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

    data class Error(val throwable: Throwable, val message: String) : Lce<Nothing>() {
        constructor(t: Throwable) : this(t, t.message ?: "")

        override fun <R> map(f: (Option<Nothing>) -> R): Lce<R> = this
    }

    object Loading : Lce<Nothing>() {
        override fun <R> map(f: (Option<Nothing>) -> R): Lce<R> = this
    }

    object Idle : Lce<Nothing>() {
        override fun <R> map(f: (Option<Nothing>) -> R): Lce<R> = this
    }
}
