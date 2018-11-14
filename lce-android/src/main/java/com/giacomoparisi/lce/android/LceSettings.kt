package com.giacomoparisi.lce.android

import android.view.View
import arrow.core.None
import arrow.core.Option

data class LceSettings(
        val loading: Option<LceLoadingSettings>,
        val error: Option<LceErrorSettings>
)

data class LceErrorSettings(
        val errorLayoutId: Option<Int> = None,
        val onError: Option<(
                throwable: Throwable,
                errorMessage: String,
                errorView: Option<View>
        ) -> Unit> = None
)

data class LceLoadingSettings(
        val loadingLayoutId: Option<Int> = None,
        val onLoading: Option<(loadingView: Option<View>) -> Unit> = None
)