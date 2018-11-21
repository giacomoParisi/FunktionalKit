package com.giacomoparisi.lce.android

import android.view.View
import arrow.core.None
import arrow.core.Option
import arrow.core.toOption

data class LceSettings(
        val loading: LceLoadingSettings,
        val error: LceErrorSettings
)

data class LceErrorSettings(
        val errorLayoutId: Option<Int> = None,
        val onError: Option<(
                throwable: Throwable,
                errorMessage: String,
                errorView: Option<View>,
                lceWrapper: LceWrapper<*>
        ) -> Unit> = None
)

data class LceLoadingSettings(
        val loadingLayoutId: Option<Int> = None,
        val onLoading: Option<(loadingView: Option<View>) -> Unit> = None
)

fun getLceSettings(
        loadingLayoutId: Int? = null,
        errorLayoutId: Int? = null,
        onLoading: ((loadingView: Option<View>) -> Unit)? = null,
        onError: ((throwable: Throwable, errorMessage: String, errorView: Option<View>, LceWrapper<*>) -> Unit)? = null
) =
        LceSettings(
                LceLoadingSettings(loadingLayoutId.toOption(), onLoading.toOption()),
                LceErrorSettings(errorLayoutId.toOption(), onError.toOption())
        )