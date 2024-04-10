pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        val kotlin_version: String = extra["kotlin.version"] as String
        kotlin("multiplatform").version(kotlin_version)

        val dokka_version: String = extra["dokka.version"] as String
        id("org.jetbrains.dokka").version(dokka_version)
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "mediasession-kt"
include(":library")
include(":sample")
