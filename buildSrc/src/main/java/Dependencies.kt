object Config {
    const val min_sdk = 21
    const val target_sdk = 28
    const val compile_sdk = 28
    const val version_code = 1
    const val version_name = "0.1"
    const val app_id = "com.antslife.mobile"
}

object Versions {
    //** KOTLIN **//
    const val kotlin = "1.2.51"
    const val androidKtx = "1.0.0-beta01"
    //** SUPPORT **//
    const val support_lib = "1.0.0-beta01"
    //** COROUTINES **//
    const val coroutines = "0.23.4"
    //** ANDROID ARCH **//
    const val arch = "2.0.0-alpha1"
    //** LAYOUT **//
    const val constraint_layout = "1.1.0"
    //** NETWORKING **//
    const val retrofit = "2.4.0"
    const val retrofit_ok_http_interceptor = "3.8.1"
    const val retrofit_coroutines_adapter = "1.0.0"
    //** RX **//
    const val rx = "2.1.16"
    const val rxAndroid = "2.0.2"
    //** ARROW **//
    const val arrow = "0.7.2"
    //** PARSING **//
    const val moshi = "1.6.0"
    //** DAGGER **//
    const val dagger = "2.16"
    //** RECYCLER DROID **//
    const val recycler_droid = "bdc84fe"
    //** PAYMENT **//
    const val stripe = "6.1.2"
}

object Deps {

    //** KOTLIN **//
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    const val kotlin_gradle_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val core_ktx = "androidx.core:core-ktx:${Versions.androidKtx}"

    //** SUPPORT **//
    const val support_v4 = "androidx.legacy:legacy-support-v4:${Versions.support_lib}"
    const val app_compat_v7 = "androidx.appcompat:appcompat:${Versions.support_lib}"
    const val card_view = "androidx.cardview:cardview:${Versions.support_lib}"
    const val design = "com.google.android.material:material:${Versions.support_lib}"
    const val coordinator = "androidx.coordinatorlayout:coordinatorlayout:${Versions.support_lib}"

    //** COROUTINES **//
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"

    //** ANDROID ARCH **//
    const val android_arch_runtime = "androidx.lifecycle:lifecycle-runtime:${Versions.arch}"
    const val android_arch_extensions = "androidx.lifecycle:lifecycle-extensions:${Versions.arch}"

    //** LAYOUT **//
    const val constraint_layout = "androidx.constraintlayout:constraintlayout:${Versions.constraint_layout}"

    //** NETWORKING **//
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retorfit_moshi_converter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
    const val retrofit_rx_adapter = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
    const val retorfit_ok_http_interceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.retrofit_ok_http_interceptor}"
    const val retrofit_coroutines_adapter = "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-experimental-adapter:${Versions.retrofit_coroutines_adapter}"

    //** RX **//
    const val rx = "io.reactivex.rxjava2:rxjava:${Versions.rx}"
    const val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroid}"

    //** ARROW **//
    const val arrow_core = "io.arrow-kt:arrow-core:${Versions.arrow}"
    const val arrow_sintax = "io.arrow-kt:arrow-syntax:${Versions.arrow}"
    const val arrow_typeclasses = "io.arrow-kt:arrow-typeclasses:${Versions.arrow}"
    const val arrow_data = "io.arrow-kt:arrow-data:${Versions.arrow}"
    const val arrow_instances_core = "io.arrow-kt:arrow-instances-core:${Versions.arrow}"
    const val arrow_instances_data = "io.arrow-kt:arrow-instances-data:${Versions.arrow}"
    const val arrow_annotations = "io.arrow-kt:arrow-annotations-processor:${Versions.arrow}"

    //** PARSING **//
    const val moshi = "com.squareup.moshi:moshi-kotlin:${Versions.moshi}"

    //** DAGGER **//
    const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    const val daggerRuntime = "com.google.dagger:dagger:${Versions.dagger}"
    const val daggerAndroid = "com.google.dagger:dagger-android:${Versions.dagger}"
    const val daggerAndroidSupport = "com.google.dagger:dagger-android-support:${Versions.dagger}"
    const val daggerProcessor = "com.google.dagger:dagger-android-processor:${Versions.dagger}"

    //** RECYCLER DROID **//
    const val recycler_droid = "com.github.giacomoParisi:RecyclerDroid:${Versions.recycler_droid}"

    //** PAYMENT **//
    const val stripe = "com.stripe:stripe-android:${Versions.stripe}"
}