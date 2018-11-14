package com.giacomoparisi.lce.live.data

import arrow.core.Option
import arrow.core.Try
import com.giacomoparisi.kotlin.functional.extensions.arrow.`try`.ifFailure
import com.giacomoparisi.kotlin.functional.extensions.arrow.`try`.ifSuccess
import com.giacomoparisi.kotlin.functional.extensions.core.ifFalse
import com.giacomoparisi.kotlin.functional.extensions.core.ifTrue
import com.giacomoparisi.lce.core.Lce
import com.giacomoparisi.lce.core.LceException
import com.giacomoparisi.lce.core.isConnectionError

inline fun <R> runLce(f: () -> Option<R>): LiveData<Lce<R>> =
        MutableLiveData<Lce<R>>().also { liveData ->
            postOnUI { liveData.value = Lce.Loading }
            Try {
                Lce.Success(f())
            }.ifSuccess {
                postOnUI { liveData.value = it }
            }.ifFailure {
                it.isConnectionError()
                        .or(it is LceException)
                        .ifTrue { postOnUI { liveData.value = Lce.Error(it) } }
                        .ifFalse { throw it }
            }
        }