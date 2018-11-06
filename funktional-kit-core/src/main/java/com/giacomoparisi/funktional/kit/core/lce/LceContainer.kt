package com.giacomoparisi.funktional.kit.core.lce

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import arrow.core.*
import arrow.instances.option.monad.monad
import arrow.typeclasses.binding
import com.giacomoparisi.funktionalkit.lce.Lce
import com.giacomoparisi.funktionalkit.lce.R
import com.giacomoparisi.kotlin.functional.extensions.android.view.visibleOrGone

class LceContainer<T> private constructor(
        context: Context,
        private var _loadingLayoutId: Option<Int> = none(),
        private var _errorLayoutId: Option<Int> = none(),
        private var _errorMessageId: Option<Int> = none(),
        private var _errorRetryId: Option<Int> = none(),
        private var _retryAction: Option<() -> Unit> = none(),
        private var _networkErrorMessageRes: Option<Int> = none(),
        private var _onError: Option<(throwable: Throwable, errorMessage: String, view: View, retry: Option<() -> Unit>) -> Unit> = none(),
        private var _onLoading: Option<() -> Unit> = none()
) : CoordinatorLayout(context) {

    private var _loading: Option<View> = none()
    private var _error: Option<View> = none()
    private var _errorMessage: Option<TextView> = none()
    private var _retry: Option<View> = none()

    private val _params = CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

    init {
        this._params.gravity = Gravity.CENTER
        this.layoutParams = this._params
    }

    var lce: Lce<T>? = null
        set(value) {
            when (value) {
                is Lce.Loading -> {
                    if (this.showLoading) {
                        for (i in 0 until this.childCount) {
                            this.getChildAt(i).visibleOrGone(this.getChildAt(i) == this._loading.orNull() || this.getChildAt(i) != this._error.orNull())
                        }
                        this._onLoading.map { it.invoke() }
                    } else
                        for (i in 0 until this.childCount) {
                            this.getChildAt(i).visibleOrGone(this.getChildAt(i) != this._loading.orNull() && this.getChildAt(i) != this._error.orNull())
                        }
                }
                is Lce.Success -> {
                    for (i in 0 until this.childCount) {
                        this.getChildAt(i).visibleOrGone(this.getChildAt(i) != this._loading.orNull() && this.getChildAt(i) != this._error.orNull())
                    }
                }
                is Lce.Error -> {
                    if (this.showError) {
                        for (i in 0 until this.childCount) {
                            this.getChildAt(i).visibleOrGone(this.getChildAt(i) == this._error.orNull() || this.getChildAt(i) != this._loading.orNull())
                        }
                        if (value.connectionError) {
                            val networkError = context.getString(_networkErrorMessageRes.getOrElse { R.string.ERROR_Network })
                            this._errorMessage.map { it.text = networkError }
                            this._onError.map { it.invoke(value.throwable, networkError, this, this._retryAction) }
                        } else {
                            this._errorMessage.map { it.text = value.message }
                            this._onError.map { it.invoke(value.throwable, value.message, this, this._retryAction) }
                        }
                    }
                }
                is Lce.Idle -> {
                    for (i in 0 until this.childCount) {
                        this.getChildAt(i).visibleOrGone(this.getChildAt(i) != this._loading.orNull() && this.getChildAt(i) != this._error.orNull())
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
        private var _onError: Option<(throwable: Throwable, errorMessage: String, view: View, retry: Option<() -> Unit>) -> Unit> = none()
        private var _onLoading: Option<() -> Unit> = none()

        fun Builder<T>.error(
                @LayoutRes layoutId: Int,
                retryAction: () -> Unit,
                @IdRes errorMessageId: Int? = null,
                @IdRes errorRetryId: Int? = null,
                networkErrorMessageRes: Int? = null,
                onError: ((throwable: Throwable, error: String, view: View, retry: Option<() -> Unit>) -> Unit)? = null
        ): Builder<T> {
            this._errorLayoutId = layoutId.toOption()
            this._errorMessageId = errorMessageId.toOption()
            this._errorRetryId = errorRetryId.toOption()
            this._retryAction = retryAction.toOption()
            this._networkErrorMessageRes = networkErrorMessageRes.toOption()
            this._onError = onError.toOption()
            return this@Builder
        }

        fun error(onError: ((throwable: Throwable, error: String, view: View, retry: Option<() -> Unit>) -> Unit),
                  retryAction: () -> Unit,
                  networkErrorMessageRes: Int? = null
        ): Builder<T> {
            this._networkErrorMessageRes = networkErrorMessageRes.toOption()
            this._onError = onError.toOption()
            this._retryAction = retryAction.toOption()
            return this@Builder
        }

        fun loading(@LayoutRes layoutId: Int, onLoading: (() -> Unit)? = null): Builder<T> {
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

            _loading = _loadingLayoutId.map { layout ->
                inflater.inflate(layout, this, false)
                        .also { addView(it) }
            }

            _error = _errorLayoutId.map { layout ->
                inflater.inflate(layout, this, false)
                        .also { addView(it) }
            }

            _errorMessage = Option.monad()
                    .binding { _error.bind().findViewById<TextView>(_errorMessageId.bind()) }
                    .fix()

            _retry = Option.monad()
                    .binding { _error.bind().findViewById<View>(_errorRetryId.bind()) }
                    .fix()

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
            val params = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            this.addView(view, params)
            val inflater = LayoutInflater.from(context)

            _loading = _loadingLayoutId.map { layoutId ->
                inflater.inflate(layoutId, this, false)
                        .also { addView(it, params) }
            }

            _error = _errorLayoutId.map { layoutId ->
                inflater.inflate(layoutId, this, false)
                        .also { addView(it, params) }
            }

            _errorMessage = Option.monad()
                    .binding { _error.bind().findViewById<TextView>(_errorMessageId.bind()) }
                    .fix()

            _retry = Option.monad()
                    .binding { _error.bind().findViewById<View>(_errorRetryId.bind()) }
                    .fix()

            _retry.map { retryButton -> retryButton.setOnClickListener { _retryAction.map { it() } } }
        }
    }
}