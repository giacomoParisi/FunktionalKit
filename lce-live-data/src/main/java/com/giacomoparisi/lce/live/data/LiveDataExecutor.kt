package com.giacomoparisi.lce.live.data

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import arrow.Kind
import arrow.core.*
import arrow.effects.DeferredK
import arrow.effects.ForDeferredK
import arrow.effects.deferredk.monadDefer.monadDefer
import arrow.effects.handleErrorWith
import arrow.effects.runAsync
import arrow.effects.typeclasses.Disposable
import arrow.effects.typeclasses.bindingCancellable
import arrow.syntax.function.pipe
import com.giacomoparisi.kotlin.functional.extensions.arrow.either.ifLeft
import com.giacomoparisi.kotlin.functional.extensions.arrow.option.ifSome
import com.giacomoparisi.kotlin.functional.extensions.core.fold
import com.giacomoparisi.lce.core.Lce
import com.giacomoparisi.lce.core.LceException
import com.giacomoparisi.lce.core.isInWitheList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers

data class LiveDataLce<R>(
        val deferredK: Kind<ForDeferredK, Lce<R>>,
        val liveData: MutableLiveData<Lce<R>>,
        val disposable: Disposable
)

fun <R> liveDataLce(
        whiteListExceptionsClassName: List<String> = emptyList(),
        f: suspend () -> Option<R>
) = MutableLiveData<Lce<R>>()
        .pipe { liveData ->
            getLiveDataDeferred(liveData, f, whiteListExceptionsClassName).pipe {
                LiveDataLce(it.a, liveData, it.b)
            }
        }

private fun <R> getLiveDataDeferred(
        liveData: MutableLiveData<Lce<R>>,
        f: suspend () -> Option<R>,
        whiteListExceptionsClassName: List<String>
) =
        DeferredK.monadDefer().bindingCancellable {
            DeferredK.invoke(ctx = Dispatchers.Main, f = { liveData.value = Lce.Loading }).bind()
            DeferredK.defer(f = { f().right() })
                    .handleErrorWith { handleError(it, whiteListExceptionsClassName) }
                    .bind()
                    .fold({ Lce.Error(it) }) { Lce.Success(it) }
                    .also { DeferredK(ctx = Dispatchers.Main) { liveData.value = it } }
        }.fix()

private fun handleError(throwable: Throwable,
                        whiteListExceptionsClassName: List<String>) =
        isInWitheList(throwable, whiteListExceptionsClassName)
                .or(throwable is LceException)
                .fold({ throw throwable }) { DeferredK.just(throwable.left()) }

fun <R> LiveDataLce<R>.observe(
        owner: LifecycleOwner,
        onNext: (Lce<R>) -> Unit,
        onSuccess: ((Option<R>) -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
        onLoading: (() -> Unit)? = null,
        onIdle: (() -> Unit)? = null
) =
        this.also {
            this.liveData.observe(owner, Observer { lce ->
                onNext(lce)
                when (lce) {
                    is Lce.Success -> onSuccess.toOption().ifSome { it(lce.data) }
                    is Lce.Error -> onError.toOption().ifSome { it(lce.throwable) }
                    Lce.Loading -> onLoading.toOption().ifSome { it() }
                    Lce.Idle -> onIdle.toOption().ifSome { it() }
                }
            })
        }

fun <R> LiveDataLce<R>.execute(onError: (Throwable) -> Unit) =
        this.deferredK
                .handleErrorWith { throwable ->
                    (throwable is CancellationException)
                            .fold({ throw throwable })
                            { DeferredK(ctx = Dispatchers.Main) { Lce.Idle.also { idle -> this.liveData.value = idle } } }
                }.runAsync(cb = { either ->
                    DeferredK {
                        either.ifLeft(onError).pipe { }
                    }
                })

