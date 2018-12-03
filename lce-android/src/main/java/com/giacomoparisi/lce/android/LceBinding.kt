package com.giacomoparisi.lce.android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import arrow.core.None
import arrow.core.Option
import arrow.core.toOption
import arrow.syntax.function.pipe
import com.giacomoparisi.kotlin.functional.extensions.android.view.visibleOrGone
import com.giacomoparisi.kotlin.functional.extensions.arrow.option.ifSome
import com.giacomoparisi.lce.core.Lce
import com.giacomoparisi.lce.core.lce

class LceWrapper<V : ViewGroup>(
        private val _loadingRoot: V,
        private val _errorRoot: V,
        private val _settings: LceSettings
) {

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
        val inflater = LayoutInflater.from(this._loadingRoot.context)

        this._loading = this@LceWrapper._settings.loading
                .loadingLayoutId
                .map { layoutId ->
                    inflater.inflate(layoutId, this@LceWrapper._loadingRoot, false)
                            .also { this@LceWrapper._loadingRoot.addView(it, params) }
                }


        this._error = this@LceWrapper._settings.error
                .errorLayoutId
                .map { layoutId ->
                    inflater.inflate(layoutId, this@LceWrapper._errorRoot, false)
                            .also { this@LceWrapper._errorRoot.addView(it, params) }
                }
    }

    fun attachLoadingTo(view: View) =
            this.attach(view, this._loadingRoot)

    fun attachErrorTo(view: View) =
            this.attach(view, this._errorRoot)

    fun attachLoadingToId(view: View, @IdRes id: Int) =
            this.attachToViewWithId(view, id, this._loadingRoot)

    fun attachErrorToId(view: View, @IdRes id: Int) =
            this.attachToViewWithId(view, id, this._loadingRoot)


    private fun attach(view: View, root: ViewGroup): View =
            ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
                    .pipe { root.addView(view, 0, it) }
                    .pipe { root }
                    .also { this.apply(lce { }) }

    private fun attachToViewWithId(view: View, @IdRes id: Int, root: ViewGroup): View =
            (view.findViewById<View>(id).parent as? ViewGroup).toOption().ifSome {
                val index = it.indexOfChild(view)
                val wrapView = view.findViewById<View>(id)
                it.removeView(wrapView)
                it.addView(root, index, wrapView.layoutParams)
                val childParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
                root.addView(wrapView, 0, childParams)
            }.pipe { view }.also { this.apply(lce { }) }


    private fun isEqualAtRootIndex(view: Option<View>, index: Int, root: ViewGroup): Boolean =
            root.getChildAt(index) == view.orNull()

    fun apply(lce: Lce<*>) {
        when (lce) {
            is Lce.Loading -> {
                this.applyLoading(this._loadingRoot)
                this.applyLoading(this._errorRoot)
            }
            is Lce.Success -> {
                this.applySuccess(this._loadingRoot)
                this.applySuccess(this._errorRoot)
            }
            is Lce.Error -> {
                this.applyError(this._loadingRoot, lce)
                this.applyError(this._errorRoot, lce)
            }
            is Lce.Idle -> {
                this.applyIdle(this._loadingRoot)
                this.applyIdle(this._errorRoot)
            }
        }
    }

    private fun applyLoading(root: ViewGroup) {
        for (i in 0 until root.childCount) {
            this._loadingRoot.getChildAt(i).visibleOrGone(isEqualAtRootIndex(this._loading, i, root)
                    .or(isEqualAtRootIndex(this._error, i, root).not()))
        }
        this._settings.loading.onLoading.ifSome { it(this._loading) }
    }

    private fun applySuccess(root: ViewGroup) {
        for (i in 0 until root.childCount) {
            root.getChildAt(i).visibleOrGone(isEqualAtRootIndex(this._loading, i, root)
                    .not()
                    .and(isEqualAtRootIndex(this._error, i, root).not()))
        }
    }

    private fun applyError(root: ViewGroup, lce: Lce.Error) {
        for (i in 0 until root.childCount) {
            root.getChildAt(i).visibleOrGone(isEqualAtRootIndex(this._loading, i, root)
                    .not()
                    .or(isEqualAtRootIndex(this._error, i, root)))
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

    private fun applyIdle(root: ViewGroup) {
        for (i in 0 until root.childCount) {
            root.getChildAt(i).visibleOrGone(isEqualAtRootIndex(this._loading, i, root)
                    .not()
                    .and(isEqualAtRootIndex(this._error, i, root).not()))
        }
    }

    fun idle() {
        this.apply(lce { })
    }
}