package com.giacomoparisi.lce.android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arrow.core.None
import arrow.core.Option
import arrow.core.fix
import arrow.instances.option.monad.monad
import arrow.syntax.function.pipe
import arrow.typeclasses.binding
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

        this._loading = Option.monad().binding {
            this@LceWrapper._settings.loading
                    .bind()
                    .loadingLayoutId
                    .bind()
                    .pipe { inflater.inflate(it, this@LceWrapper._root, false) }
                    .also { this@LceWrapper._root.addView(it, params) }
        }.fix()


        this._error = Option.monad().binding {
            this@LceWrapper._settings.error
                    .bind()
                    .errorLayoutId
                    .bind()
                    .pipe { inflater.inflate(it, this@LceWrapper._root, false) }
                    .also { this@LceWrapper._root.addView(it, params) }
        }.fix()

        this.apply(Lce.Idle)
    }

    fun addToRoot(view: View): View =
            ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
                    .pipe { this._root.addView(view, 0, it) }
                    .pipe { this._root }


    private fun isEqualAtRootIndex(view: Option<View>, index: Int): Boolean =
            this._root.getChildAt(index) == view.orNull()

    fun apply(lce: Lce<*>) {
        when (lce) {
            is Lce.Loading -> {
                for (i in 0 until this._root.childCount) {
                    this._root.getChildAt(i).visibleOrGone(isEqualAtRootIndex(this._loading, i)
                            .or(!isEqualAtRootIndex(this._error, i)))
                }
                this._settings.loading.ifSome { it.onLoading.ifSome { it(this._loading) } }
            }
            is Lce.Success -> {
                for (i in 0 until this._root.childCount) {
                    this._root.getChildAt(i).visibleOrGone(!isEqualAtRootIndex(this._loading, i)
                            .and(!isEqualAtRootIndex(this._error, i)))
                }
            }
            is Lce.Error -> {
                for (i in 0 until this._root.childCount) {
                    this._root.getChildAt(i).visibleOrGone(!isEqualAtRootIndex(this._loading, i)
                            .or(isEqualAtRootIndex(this._error, i)))
                }
                this._settings.error.ifSome {
                    it.onError.ifSome {
                        it(
                                lce.throwable,
                                lce.message,
                                this._error
                        )
                    }
                }
            }
            is Lce.Idle -> {
                for (i in 0 until this._root.childCount) {
                    this._root.getChildAt(i).visibleOrGone(!isEqualAtRootIndex(this._loading, i)
                            .and(!isEqualAtRootIndex(this._error, i)))
                }
            }
        }
    }
}