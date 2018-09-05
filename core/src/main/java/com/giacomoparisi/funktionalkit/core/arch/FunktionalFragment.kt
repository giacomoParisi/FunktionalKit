package com.giacomoparisi.funktionalkit.core.arch

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by Giacomo Parisi on 17/07/18.
 * https://github.com/giacomoParisi
 */
open class FunktionalFragment : Fragment() {

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
        get() {
            if (field.isDisposed)
                field = CompositeDisposable()
            return field
        }

    protected fun dispose() {
        compositeDisposable.dispose()
    }

    fun Disposable.bindToLifecycle() {
        compositeDisposable.add(this)
    }

    data class ViewCreated(val view: View, val savedInstanceState: Bundle?)

    private val _onViewCreated = PublishSubject.create<ViewCreated>()
    protected val onViewCreated: Observable<ViewCreated> = _onViewCreated

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _onViewCreated.onNext(ViewCreated(view, savedInstanceState))
    }


    data class ActivityCreated(val savedInstanceState: Bundle?)

    private val _onActivityCreated = PublishSubject.create<ActivityCreated>()
    protected val onActivityCreated: Observable<ActivityCreated> = _onActivityCreated

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        _onActivityCreated.onNext(ActivityCreated(savedInstanceState))
    }


    data class Create(val savedInstanceState: Bundle?)

    private val _onCreate = PublishSubject.create<Create>()
    protected val onCreate: Observable<Create> = _onCreate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _onCreate.onNext(Create(savedInstanceState))
    }

    private val _onDestroy = PublishSubject.create<Boolean>()
    protected val onDestroy: Observable<Boolean> = _onDestroy

    override fun onDestroy() {
        _onDestroy.onNext(true)
        _onDestroy.onComplete()
        this.dispose()
        super.onDestroy()
    }
}