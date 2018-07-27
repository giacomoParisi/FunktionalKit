package com.giacomoparisi.funktionalkit.core.dagger

import com.giacomoparisi.funktionalkit.core.arch.AndroidCoroutines
import com.giacomoparisi.funktionalkit.core.arch.Coroutines
import com.giacomoparisi.funktionalkit.core.arch.ViewModelFactory
import dagger.Module
import dagger.Provides

/**
 * Created by Giacomo Parisi on 14/12/17.
 * https://github.com/giacomoParisi
 */
@Module()
object AntsCoreModule {

    @JvmStatic
    @Provides
    fun coroutines(): Coroutines = AndroidCoroutines()

    @JvmStatic
    @Provides
    fun viewModelFactory() = ViewModelFactory()
}
