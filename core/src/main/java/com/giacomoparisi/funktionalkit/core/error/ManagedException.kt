package com.giacomoparisi.funktionalkit.core.error

import android.support.annotation.StringRes
import com.giacomoparisi.funktionalkit.core.utils.ResourceProvider

open class ManagedException : RuntimeException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace)
    constructor(resourceProvider: ResourceProvider, @StringRes message: Int) : super(resourceProvider.getString(message))
}
