package com.giacomoparisi.lce.android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arrow.core.None
import arrow.core.Option
import arrow.syntax.function.pipe
import com.giacomoparisi.kotlin.functional.extensions.android.view.visibleOrGone
import com.giacomoparisi.kotlin.functional.extensions.arrow.option.ifSome
import com.giacomoparisi.lce.core.Lce

class LceWrapper<V : ViewGroup>(private val _root: V, private val _settings: LceSettings) {

    private var _loading: Option<View> = None
    private var _error: Option<View> = None

    init {
        this.buildView()
    }

    private fun buildView() {
        val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        val inflater = LayoutInflater.from(this._root.context)

        this._loading = this@LceWrapper._settings.loading
                .loadingLayoutId
                .map { layoutId ->
                    inflater.inflate(layoutId, this@LceWrapper._root, false)
                            .also { this@LceWrapper._root.addView(it, params) }
                }


        this._error = this@LceWrapper._settings.error
                .errorLayoutId
                .map { layoutId ->
                    inflater.inflate(layoutId, this@LceWrapper._root, false)
                            .also { this@LceWrapper._root.addView(it, params) }
                }
    }

    fun addToRoot(view: View): View =
            ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
                    .pipe { this._root.addView(view, 0, it) }
                    .pipe { this._root }
                    .also { this.apply(Lce.Idle) }


    private fun isEqualAtRootIndex(view: Option<View>, index: Int): Boolean =
            this._root.getChildAt(index) == view.orNull()

    fun apply(lce: Lce<*>) {
        when (lce) {
            is Lce.Loading -> {
                for (i in 0 until this._root.childCount) {
                    this._root.getChildAt(i).visibleOrGone(isEqualAtRootIndex(this._loading, i)
                            .or(isEqualAtRootIndex(this._error, i).not()))
                }
                this._settings.loading.onLoading.ifSome { it(this._loading) }
            }
            is Lce.Success -> {
                for (i in 0 until this._root.childCount) {
                    this._root.getChildAt(i).visibleOrGone(isEqualAtRootIndex(this._loading, i)
                            .not()
                            .and(isEqualAtRootIndex(this._error, i).not()))
                }
            }
            is Lce.Error -> {
                for (i in 0 until this._root.childCount) {
                    this._root.getChildAt(i).visibleOrGone(isEqualAtRootIndex(this._loading, i)
                            .not()
                            .or(isEqualAtRootIndex(this._error, i)))
                }
                this._settings.error
                        .onError.ifSome {
                    it(
                            lce.throwable,
                            lce.message,
                            this._error,
                            this
                    )
                }
            }
            is Lce.Idle -> {
                for (i in 0 until this._root.childCount) {
                    this._root.getChildAt(i).visibleOrGone(isEqualAtRootIndex(this._loading, i)
                            .not()
                            .and(isEqualAtRootIndex(this._error, i).not()))
                }
            }
        }
    }

    fun idle() {
        this.apply(Lce.Idle)
    }
}