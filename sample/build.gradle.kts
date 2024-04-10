plugins {
    kotlin("multiplatform")
    application
}

kotlin {
    jvm().apply {
        withJava()
    }

    linuxX64().apply {
        binaries {
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

application {
    mainClass.set("dev.toastbits.sample.SampleKt")
}
