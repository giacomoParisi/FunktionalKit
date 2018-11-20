package com.giacomoparisi.lce.live.data

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import arrow.Kind
import arrow.core.*
import arrow.effects.DeferredK
import arrow.effects.ForDeferredK
import arrow.effects.deferredk.monadDefer.monadDefer
import arrow.effects.typeclasses.Disposable
import arrow.effects.typeclasses.bindingCancellable
import arrow.syntax.function.pipe
import com.giacomoparisi.kotlin.functional.extensions.core.fold
import com.giacomoparisi.lce.core.Lce
import com.giacomoparisi.lce.core.LceException
import com.giacomoparisi.lce.core.isInWitheList
import kotlinx.coroutines.Dispatchers

data class LiveDataLce<R>(
        val deferredK: Kind<ForDeferredK, Option<R>>,
        val liveData: LiveData<Lce<R>>,
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
            val result = DeferredK.defer(f = { f().right() })
                    .handleErrorWith { handleError(it, whiteListExceptionsClassName) }
                    .bind()
            when (result) {
                is Either.Left -> liveData.value = Lce.Error(result.a)
                is Either.Right -> liveData.value = Lce.Success(result.b)
            }
            result.fold({ None }) { option -> option }
        }.fix()

private fun handleError(throwable: Throwable,
                        whiteListExceptionsClassName: List<String>) =
        isInWitheList(throwable, whiteListExceptionsClassName)
                .or(throwable is LceException)
                .fold({ throw throwable }) { DeferredK.just(throwable.left()) }

fun <R> LiveDataLce<R>.observe(owner: LifecycleOwner, onNext: (Lce<R>) -> Unit) =
        this.also {
            this.liveData.observe(owner, Observer { lce -> onNext(lce) })
        }

