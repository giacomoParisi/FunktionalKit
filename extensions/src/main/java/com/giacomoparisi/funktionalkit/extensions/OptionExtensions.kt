package com.giacomoparisi.funktionalkit.extensions

import android.support.annotation.StringRes
import arrow.core.*
import arrow.typeclasses.binding
import com.giacomoparisi.funktionalkit.core.error.ManagedException
import com.giacomoparisi.funktionalkit.core.utils.ResourceProvider

suspend inline fun <T, R> Option<T>.run(someAction: (T) -> Option<R>, noneAction: () -> Option<R>): Option<R> {
    return when (this) {
        is Some -> Option.monad().binding { this@run.bind() }.fix().orNull()?.let { someAction(it) }!!
        is None -> noneAction()
    }
}

suspend inline fun <T> Option<T>.run(someAction: (T) -> Unit) {
    if (this.isDefined()) {
        Option.monad().binding { this@run.bind() }.fix().orNull()?.let { someAction(it) }
    }
}

fun <T> Option<T>.error(resourceProvider: ResourceProvider, @StringRes error: Int): Option<T> =
        this.fold({ throw ManagedException(resourceProvider, error) }) { this }