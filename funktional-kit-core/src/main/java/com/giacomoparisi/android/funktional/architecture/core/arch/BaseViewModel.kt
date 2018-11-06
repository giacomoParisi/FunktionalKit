package com.giacomoparisi.android.funktional.architecture.core.arch

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import arrow.core.Option
import arrow.core.toOption
import com.giacomoparisi.android.funktional.architecture.core.BuildConfig
import com.giacomoparisi.android.funktional.architecture.core.error.ManagedException
import com.giacomoparisi.android.funktional.architecture.core.lce.Lce
import com.giacomoparisi.android.funktional.architecture.core.lce.isConnectionError
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseViewModel<T : Any>(
        val coroutines: Coroutines,
        initialState: T
) : ViewModel(), LifecycleObserver {

    //#region DISPOSABLE
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
        get() {
            if (field.isDisposed)
                field = CompositeDisposable()
            return field
        }

    protected fun dispose() {
        this.compositeDisposable.dispose()
    }

    fun Disposable.bindToLifecycle() {
        this@BaseViewModel.compositeDisposable.add(this)
    }

    override fun onCleared() {
        this.dispose()
        super.onCleared()
    }

    //endregion

    //#region STATE
    private val liveData = LiveDataDelegate(initialState)

    val state by liveData

    suspend inline fun postState(crossinline f: (T) -> T) {
        this.coroutines.invokeOnUi {
            this.setState(f(this.state))
        }
    }

    fun setState(newState: T) {
        this.liveData.setValue(newState)
    }
    //endregion

    //#region UI
    private val uiActions = UiActionsLiveData(coroutines)

    fun uiAction(action: UiAction) {
        this.uiActions.executeFromUI(action)
    }

    suspend fun postUiAction(action: UiAction) {
        this.uiActions.invoke(action)
    }
    //endregion

    //#region OBSERVABLE

    fun observe(fragment: Fragment, observer: (T) -> Unit) {
        this.liveData.observe(fragment, observer)
        this.uiActions.observe(fragment) { it(fragment.requireActivity()) }
        this.onOwnerConnected(fragment)
    }

    fun observe2(fragment: Fragment, observer: (Option<T>, T) -> Unit) {
        this.liveData.observe2(fragment) { oldState: T?, newState: T -> observer.invoke(oldState.toOption(), newState) }
        this.uiActions.observe(fragment) { it(fragment.requireActivity()) }
        this.onOwnerConnected(fragment)
    }

    fun observe(activity: AppCompatActivity, observer: (T) -> Unit) {
        this.liveData.observe(activity, observer)
        this.uiActions.observe(activity) { it(activity) }
        this.onOwnerConnected(activity)
    }

    fun observe2(activity: AppCompatActivity, observer: (Option<T>, T) -> Unit) {
        this.liveData.observe2(activity) { oldState: T?, newState: T -> observer.invoke(oldState.toOption(), newState) }
        this.uiActions.observe(activity) { it(activity) }
        this.onOwnerConnected(activity)
    }

    open fun onOwnerConnected(owner: LifecycleOwner) {

    }

    fun observeForever(stateObserver: (T) -> Unit, actionObserve: (UiAction) -> Unit) {
        this.liveData.observeForever(stateObserver)
        this.uiActions.observeForever(actionObserve)
    }
    //endregion

    suspend inline fun <R> execLce(crossinline copy: (Lce<R>) -> T, f: () -> Option<R>) {
        this.postState {
            copy(Lce.Loading)
        }
        try {
            val success = Lce.Success(f())
            this.postState {
                copy(success)
            }
        } catch (e: Throwable) {
            if (e.isConnectionError() || e is ManagedException || BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace()
                }
                this.postState {
                    copy(Lce.Error(e))
                }
            } else {
                throw e
            }
        }
    }
}