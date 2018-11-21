package com.giacomoparisi.lce.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import arrow.core.Option
import arrow.core.some
import arrow.syntax.function.pipe
import com.giacomoparisi.kotlin.functional.extensions.android.toast.showLongToast
import com.giacomoparisi.lce.android.LceErrorSettings
import com.giacomoparisi.lce.android.LceLoadingSettings
import com.giacomoparisi.lce.android.LceSettings
import com.giacomoparisi.lce.android.LceWrapper
import com.giacomoparisi.lce.live.data.LiveDataLce
import com.giacomoparisi.lce.live.data.execute
import com.giacomoparisi.lce.live.data.liveDataLce
import com.giacomoparisi.lce.live.data.observe
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {

    private val loadingSettings = LceLoadingSettings(R.layout.loading.some()).some()
    private val errorSettings = LceErrorSettings(onError = { _: Throwable, message: String, _: Option<View> -> this.showLongToast(message) }.some()).some()

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLceWrapper = LceWrapper(
                FrameLayout(this),
                LceSettings(loadingSettings, errorSettings)
        )

        rootLceWrapper.addToRoot(this.layoutInflater.inflate(
                R.layout.activity_main,
                null
        )).pipe { this.setContentView(it) }

        this.root_loading_success.setOnClickListener { _ ->
            liveDataLce {
                delay(5000)
                "Completed".some()
            }.pipe { result ->
                result.also { it.execute { } }
                        .also { this.observeOnRoot(it, rootLceWrapper) }
            }
        }

        this.root_loading_error.setOnClickListener { _ ->
            liveDataLce(listOf(IllegalArgumentException::class.java.name)) {
                delay(5000)
                throw IllegalArgumentException("Error")
                "Completed".some()
            }.pipe { result ->
                result.also { it.execute { } }
                        .also { this.observeOnRoot(it, rootLceWrapper) }
            }
        }

        this.root_loading_error_not_handled.setOnClickListener { _ ->
            liveDataLce {
                delay(3000)
                throw IllegalArgumentException("Error")
                "Completed".some()
            }.pipe { result ->
                result.also { it.execute { } }
                        .also { this.observeOnRoot(it, rootLceWrapper) }
            }
        }

        this.root_loading_dispose.setOnClickListener { _ ->
            liveDataLce(listOf(IllegalArgumentException::class.java.name)) {
                delay(10000)
                "Completed".some()
            }.pipe { result ->
                result.also { it.execute { } }
                        .also { this.observeOnRoot(it, rootLceWrapper) }
                        .also {
                            it.disposable()
                        }
            }
        }
    }

    private fun <R> observeOnRoot(liveDataLce: LiveDataLce<R>, lceWrapper: LceWrapper<*>) {
        liveDataLce.observe(
                this,
                onNext = { lceWrapper.apply(it) },
                onSuccess = { showSuccess() },
                onIdle = { showCancel() }
        )
    }

    private fun showSuccess() {
        this.showLongToast("Success")
    }

    private fun showCancel() {
        this.showLongToast("Cancel")
    }
}

