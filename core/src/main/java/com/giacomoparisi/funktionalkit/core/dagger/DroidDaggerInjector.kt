package com.giacomoparisi.funktionalkit.core.dagger

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerApplication

/**
 * Created by Giacomo Parisi on 30/06/2017.
 * https://github.com/giacomoParisi
 */

// Handle application dagger injection
abstract class DroidDaggerInjector {

    abstract fun injectComponent(): AndroidInjector<out DaggerApplication>

    fun init(application: Application): AndroidInjector<out DaggerApplication> {
        val injector = injectComponent()
        application
                .registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                    override fun onActivityPaused(activity: Activity?) {}

                    override fun onActivityResumed(activity: Activity?) {}

                    override fun onActivityStarted(activity: Activity?) {}

                    override fun onActivityDestroyed(activity: Activity?) {}

                    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

                    override fun onActivityStopped(activity: Activity?) {}

                    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                        activity?.let { handleInjection(it) }
                    }

                })
        return injector
    }

    private fun handleInjection(activity: Activity) {
        if (activity.javaClass.isAnnotationPresent(Injectable::class.java)) {
            AndroidInjection.inject(activity)
        }
        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                    object : FragmentManager.FragmentLifecycleCallbacks() {

                        override fun onFragmentCreated(
                                fm: FragmentManager,
                                f: Fragment,
                                savedInstanceState: Bundle?
                        ) {
                            if (f.javaClass.isAnnotationPresent(Injectable::class.java)) {
                                AndroidSupportInjection.inject(f)
                            }
                        }
                    }, true)
        }
    }
}
