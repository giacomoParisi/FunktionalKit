import kotlin.String

/**
 * Find which updates are available by running
 *     `$ ./gradlew syncLibs`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val appcompat: String = "1.0.1" 

    const val constraintlayout: String = "1.1.3" 

    const val legacy_support_v4: String = "1.0.0" 

    const val androidx_lifecycle: String = "2.0.0" 

    const val espresso_core: String = "3.1.0" 

    const val androidx_test_runner: String = "1.1.0" 

    const val jetifier_processor: String = "1.0.0-beta02" // exceed the version found: 0.0.1

    const val aapt2: String = "3.2.1-4818971" 

    const val com_android_tools_build_gradle: String = "3.2.1" 

    const val lint_gradle: String = "26.2.1" 

    const val android_maven_gradle_plugin: String = "2.1" 

    const val com_github_giacomoparisi_kotlin_functional_extensions: String =
            "a23263baf2" // No update information. Is this dependency available on jcenter or mavenCentral?

    const val com_google_dagger: String = "2.19" 

    const val retrofit: String = "2.4.0" 

    const val io_arrow_kt: String =
            "0.8.0" // No update information. Is this dependency available on jcenter or mavenCentral?

    const val rxandroid: String = "2.1.0" 

    const val rxjava: String = "2.2.3" 

    const val jmfayard_github_io_gradle_kotlin_dsl_libs_gradle_plugin: String = "0.2.6" 

    const val junit: String = "4.12" 

    const val org_jetbrains_kotlin: String = "1.3.0" 

    const val kotlinx_coroutines_android: String = "1.0.0" 

    /**
     *
     *   To update Gradle, edit the wrapper file at path:
     *      ./gradle/wrapper/gradle-wrapper.properties
     */
    object Gradle {
        const val runningVersion: String = "4.10.2"

        const val currentVersion: String = "4.10.2"

        const val nightlyVersion: String = "5.1-20181106000028+0000"

        const val releaseCandidate: String = "5.0-rc-1"
    }
}
