# mediasession-kt

A Kotlin Multiplatform library for emitting a system media session.

Windows implementation adapted from [JavaMediaTransportControls](https://github.com/Selemba1000/JavaMediaTransportControls) by [Selemba1000](https://github.com/Selemba1000).

## Setup

mediasession-kt currently supports the following Kotlin platforms:
- JVM (tested on x86_64 but may work on other architectures)
    - Linux
    - Windows
- Native
    - Linux x86_64
    - Linux ARM64
    - Windows x86_64

#### Gradle:

1. Add the Maven Central repository to your dependency resolution configuration

```
repositories {
    mavenCentral()
}
```

2. Add the following line to your dependencies (replace `<version>` with the desired mediasession-kt [version](https://github.com/toasterofbread/mediasession-kt/tags))

```
implementation("dev.toastbits:mediasession:<version>")
```

### Example usage

```
var time = TimeSource.Monotonic.markNow()

// Initialise the media session
val session: MediaSession =
    MediaSession.create(
        getPositionMs = { time.elapsedNow().inWholeMilliseconds }
    )!!

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
