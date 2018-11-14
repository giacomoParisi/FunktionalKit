package com.giacomoparisi.lce.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import arrow.core.None
import arrow.core.some
import arrow.syntax.function.pipe
import com.giacomoparisi.kotlin.functional.extensions.coroutines.postOnUI
import com.giacomoparisi.lce.android.LceLoadingSettings
import com.giacomoparisi.lce.android.LceSettings
import com.giacomoparisi.lce.android.LceWrapper
import com.giacomoparisi.lce.live.data.liveDataLce
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



        this.root_loading_success.setOnClickListener {
            liveDataLce {
                delay(5000)
                "Completed".some()
            }.pipe { liveData ->
                postOnUI {
                    liveData.observe(
                            this@MainActivity,
                            Observer { lce -> rootLceWrapper.apply(lce) }
                    )
                }
            }
        }
    }
}
