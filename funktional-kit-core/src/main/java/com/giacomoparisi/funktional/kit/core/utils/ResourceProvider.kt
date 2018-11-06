package com.giacomoparisi.funktional.kit.core.utils

import android.app.Application
import androidx.annotation.StringRes

/**
 * Created by Giacomo Parisi on 23/07/18.
 * https://github.com/giacomoParisi
 */
class ResourceProvider(private val _application: Application) {

    fun getString(@StringRes resId: Int): String {
        return _application.getString(resId)
    }

    fun getString(@StringRes resId: Int, args: Array<Any>): String {
        return _application.getString(resId, *args)
    }
}