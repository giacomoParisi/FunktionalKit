package com.giacomoparisi.lce.live.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import arrow.core.Option
import arrow.core.Try
import com.giacomoparisi.kotlin.functional.extensions.arrow.`try`.ifFailure
import com.giacomoparisi.kotlin.functional.extensions.arrow.`try`.ifSuccess
import com.giacomoparisi.kotlin.functional.extensions.core.ifFalse
import com.giacomoparisi.kotlin.functional.extensions.core.ifTrue
import com.giacomoparisi.kotlin.functional.extensions.coroutines.postOnUI
import com.giacomoparisi.lce.core.Lce
import com.giacomoparisi.lce.core.LceException
import com.giacomoparisi.lce.core.isInWitheList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun <R> liveDataLce(
        whiteListExceptionsClassName: List<String> = emptyList(),
        f: suspend () -> Option<R>
): LiveData<Lce<R>> =
        MutableLiveData<Lce<R>>().also { liveData ->
            GlobalScope.launch {
                postOnUI { liveData.value = Lce.Loading }
                Try {
                    Lce.Success(f())
                }.ifSuccess {
                    postOnUI { liveData.value = it }
                }.ifFailure {
                    isInWitheList(it, whiteListExceptionsClassName)
                            .or(it is LceException)
                            .ifTrue { postOnUI { liveData.value = Lce.Error(it) } }
                            .ifFalse { throw it }
                }
            }
        }