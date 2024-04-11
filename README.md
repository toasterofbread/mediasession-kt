# mediasession-kt

A Kotlin Multiplatform library for emitting a system media session.

## Setup

mediasession-kt currently supports the following Kotlin platforms:
- Linux JVM (Windows support planned)
- Native (Linux x86_64)

#### Gradle:

1. Add the Maven Central repository to your dependency resolution configuration

```
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

2. To your dependencies, add the line corresponding to the target platform (replace `<version>` with the desired mediasession-kt [version](https://github.com/toasterofbread/mediasession-kt/tags))

- JVM (Kotlin/JVM): `implementation("dev.toastbits.mediasession:library-jvm:<version>")`
- Linux x86_64 (Kotlin/Native): `implementation("dev.toastbits.mediasession:library-linuxx64:<version>")`

### Example usage

```
var time = TimeSource.Monotonic.markNow()

// Initialise the media session
val session: MediaSession =
    object : MediaSession() {
        override fun getPositionMs(): Long = time.elapsedNow().inWholeMilliseconds
    }

// Set callbacks
session.onPlay = {
    println("onPlay called")
}
session.onSeek = { by_ms: Long ->
    println("onSeek called")
}

// Set properties
session.setIdentity("mediasession.sample")
session.setPlaybackStatus(MediaSessionPlaybackStatus.PAUSED)
session.setMetadata(
    MediaSessionMetadata(
        title = "Title",
    )
)

// Enable session (asynchronous)
session.setEnabled(true)
```

#### See the [sample application](sample/src/commonMain/kotlin/dev/toastbits/sample/Sample.kt) for a more detailed example
