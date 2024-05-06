import com.vanniktech.maven.publish.SonatypeHost
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.JavadocJar

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
    kotlin("plugin.serialization") version "1.9.22"
}

allprojects {
    group = "dev.toastbits.mediasession"
    version = "0.0.2"
}

kotlin {
    jvm()
    linuxX64().apply {
        compilations.getByName("main") {
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
            }
        }
    }
}

mavenPublishing {
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
                name.set("GPL-3.0")
                url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
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

tasks.configureEach {
    notCompatibleWithConfigurationCache("https://github.com/Kotlin/dokka/issues/2231")
}
