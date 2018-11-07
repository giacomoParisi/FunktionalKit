package com.giacomoparisi.android.funktional.architecture.core.dagger

import com.giacomoparisi.android.funktional.architecture.core.arch.AndroidCoroutines
import com.giacomoparisi.android.funktional.architecture.core.arch.Coroutines
import com.giacomoparisi.android.funktional.architecture.core.arch.ViewModelFactory
import dagger.Module
import dagger.Provides

/**
 * Created by Giacomo Parisi on 14/12/17.
 * https://github.com/giacomoParisi
 */
@Module()
object FunktionalCoreModule {

    @JvmStatic
    @Provides
    fun coroutines(): Coroutines = AndroidCoroutines()

    @JvmStatic
    @Provides
    fun viewModelFactory() = ViewModelFactory()
}
