package com.giacomoparisi.lce.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import arrow.effects.DeferredK
import arrow.effects.unsafeRunAsync
import arrow.syntax.function.pipe
import com.giacomoparisi.kotlin.functional.extensions.android.toast.showLongToast
import com.giacomoparisi.kotlin.functional.extensions.arrow.option.ifSome
import com.giacomoparisi.lce.android.LceWrapper
import com.giacomoparisi.lce.android.getLceSettings
import com.giacomoparisi.lce.core.lce
import com.giacomoparisi.lce.live.data.execute
import com.giacomoparisi.lce.live.data.liveDataLce
import com.giacomoparisi.lce.live.data.observe
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.error.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {

    private val rootLceWrapper: LceWrapper = getLceWrapper()
    private val idLceWrapper: LceWrapper = getLceWrapper()

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.layoutInflater.inflate(R.layout.activity_main, null)
                .also { this.rootLceWrapper.build(this) }
                .also { this.idLceWrapper.build(this) }
                .pipe { this.rootLceWrapper.attachErrorToViewAndWrap(it as ViewGroup, FrameLayout(this)) }
                .pipe { this.rootLceWrapper.attachLoadingToView(it) }
                .pipe { this.idLceWrapper.attachErrorToView(it) }
                .pipe { this.idLceWrapper.attachLoadingToIdAndWrap(R.id.loading_id, it, this.layoutInflater.inflate(R.layout.linear_wrapper, null) as ViewGroup) }
                .pipe { this.setContentView(it) }

        this.root_loading_success.setOnClickListener {
            lce { delay(3000) }
                    .liveDataLce()
                    .observe(this, listOf(rootLceWrapper), { showSuccess() }, { showError() })
                    .execute()
        }

        this.root_loading_error.setOnClickListener {
            lce { delay(3000).pipe { throw Throwable() } }
                    .liveDataLce()
                    .observe(this, listOf(rootLceWrapper), { showSuccess() }, { showError() })
                    .execute()
        }

        this.root_loading_dispose.setOnClickListener {
            val lce = lce { delay(5000) }
                    .liveDataLce()
                    .observe(this, listOf(rootLceWrapper), { showSuccess() }, { showError() }, { showCancel() })
                    .execute()

            DeferredK.defer(f = {
                DeferredK(ctx = Dispatchers.Main) { lce.disposable() }
            }).unsafeRunAsync { }
        }

        this.loading_id.setOnClickListener {
            lce { delay(3000) }
                    .liveDataLce()
                    .observe(this, listOf(idLceWrapper), { showSuccess() }, { showError() }, { showCancel() })
                    .execute()
        }
    }

    private fun showSuccess() {
        this.showLongToast("Success")
    }

    private fun showError() {
        this.showLongToast("Error")
    }

    private fun showCancel() {
        this.showLongToast("Cancel")
    }

    private fun getLceWrapper() =
            LceWrapper(
                    getLceSettings(
                            R.layout.loading,
                            R.layout.error,
                            onError = { _, _, errorView, lceWrapper ->
                                errorView.ifSome { it.ok.setOnClickListener { lceWrapper.idle() } }
                            })
            )
}

