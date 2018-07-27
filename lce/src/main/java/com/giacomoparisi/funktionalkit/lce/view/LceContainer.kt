package com.giacomoparisi.funktionalkit.lce.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import arrow.core.*
import arrow.typeclasses.binding
import com.giacomoparisi.funktionalkit.extensions.visibleOrGone
import com.giacomoparisi.funktionalkit.lce.Lce
import com.giacomoparisi.funktionalkit.lce.R

class LceContainer<T>(
        context: Context,
        private val _lceSettings: LceContainerSettings
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
                        this.children.forEach { it.visibleOrGone = it == this._loading.orNull() || it != this._error.orNull() }
                        this._lceSettings.onLoading.map { it.invoke() }
                    } else
                        this.children.forEach { it.visibleOrGone = it != this._loading.orNull() && it != this._error.orNull() }
                }
                is Lce.Success -> {
                    this.children.forEach { it.visibleOrGone = it != this._loading.orNull() && it != this._error.orNull() }
                }
                is Lce.Error -> {
                    if (this.showError) {
                        this.children.forEach { it.visibleOrGone = it == this._error.orNull() || it != this._loading.orNull() }
                        if (value.connectionError) {
                            val networkError = context.getString(_lceSettings.networkErrorMessageRes.getOrElse { R.string.ERROR_Network })
                            this._errorMessage.map { it.text = networkError }
                            this._lceSettings.onError.map { it.invoke(networkError, this, this._lceSettings.retryAction) }
                        } else {
                            this._errorMessage.map { it.text = value.message }
                            this._lceSettings.onError.map { it.invoke(value.message, this, this._lceSettings.retryAction) }
                        }
                    }
                }
                is Lce.Idle -> {
                    children.forEach { it.visibleOrGone = it != this._loading.orNull() && it != this._error.orNull() }
                }
            }
        }


    var showLoading: Boolean = true

    var showError: Boolean = true

    fun attachTo(view: View) {
        if (!isInEditMode) {
            this.addView(view)
            val inflater = LayoutInflater.from(context)
            _loading = _lceSettings.loadingLayoutId.map { inflater.inflate(it, this, false).also { addView(it) } }
            _error = _lceSettings.errorLayoutId.map { inflater.inflate(it, this, false).also { addView(it) } }
            _errorMessage = Option.monad().binding { _error.bind().findViewById<TextView>(_lceSettings.errorMessageId.bind()) }.fix()
            _retry = Option.monad().binding { _error.bind().findViewById<View>(_lceSettings.errorRetryId.bind()) }.fix()
            _retry.map { it.setOnClickListener { _lceSettings.retryAction.map { it() } } }

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
            _loading = _lceSettings.loadingLayoutId.map { inflater.inflate(it, this, false).also { addView(it, params) } }
            _error = _lceSettings.errorLayoutId.map { inflater.inflate(it, this, false).also { addView(it, params) } }
            _errorMessage = Option.monad().binding { _error.bind().findViewById<TextView>(_lceSettings.errorMessageId.bind()) }.fix()
            _retry = Option.monad().binding { _error.bind().findViewById<View>(_lceSettings.errorRetryId.bind()) }.fix()
            _retry.map { it.setOnClickListener { _lceSettings.retryAction.map { it() } } }
        }
    }
}