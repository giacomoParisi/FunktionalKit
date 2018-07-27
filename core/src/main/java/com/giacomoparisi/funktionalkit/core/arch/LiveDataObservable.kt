package com.giacomoparisi.funktionalkit.core.arch

import androidx.lifecycle.LifecycleOwner

/**
 * Created by Giacomo Parisi on 16/07/18.
 * https://github.com/giacomoParisi
 */
interface LiveDataObservable<out T> {

    fun observe(owner: LifecycleOwner, observer: (T) -> Unit)

    fun observe2(owner: LifecycleOwner, observer: (T?, T) -> Unit)

    fun observeForever(observer: (T) -> Unit)
}