package com.giacomoparisi.funktionalkit.core.arch

import android.os.AsyncTask
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI

/**
 * Created by Giacomo Parisi on 16/07/18.
 * https://github.com/giacomoParisi
 */
interface Coroutines {
    operator fun invoke(addToJob: Boolean = true, f: suspend () -> Unit)

    fun cancel()

    suspend fun invokeOnUi(f: () -> Unit)
}

class AndroidCoroutines : Coroutines {
    private val job = Job()

    override operator fun invoke(addToJob: Boolean, f: suspend () -> Unit) {
        launch(CommonPool, parent = if (addToJob) job else null) {
            try {
                f()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    override suspend fun invokeOnUi(f: () -> Unit) {
        withContext(UI) { f() }
    }

    override fun cancel() {
        job.cancel()
    }
}

class TestCoroutines : Coroutines {
    override fun invoke(addToJob: Boolean, f: suspend () -> Unit) {
        runBlocking { f() }
    }

    override fun cancel() {
    }

    override suspend fun invokeOnUi(f: () -> Unit) {
        withContext(CommonPool) { f() }
    }
}

class EspressoTestCoroutines : Coroutines {
    private val job = Job()

    override operator fun invoke(addToJob: Boolean, f: suspend () -> Unit) {
        launch(AsyncTask.THREAD_POOL_EXECUTOR.asCoroutineDispatcher(), parent = if (addToJob) job else null) {
            try {
                f()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    override suspend fun invokeOnUi(f: () -> Unit) {
        withContext(UI) { f() }
    }

    override fun cancel() {
        job.cancel()
    }
}