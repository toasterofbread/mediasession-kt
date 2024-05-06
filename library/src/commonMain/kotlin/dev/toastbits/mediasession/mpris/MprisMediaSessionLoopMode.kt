package dev.toastbits.mediasession.mpris

import dev.toastbits.mediasession.MediaSessionLoopMode

fun MediaSessionLoopMode.toMprisLoopMode(): String =
    when (this) {
        MediaSessionLoopMode.NONE -> "None"
        MediaSessionLoopMode.ONE -> "Track"
        MediaSessionLoopMode.ALL -> "Playlist"
    }

fun String.fromMprisLoopMode(): MediaSessionLoopMode =
    when (this) {
        "None" -> MediaSessionLoopMode.NONE
        "Track" -> MediaSessionLoopMode.ONE
        "Playlist" -> MediaSessionLoopMode.ALL
        else -> throw NotImplementedError("Unknown loop status '$this'")
    }
