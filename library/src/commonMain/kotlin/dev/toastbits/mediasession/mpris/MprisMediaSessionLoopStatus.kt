package dev.toastbits.mediasession.mpris

import dev.toastbits.mediasession.MediaSessionLoopStatus

fun MediaSessionLoopStatus.toMprisLoopStatus(): String =
    when (this) {
        MediaSessionLoopStatus.NONE -> "None"
        MediaSessionLoopStatus.ONE -> "Track"
        MediaSessionLoopStatus.ALL -> "Playlist"
    }

fun String.fromMprisLoopStatus(): MediaSessionLoopStatus =
    when (this) {
        "None" -> MediaSessionLoopStatus.NONE
        "Track" -> MediaSessionLoopStatus.ONE
        "Playlist" -> MediaSessionLoopStatus.ALL
        else -> throw NotImplementedError("Unknown loop status '$this'")
    }
