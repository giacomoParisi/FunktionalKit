package com.giacomoparisi.lce.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import arrow.core.None
import arrow.core.some
import arrow.effects.unsafeRunAsync
import arrow.syntax.function.pipe
import com.giacomoparisi.lce.android.LceLoadingSettings
import com.giacomoparisi.lce.android.LceSettings
import com.giacomoparisi.lce.android.LceWrapper
import com.giacomoparisi.lce.live.data.liveDataLce
import com.giacomoparisi.lce.live.data.observe
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {

    private val loadingSettings = LceLoadingSettings(R.layout.loading.some()).some()

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLceWrapper = LceWrapper(
                FrameLayout(this),
                LceSettings(loadingSettings, None)
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
                result.also { it.deferredK.unsafeRunAsync { } }
                        .also { it.observe(this) { lce -> rootLceWrapper.apply(lce) } }
            }
        }
    }
}
