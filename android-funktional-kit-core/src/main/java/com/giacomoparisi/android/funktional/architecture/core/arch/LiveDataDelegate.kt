package com.giacomoparisi.android.funktional.architecture.core.arch

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlin.reflect.KProperty

/**
 * Created by Giacomo Parisi on 16/07/18.
 * https://github.com/giacomoParisi
 */
class LiveDataDelegate<T : Any>(
        initialState: T,
        private val liveData: MutableLiveData<T> =
                MutableLiveData<T>()
) : LiveDataObservable<T> {

    var prevValue: T? = null

    init {
        liveData.value = initialState
    }

    override fun observe(owner: LifecycleOwner, observer: (T) -> Unit) =
            liveData.observe(owner, Observer { observer(it!!) })

    override fun observe2(owner: LifecycleOwner, observer: (T?, T) -> Unit) =
            liveData.observe(owner, Observer { observer(prevValue, it!!) })

    override fun observeForever(observer: (T) -> Unit) =
            liveData.observeForever { observer(it!!) }

    fun setValue(value: T) {
        prevValue = liveData.value
        liveData.value = value
    }

    operator fun getValue(ref: Any, p: KProperty<*>): T =
            liveData.value!!
}
