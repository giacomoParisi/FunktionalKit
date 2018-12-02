package com.giacomoparisi.lce.live.data

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import arrow.Kind
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import arrow.effects.DeferredK
import arrow.effects.ForDeferredK
import arrow.effects.deferredk.monadDefer.monadDefer
import arrow.effects.handleErrorWith
import arrow.effects.runAsync
import arrow.effects.typeclasses.Disposable
import arrow.effects.typeclasses.bindingCancellable
import arrow.syntax.function.pipe
import com.giacomoparisi.kotlin.functional.extensions.arrow.option.ifSome
import com.giacomoparisi.kotlin.functional.extensions.core.fold
import com.giacomoparisi.lce.android.LceWrapper
import com.giacomoparisi.lce.core.Lce
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers

data class LiveDataLce<R>(
        val idle: Lce.Idle<R>,
        val deferredK: Kind<ForDeferredK, Lce<R>>,
        val liveData: MutableLiveData<Lce<R>>,
        val disposable: Disposable
)

fun <R> Lce.Idle<R>.liveDataLce() =
        MutableLiveData<Lce<R>>()
                .pipe { liveData ->
                    getLiveDataDeferred(liveData, this.provider).pipe {
                        LiveDataLce(this, it.a, liveData, it.b)
                    }
                }

private fun <R> getLiveDataDeferred(liveData: MutableLiveData<Lce<R>>, provider: DeferredK<R>) =
        DeferredK.monadDefer().bindingCancellable {
            DeferredK.invoke(ctx = Dispatchers.Main, f = { liveData.value = Lce.Loading }).bind()
            provider.map { it.right() }.handleErrorWith { DeferredK.just(it.left()) }.bind()
                    .fold({ Lce.Error(it) }) { Lce.Success(it) }
                    .also { DeferredK(ctx = Dispatchers.Main) { liveData.value = it } }
        }.fix()

fun <R, V : ViewGroup> LiveDataLce<R>.observe(
        owner: LifecycleOwner,
        lceWrapper: LceWrapper<V>? = null,
        onSuccess: ((R) -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
        onCancel: (() -> Unit)? = null,
        onLoading: (() -> Unit)? = null
) =
        this.also {
            this.liveData.observe(owner, Observer { lce ->
                lceWrapper.toOption().ifSome { wrapper -> wrapper.apply(lce) }
                when (lce) {
                    is Lce.Success -> onSuccess.toOption().ifSome { it(lce.data) }
                    is Lce.Error -> onError.toOption().ifSome { it(lce.throwable) }
                    is Lce.Idle -> onCancel.toOption().ifSome { it() }
                    Lce.Loading -> onLoading.toOption().ifSome { it() }
                }
            })
        }

fun <R> LiveDataLce<R>.execute() =
        this.also {
            this.deferredK
                    .handleErrorWith { throwable ->
                        (throwable is CancellationException)
                                .fold({ throw throwable })
                                { DeferredK(ctx = Dispatchers.Main) { Lce.Idle(this.idle.provider).also { idle -> this.liveData.value = idle } } }
                    }
                    .runAsync(cb = { DeferredK.unit() })
        }

