plugins {
    kotlin("multiplatform")
    application
}

kotlin {
    jvm().apply {
        withJava()
    }

    val native_targets = listOf(linuxX64(), linuxArm64(), mingwX64())

    for (target in native_targets) {
        target.binaries {
            executable {
                entryPoint = "dev.toastbits.sample.main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":library"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            }
        }

        val jvmMain by getting
    }
}
