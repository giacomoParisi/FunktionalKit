package com.giacomoparisi.lce.android

import android.content.Context
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

class LceWrapper(private val _settings: LceSettings) {

    private var _loading: Option<View> = None
    private var _error: Option<View> = None

    fun build(context: Context) {
        val inflater = LayoutInflater.from(context)
        this._loading = this@LceWrapper._settings.loading
                .loadingLayoutId
                .map { layoutId ->
                    inflater.inflate(layoutId, null, false)
                }


        this._error = this@LceWrapper._settings.error
                .errorLayoutId
                .map { layoutId ->
                    inflater.inflate(layoutId, null, false)
                }
    }

    fun attachLoadingToView(toView: ViewGroup) =
            this._loading.fold({ toView }) { this.attachToView(it, toView) }

    fun attachLoadingToViewAndWrap(toView: ViewGroup, container: ViewGroup) =
            this._loading.fold({ toView }) { this.attachToViewAndWrap(it, toView, container) }

    fun attachErrorToView(toView: ViewGroup) =
            this._error.fold({ toView }) { this.attachToView(it, toView) }

    fun attachErrorToViewAndWrap(toView: ViewGroup, container: ViewGroup) =
            this._error.fold({ toView }) { this.attachToViewAndWrap(it, toView, container) }

    fun attachLoadingToId(@IdRes id: Int, toView: ViewGroup) =
            this._loading.fold({ toView }) { this.attachToViewWithId(it, id, toView) }

    fun attachLoadingToIdAndWrap(@IdRes id: Int, toView: ViewGroup, container: ViewGroup) =
            this._loading.fold({ toView }) { this.attachToViewWithIdAndWrap(it, id, toView, container) }

    fun attachErrorToId(@IdRes id: Int, toView: ViewGroup) =
            this._loading.fold({ toView }) { this.attachToViewWithId(it, id, toView) }

    fun attachErrorToIdAndWrap(@IdRes id: Int, toView: ViewGroup, container: ViewGroup) =
            this._loading.fold({ toView }) { this.attachToViewWithIdAndWrap(it, id, toView, container) }


    private fun attachToView(view: View, toView: ViewGroup): ViewGroup =
            ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
                    .pipe { toView.addView(view, it) }
                    .pipe { toView }
                    .also { this.apply(lce { }) }

    private fun attachToViewAndWrap(view: View, toView: View, container: ViewGroup): ViewGroup =
            ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
                    .also { container.addView(toView, it) }
                    .pipe { container.addView(view, it) }
                    .pipe { container }
                    .also { this.apply(lce { }) }

    private fun attachToViewWithIdAndWrap(view: View, @IdRes id: Int, toView: ViewGroup, container: ViewGroup): ViewGroup =
            (toView.findViewById<View>(id).parent as? ViewGroup).toOption().ifSome {
                val index = it.indexOfChild(view)
                val wrapView = toView.findViewById<View>(id)
                it.removeView(wrapView)
                it.addView(container, index, wrapView.layoutParams)
                val childParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
                container.addView(wrapView, 0, childParams)
                container.addView(view, childParams)
            }.pipe { toView }.also { this.apply(lce { }) }

    private fun attachToViewWithId(view: View, @IdRes id: Int, toView: ViewGroup): ViewGroup =
            (toView.findViewById<View>(id).parent as? ViewGroup).toOption().ifSome {
                val childParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
                it.addView(view, childParams)
            }.pipe { toView }.also { this.apply(lce { }) }

    fun apply(lce: Lce<*>) {
        when (lce) {
            is Lce.Loading -> {
                this.applyLoading()
            }
            is Lce.Success -> {
                this.applySuccess()
            }
            is Lce.Error -> {
                this.applyError(lce)
            }
            is Lce.Idle -> {
                this.applyIdle()
            }
        }
    }

    private fun applyLoading() {
        this._loading.ifSome { it.visibleOrGone(true) }
        this._error.ifSome { it.visibleOrGone(false) }
        this._settings.loading.onLoading.ifSome { it(this._loading) }
    }

    private fun applySuccess() {
        this._loading.ifSome { it.visibleOrGone(false) }
        this._error.ifSome { it.visibleOrGone(false) }
    }

    private fun applyError(lce: Lce.Error) {
        this._loading.ifSome { it.visibleOrGone(false) }
        this._error.ifSome { it.visibleOrGone(true) }
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

    private fun applyIdle() {
        this._loading.ifSome { it.visibleOrGone(false) }
        this._error.ifSome { it.visibleOrGone(false) }
    }

    fun idle() {
        this.apply(lce { })
    }
}
