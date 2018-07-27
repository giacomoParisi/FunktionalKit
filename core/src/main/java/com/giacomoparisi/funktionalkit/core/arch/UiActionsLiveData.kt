package com.giacomoparisi.funktionalkit.core.arch

import androidx.annotation.MainThread
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * Created by Giacomo Parisi on 16/07/18.
 * https://github.com/giacomoParisi
 */
typealias UiAction = (FragmentActivity) -> Unit

class UiActionsLiveData(private val coroutines: Coroutines) {
    private val delegate = MutableLiveData<List<UiAction>>()

    private var list: MutableList<UiAction> = ArrayList()

    fun executeFromUI(action: UiAction) {
        list.add(action)
        delegate.value = list
    }

    suspend operator fun invoke(action: UiAction) {
        coroutines.invokeOnUi {
            executeFromUI(action)
        }
    }

    fun observe(owner: LifecycleOwner, executor: (UiAction) -> Unit) =
            delegate.observe(owner, Observer {
                list.forEach { executor(it) }
                list = ArrayList()
            })

    @MainThread
    fun observeForever(executor: (UiAction) -> Unit) =
            delegate.observeForever {
                list.forEach { executor(it) }
                list = ArrayList()
            }
}
