package com.giacomoparisi.lce.core

import arrow.effects.DeferredK

/**
 * Created by Giacomo Parisi on 16/07/18.
 * https://github.com/giacomoParisi
 */
sealed class Lce<out T> {

    data class Success<out T>(val data: T) : Lce<T>()

    data class Error(val throwable: Throwable, val message: String) : Lce<Nothing>() {
        constructor(t: Throwable) : this(t, t.message ?: "")
    }

    object Loading : Lce<Nothing>()

    data class Idle<T>(val provider: DeferredK<T>) : Lce<T>()
}

fun <T> lce(provider: suspend () -> T) =
        Lce.Idle(DeferredK.defer(f = { provider() }))