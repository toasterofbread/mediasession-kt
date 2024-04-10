package dev.toastbits.mediasession.mpris

import dev.toastbits.mediasession.MediaSessionPlaybackStatus

fun MediaSessionPlaybackStatus.toMprisPlaybackStatus(): String =
    when (this) {
        MediaSessionPlaybackStatus.PLAYING -> "Playing"
        MediaSessionPlaybackStatus.PAUSED -> "Paused"
        MediaSessionPlaybackStatus.STOPPED -> "Stopped"
    }

fun String.fromMprisPlaybackStatus(): MediaSessionPlaybackStatus =
    when (this) {
        "Playing" -> MediaSessionPlaybackStatus.PLAYING
        "Paused" -> MediaSessionPlaybackStatus.PAUSED
        "Stopped" -> MediaSessionPlaybackStatus.STOPPED
        else -> throw NotImplementedError("Unknown playback status '$this'")
    }
