plugins {
    kotlin("multiplatform")
    id("module.publication")
    id("org.jetbrains.dokka")
    kotlin("plugin.serialization") version "1.9.22"
}

kotlin {
    targetHierarchy.default()

    jvm()

    linuxX64().apply {
        compilations.getByName("main") {
            cinterops {
                val libdbus by creating
            }
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
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
