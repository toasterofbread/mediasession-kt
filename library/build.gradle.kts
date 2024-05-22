import com.vanniktech.maven.publish.SonatypeHost
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.JavadocJar
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
    kotlin("plugin.serialization") version "1.9.22"
}

kotlin {
    jvm()

    val linux_targets: List<KotlinNativeTarget> = listOf(linuxX64(), linuxArm64())
    for (target in linux_targets) {
        target.compilations.getByName("main") {
            cinterops {
                val libdbus by creating
            }
        }
    }

    mingwX64().apply {
        compilations.getByName("main") {
            cinterops {
                val libsmtc by creating {
                    includeDirs(file("src/nativeInterop/mingw-x86_64/include"))
                    extraOpts("-libraryPath", file("src/nativeInterop/mingw-x86_64/thirdparty/libsmtc/build").absolutePath)
                }
            }
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
                enableLanguageFeature("ExpectActualClasses")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("com.github.hypfvieh:dbus-java-core:5.0.0")
                implementation("com.github.hypfvieh:dbus-java-transport-jnr-unixsocket:5.0.0")
                implementation("net.java.dev.jna:jna:5.14.0")
            }
        }
    }
}

tasks.register<Copy>("copyX86Dll") {
    from("src/nativeInterop/mingw-x86_64/thirdparty/libsmtc/build/libSMTCAdapter.dll")
    into("src/jvmMain/resources/win32-x86")
}

tasks.register<Copy>("copyX64Dll") {
    from("src/nativeInterop/mingw-x86_64/thirdparty/libsmtc/build/libSMTCAdapter.dll")
    into("src/jvmMain/resources/win32-x86-64")
}

tasks.getByName("jvmProcessResources") {
    dependsOn("copyX86Dll")
    dependsOn("copyX64Dll")
}

mavenPublishing {
    coordinates("dev.toastbits", "mediasession", "0.1.1")

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    configure(KotlinMultiplatform(
        javadocJar = JavadocJar.Dokka("dokkaHtml"),
        sourcesJar = true
    ))

    pom {
        name.set("mediasession-kt")
        description.set("A Kotlin Multiplatform library for emitting a system media session")
        url.set("https://github.com/toasterofbread/mediasession-kt")
        inceptionYear.set("2024")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }
        developers {
            developer {
                id.set("toasterofbread")
                name.set("Talo Halton")
                email.set("talohalton@gmail.com")
                url.set("https://github.com/toasterofbread")
            }
        }
        scm {
            connection.set("https://github.com/toasterofbread/mediasession-kt.git")
            url.set("https://github.com/toasterofbread/mediasession-kt")
        }
        issueManagement {
            system.set("Github")
            url.set("https://github.com/toasterofbread/mediasession-kt/issues")
        }
    }
}
