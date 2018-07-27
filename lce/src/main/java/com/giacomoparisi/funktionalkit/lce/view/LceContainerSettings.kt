package com.giacomoparisi.funktionalkit.lce.view

import android.view.View
import androidx.annotation.StringRes
import arrow.core.Option
import arrow.core.toOption

/**
 * Created by Giacomo Parisi on 28/07/18.
 * https://github.com/JackParisi
 */
data class LceContainerSettings(
        private val _loadingLayoutId: Int? = null,
        private val _errorLayoutId: Int? = null,
        private val _errorMessageId: Int? = null,
        private val _errorRetryId: Int? = null,
        private val _retryAction: (() -> Unit)? = null,
        private val _onError: ((String, View, Option<() -> Unit>) -> Unit)? = null,
        private val _onLoading: (() -> Unit)? = null,
        @StringRes private val _networkErrorMessageRes: Int? = null
) {

    val loadingLayoutId = _loadingLayoutId.toOption()
    val errorLayoutId = _errorLayoutId.toOption()
    val errorMessageId = _errorMessageId.toOption()
    val errorRetryId = _errorRetryId.toOption()
    val retryAction = _retryAction.toOption()
    val onError = _onError.toOption()
    val onLoading = _onLoading.toOption()
    val networkErrorMessageRes = _networkErrorMessageRes.toOption()
}