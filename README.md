# mediasession-kt

A Kotlin Multiplatform library for emitting a system media session.

Native Windows implementation adapted from [JavaMediaTransportControls](https://github.com/Selemba1000/JavaMediaTransportControls) by [Selemba1000](https://github.com/Selemba1000).

## Setup

mediasession-kt currently supports the following Kotlin platforms:
- JVM 
    - Linux (tested on x86_64 but should work on other architectures)
- Native
    - Linux x86_64
    - Windows x86_64

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

- JVM: `implementation("dev.toastbits.mediasession:library-jvm:<version>")`
- Native (Linux x86_64): `implementation("dev.toastbits.mediasession:library-linuxx64:<version>")`
- Native (Windows x86_64): `implementation("dev.toastbits.mediasession:library-mingwx64:<version>")`

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
