package com.giacomoparisi.funktionalkit

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import arrow.core.Option
import com.giacomoparisi.funktionalkit.core.BuildConfig
import com.giacomoparisi.funktionalkit.core.arch.Coroutines
import com.giacomoparisi.funktionalkit.core.arch.LiveDataDelegate
import com.giacomoparisi.funktionalkit.core.arch.UiAction
import com.giacomoparisi.funktionalkit.core.arch.UiActionsLiveData
import com.giacomoparisi.funktionalkit.lce.Lce
import com.giacomoparisi.funktionalkit.lce.isConnectionError
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseViewModel<T : Any>(
        val coroutines: Coroutines,
        initialState: T
) : ViewModel() {

    //#region DISPOSABLE
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
        get() {
            if (field.isDisposed)
                field = CompositeDisposable()
            return field
        }

    protected fun dispose() {
        compositeDisposable.dispose()
    }

    fun Disposable.bindToLifecycle() {
        compositeDisposable.add(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        dispose()
    }
    //endregion

    //#region STATE
    private val liveData = LiveDataDelegate(initialState)

    val state by liveData

    suspend inline fun postState(crossinline f: (T) -> T) {
        coroutines.invokeOnUi {
            setState(f(state))
        }
    }

    fun setState(newState: T) {
        liveData.setValue(newState)
    }
    //endregion

    //#region UI
    private val uiActions = UiActionsLiveData(coroutines)

    fun uiAction(action: UiAction) {
        uiActions.executeFromUI(action)
    }

    suspend fun postUiAction(action: UiAction) {
        uiActions.invoke(action)
    }
    //endregion

    //#region OBSERVABLE
    override fun onCleared() = coroutines.cancel()

    fun observe(fragment: Fragment, observer: (T) -> Unit) {
        liveData.observe(fragment, observer)
        uiActions.observe(fragment) { it(fragment.requireActivity()) }
        onOwnerConnected(fragment)
    }

    fun observe2(fragment: Fragment, observer: (T?, T) -> Unit) {
        liveData.observe2(fragment, observer)
        uiActions.observe(fragment) { it(fragment.requireActivity()) }
        onOwnerConnected(fragment)
    }

    fun observe(activity: AppCompatActivity, observer: (T) -> Unit) {
        liveData.observe(activity, observer)
        uiActions.observe(activity) { it(activity) }
        onOwnerConnected(activity)
    }

    fun observe2(activity: AppCompatActivity, observer: (T?, T) -> Unit) {
        liveData.observe2(activity, observer)
        uiActions.observe(activity) { it(activity) }
        onOwnerConnected(activity)
    }

    open fun onOwnerConnected(owner: LifecycleOwner) {

    }

    fun observeForever(stateObserver: (T) -> Unit, actionObserve: (UiAction) -> Unit) {
        liveData.observeForever(stateObserver)
        uiActions.observeForever(actionObserve)
    }
    //endregion

    suspend inline fun <R> execLce(crossinline copy: (Lce<R>) -> T, f: () -> Option<R>) {
        postState {
            copy(Lce.Loading)
        }
        try {
            val success = Lce.Success(f())
            postState {
                copy(success)
            }
        } catch (e: Throwable) {
            if (e.isConnectionError() || BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace()
                }
                postState {
                    copy(Lce.Error(e))
                }
            } else {
                throw e
            }
        }
    }
}