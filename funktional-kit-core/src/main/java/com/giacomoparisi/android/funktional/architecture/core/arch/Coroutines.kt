package com.giacomoparisi.android.funktional.architecture.core.arch

import android.os.AsyncTask
import kotlinx.coroutines.*

/**
 * Created by Giacomo Parisi on 16/07/18.
 * https://github.com/giacomoParisi
 */
interface Coroutines {
    operator fun invoke(addToJob: Boolean = true, f: suspend () -> Unit)

    suspend fun invokeOnUi(f: () -> Unit)
}

class AndroidCoroutines : Coroutines {

    override operator fun invoke(addToJob: Boolean, f: suspend () -> Unit) {
        GlobalScope.launch {
            try {
                f()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    override suspend fun invokeOnUi(f: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch { f() }
    }
}

class TestCoroutines : Coroutines {
    override fun invoke(addToJob: Boolean, f: suspend () -> Unit) {
        runBlocking { f() }
    }

    override suspend fun invokeOnUi(f: () -> Unit) {
        GlobalScope.launch { f() }
    }
}

class EspressoTestCoroutines : Coroutines {
    private val job = Job()

    override operator fun invoke(addToJob: Boolean, f: suspend () -> Unit) {
        CoroutineScope(AsyncTask.THREAD_POOL_EXECUTOR.asCoroutineDispatcher()).launch {
            try {
                f()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    override suspend fun invokeOnUi(f: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch { f() }
        GlobalScope.coroutineContext.cancelChildren()
    }
}