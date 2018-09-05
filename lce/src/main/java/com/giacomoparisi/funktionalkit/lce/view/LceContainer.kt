package com.giacomoparisi.funktionalkit.lce.view

import android.content.Context
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.design.widget.CoordinatorLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import arrow.core.*
import arrow.typeclasses.binding
import com.giacomoparisi.funktionalkit.extensions.visibleOrGone
import com.giacomoparisi.funktionalkit.lce.Lce
import com.giacomoparisi.funktionalkit.lce.R

class LceContainer<T> private constructor(
        context: Context,
        private var _loadingLayoutId: Option<Int> = none(),
        private var _errorLayoutId: Option<Int> = none(),
        private var _errorMessageId: Option<Int> = none(),
        private var _errorRetryId: Option<Int> = none(),
        private var _retryAction: Option<() -> Unit> = none(),
        private var _networkErrorMessageRes: Option<Int> = none(),
        private var _onError: Option<(error: String, view: View, retry: Option<() -> Unit>) -> Unit> = none(),
        private var _onLoading: Option<() -> Unit> = none()
) : CoordinatorLayout(context) {

    private var _loading: Option<View> = none()
    private var _error: Option<View> = none()
    private var _errorMessage: Option<TextView> = none()
    private var _retry: Option<View> = none()

    var lce: Lce<T>? = null
        set(value) {
            when (value) {
                is Lce.Loading -> {
                    if (this.showLoading) {
                        for (i in 0 until this.childCount) {
                            this.getChildAt(i).visibleOrGone = this.getChildAt(i) == this._loading.orNull() || this.getChildAt(i) != this._error.orNull()
                        }
                        this._onLoading.map { it.invoke() }
                    } else
                        for (i in 0 until this.childCount) {
                            this.getChildAt(i).visibleOrGone = this.getChildAt(i) != this._loading.orNull() && this.getChildAt(i) != this._error.orNull()
                        }
                }
                is Lce.Success -> {
                    for (i in 0 until this.childCount) {
                        this.getChildAt(i).visibleOrGone = this.getChildAt(i) != this._loading.orNull() && this.getChildAt(i) != this._error.orNull()
                    }
                }
                is Lce.Error -> {
                    if (this.showError) {
                        for (i in 0 until this.childCount) {
                            this.getChildAt(i).visibleOrGone = this.getChildAt(i) == this._error.orNull() || this.getChildAt(i) != this._loading.orNull()
                        }
                        if (value.connectionError) {
                            val networkError = context.getString(_networkErrorMessageRes.getOrElse { R.string.ERROR_Network })
                            this._errorMessage.map { it.text = networkError }
                            this._onError.map { it.invoke(networkError, this, this._retryAction) }
                        } else {
                            this._errorMessage.map { it.text = value.message }
                            this._onError.map { it.invoke(value.message, this, this._retryAction) }
                        }
                    }
                }
                is Lce.Idle -> {
                    for (i in 0 until this.childCount) {
                        this.getChildAt(i).visibleOrGone = this.getChildAt(i) != this._loading.orNull() && this.getChildAt(i) != this._error.orNull()
                    }
                }
            }
        }


    var showLoading: Boolean = true

    var showError: Boolean = true

    data class Builder<T>(val context: Context) {

        private var _loadingLayoutId: Option<Int> = none()
        private var _errorLayoutId: Option<Int> = none()
        private var _errorMessageId: Option<Int> = none()
        private var _errorRetryId: Option<Int> = none()
        private var _retryAction: Option<() -> Unit> = none()
        private var _networkErrorMessageRes: Option<Int> = none()
        private var _onError: Option<(error: String, view: View, retry: Option<() -> Unit>) -> Unit> = none()
        private var _onLoading: Option<() -> Unit> = none()

        fun Builder<T>.error(
                @LayoutRes layoutId: Int,
                @IdRes errorMessageId: Int?,
                @IdRes errorRetryId: Int?,
                retryAction: () -> Unit,
                networkErrorMessageRes: Int?,
                onError: ((error: String, view: View, retry: Option<() -> Unit>) -> Unit)?
        ): Builder<T> {
            this._errorLayoutId = layoutId.toOption()
            this._errorMessageId = errorMessageId.toOption()
            this._errorRetryId = errorRetryId.toOption()
            this._retryAction = retryAction.toOption()
            this._networkErrorMessageRes = networkErrorMessageRes.toOption()
            this._onError = onError.toOption()
            return this@Builder
        }

        fun Builder<T>.error(
                networkErrorMessageRes: Int?,
                onError: ((error: String, view: View, retry: Option<() -> Unit>) -> Unit)
        ): Builder<T> {
            this._networkErrorMessageRes = networkErrorMessageRes.toOption()
            this._onError = onError.toOption()
            return this@Builder
        }

        fun Builder<T>.loading(@LayoutRes layoutId: Int, onLoading: (() -> Unit)?): Builder<T> {
            this._loadingLayoutId = layoutId.toOption()
            this._onLoading = onLoading.toOption()
            return this@Builder
        }

        fun build(): LceContainer<T> =
                LceContainer(
                        this@Builder.context,
                        this@Builder._loadingLayoutId,
                        this@Builder._errorLayoutId,
                        this@Builder._errorMessageId,
                        this@Builder._errorRetryId,
                        this@Builder._retryAction,
                        this@Builder._networkErrorMessageRes,
                        this@Builder._onError,
                        this@Builder._onLoading
                )
    }

    fun attachTo(view: View) {
        if (!isInEditMode) {
            this.addView(view)
            val inflater = LayoutInflater.from(context)
            _loading = _loadingLayoutId.map { inflater.inflate(it, this, false).also { addView(it) } }
            _error = _errorLayoutId.map { inflater.inflate(it, this, false).also { addView(it) } }
            _errorMessage = Option.monad().binding { _error.bind().findViewById<TextView>(_errorMessageId.bind()) }.fix()
            _retry = Option.monad().binding { _error.bind().findViewById<View>(_errorRetryId.bind()) }.fix()
            _retry.map { it.setOnClickListener { _retryAction.map { it() } } }

            val parent = (view.parent as ViewGroup?).toOption()

            Option.monad().binding {
                val index = parent.bind().indexOfChild(view)
                parent.bind().removeView(view)
                parent.bind().addView(this@LceContainer, index)
            }
        }
    }

    fun attachToRoot(view: View) {
        if (!isInEditMode) {
            val params = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            this.addView(view, params)
            val inflater = LayoutInflater.from(context)
            _loading = _loadingLayoutId.map { inflater.inflate(it, this, false).also { addView(it, params) } }
            _error = _errorLayoutId.map { inflater.inflate(it, this, false).also { addView(it, params) } }
            _errorMessage = Option.monad().binding { _error.bind().findViewById<TextView>(_errorMessageId.bind()) }.fix()
            _retry = Option.monad().binding { _error.bind().findViewById<View>(_errorRetryId.bind()) }.fix()
            _retry.map { it.setOnClickListener { _retryAction.map { it() } } }
        }
    }
}