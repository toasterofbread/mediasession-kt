package dev.toastbits.sample

import dev.toastbits.mediasession.MediaSession
import dev.toastbits.mediasession.MediaSessionMetadata
import dev.toastbits.mediasession.MediaSessionPlaybackStatus
import dev.toastbits.mediasession.MediaSessionLoopStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.TimeSource

fun main() {
    var time = TimeSource.Monotonic.markNow()

    val session: MediaSession =
        object : MediaSession() {
            override fun getPositionMs(): Long = time.elapsedNow().inWholeMilliseconds
        }

    session.onRaise = {
        println("onRaise called")
    }
    session.onQuit = {
        println("onQuit called")
    }
    session.onSetFullscreen = {
        println("onSetFullscreen called")
    }
    session.onNext = {
        println("onNext called")
    }
    session.onPrevious = {
        println("onPrevious called")
    }
    session.onPause = {
        println("onPause called")
    }
    session.onPlayPause = {
        println("onPlayPause called")
    }
    session.onStop = {
        println("onStop called")
    }
    session.onPlay = {
        println("onPlay called")
    }
    session.onSeek = { by_ms ->
        println("onSeek $by_ms called")
    }
    session.onSetPosition = { to_ms ->
        println("onSetPosition $to_ms called")
    }
    session.onOpenUri = { uri ->
        println("onOpenUri $uri called")
    }
    session.onSetRate = { rate ->
        println("onSetRate $rate called")
    }

    session.setIdentity("mediasessionkt.sample")
    session.setFullscreen(false)
    session.setDesktopEntry("mediasession")
    session.setSupportedUriSchemes(listOf("file", "http"))
    session.setSupportedMimeTypes(listOf("audio/mpeg", "application/ogg"))
    session.setLoopStatus(MediaSessionLoopStatus.ONE)
    session.setShuffle(true)
    session.setVolume(0.5f)
    session.setPlaybackStatus(MediaSessionPlaybackStatus.PAUSED)
    session.setRate(1f)
    session.setMaximumRate(1f)
    session.setMinimumRate(1f)

    session.setMetadata(
        MediaSessionMetadata(
            track_id = "/track/id",
            length_ms = 5000,
            art_url = "https://sample.com/art.png",
            album = "Album",
            album_artists = listOf("Artist1", "Artist2", "Artist3"),
            artist = "Artist",
            lyrics = "Lyrics",
            audio_bpm = 60,
            auto_rating = 1f,
            comment = listOf("Comment"),
            composer = listOf("Composer"),
            content_created = "2024-04-09T16:16+00:00",
            disc_number = 0,
            first_used = "2024-04-09T16:16+00:00",
            genre = listOf("Genre"),
            last_used = "2024-04-09T16:16+00:00",
            lyricist = listOf("Lyricist"),
            title = "Title",
            track_number = 0,
            url = "https://github.com/toasterofbread/mediasession-kt",
            use_count = 0,
            user_rating = 1f
        )
    )

    session.setPlaybackStatus(MediaSessionPlaybackStatus.PLAYING)

    session.setEnabled(true)

    println("Running...")
    runBlocking {
        while (true) {
            time = TimeSource.Monotonic.markNow()
            delay(1000)
            session.onPositionChanged()
        }
    }
}
