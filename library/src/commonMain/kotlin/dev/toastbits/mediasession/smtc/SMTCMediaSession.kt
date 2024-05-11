package dev.toastbits.mediasession.smtc

import dev.toastbits.mediasession.MediaSessionProperties
import dev.toastbits.mediasession.MediaSession
import dev.toastbits.mediasession.MediaSessionLoopMode
import dev.toastbits.mediasession.MediaSessionMetadata
import dev.toastbits.mediasession.MediaSessionPlaybackStatus

open class SMTCMediaSession(private val smtc: SMTCAdapter): MediaSessionProperties, MediaSession {
    private var session_enabled: Boolean = false
    private var _art_url: String? = null

    override val enabled: Boolean
        get() = session_enabled

    init {
        val result: Int = 
            try {
                smtc.init()
            }
            catch (e: Throwable) {
                throw RuntimeException("Failed to initialise smtc", e)
            }
        
        if (result != 0) {
            throw RuntimeException("Failed to initialise smtc ($result), is Windows.Media.Playback.MediaPlayer available?")
        }
    }

    override fun setEnabled(enabled: Boolean) {
        if (enabled == session_enabled) {
            return
        }

        smtc.setEnabled(enabled)
        session_enabled = enabled

        if (enabled) {
            update()
        }
    }

    override var onRaise: (() -> Unit)? = null
    override var onQuit: (() -> Unit)? = null

    override var onNext: (() -> Unit)? = null
        set(value) {
            field = value
            smtc.onNext = value
            smtc.setNextEnabled(value != null)
        }
    override var onPrevious: (() -> Unit)? = null
        set(value) {
            field = value
            smtc.onPrevious = value
            smtc.setPreviousEnabled(value != null)
        }

    override var onPause: (() -> Unit)? = null
        set(value) {
            field = value
            smtc.onPause = value
        }
    override var onStop: (() -> Unit)? = null
        set(value) {
            field = value
            smtc.onStop = value
        }
    override var onPlay: (() -> Unit)? = null
        set(value) {
            field = value
            smtc.onPlay = value
        }
    override var onSetPosition: ((to_ms: Long) -> Unit)? = null
        set(value) {
            field = value
            smtc.onSetPosition = value
        }
    override var onSetRate: ((rate: Float) -> Unit)? = null
        set(value) {
            field = value
            smtc.onSetRate = value
        }
    override var onSetLoop: ((loop_mode: MediaSessionLoopMode) -> Unit)? = null
        set(value) {
            field = value

            if (value == null) {
                smtc.onSetLoop = null
            }
            else {
                smtc.onSetLoop = { loop_mode ->
                    value.invoke(loop_mode.toLoopMode())
                }
            }
        }
    override var onSetShuffle: ((shuffle_mode: Boolean) -> Unit)? = null
        set(value) {
            field = value
            smtc.onSetShuffle = value
        }
    
    override var onPlayPause: (() -> Unit)? = null
    override var onSeek: ((by_ms: Long) -> Unit)? = null
    override var onOpenUri: ((uri: String) -> Unit)? = null

    override fun onPositionChanged() {
        smtc.setPosition(getPositionMs())
    }

    override val identity: String
        get() = ""
    override val desktop_entry: String?
        get() = null
    override val supported_uri_schemes: List<String>
        get() = emptyList()
    override val supported_mime_types: List<String>
        get() = emptyList()
    override val loop_mode: MediaSessionLoopMode
        get() = smtc.getLoop().toLoopMode()
    override val shuffle: Boolean
        get() = smtc.getShuffle()
    override val volume: Float
        get() = 0f
    override val rate: Float
        get() = smtc.getRate().toFloat()
    override val metadata: MediaSessionMetadata
        get() = MediaSessionMetadata(
            track_id = null,
            length_ms = null,
            art_url = _art_url,
            album = smtc.getMusicAlbumTitle(),
            album_artists = listOfNotNull(smtc.getMusicAlbumArtist()),
            artist = smtc.getMusicArtist(),
            lyrics = null,
            audio_bpm = null,
            auto_rating = null,
            comment = null,
            composer = null,
            content_created = null,
            disc_number = null,
            first_used = null,
            genres = (0 until smtc.getMusicGenresSize()).mapNotNull { i -> smtc.getMusicGenreAt(i) },
            last_used = null,
            lyricist = null,
            title = smtc.getMusicTitle(),
            track_number = smtc.getMusicTrack().toInt(),
            url = null,
            use_count = null,
            user_rating = null
        )
    override val playback_status: MediaSessionPlaybackStatus
        get() =
            when (smtc.getPlaybackState()) {
                0 -> MediaSessionPlaybackStatus.STOPPED
                1 -> MediaSessionPlaybackStatus.PAUSED
                2 -> MediaSessionPlaybackStatus.STOPPED
                3 -> MediaSessionPlaybackStatus.PLAYING
                4 -> MediaSessionPlaybackStatus.PAUSED
                else -> MediaSessionPlaybackStatus.STOPPED
            }
    override val maximum_rate: Float
        get() = 1f
    override val minimum_rate: Float
        get() = 0f

    override fun setIdentity(identity: String) {}

    override fun setDesktopEntry(desktop_entry: String?) {}

    override fun setSupportedUriSchemes(supported_uri_schemes: List<String>) {}

    override fun setSupportedMimeTypes(supported_mime_types: List<String>) {}

    override fun setLoopMode(loop_mode: MediaSessionLoopMode) {
        smtc.setLoop(loop_mode.toInt())
        update()
    }
    override fun setShuffle(shuffle: Boolean) {
        smtc.setShuffle(shuffle)
        update()
    }
    override fun setVolume(volume: Float) {
        // TODO
    }
    override fun setRate(rate: Float) {
        smtc.setRate(rate.toDouble())
        update()
    }
    override fun setPlaybackStatus(status: MediaSessionPlaybackStatus) {
        smtc.setPlaybackState(
            when (status) {
                MediaSessionPlaybackStatus.PLAYING -> 3
                MediaSessionPlaybackStatus.PAUSED -> 1
                MediaSessionPlaybackStatus.STOPPED -> 2
            }
        )
        update()
    }
    override fun setMaximumRate(maximum_rate: Float) {}
    override fun setMinimumRate(minimum_rate: Float) {}

    override fun setMetadata(metadata: MediaSessionMetadata) {
        smtc.setMusicTitle(metadata.title ?: "")
        smtc.setMusicArtist(metadata.artist ?: "")
        smtc.setMusicAlbumTitle(metadata.album ?: "")
        smtc.setMusicAlbumArtist(metadata.album_artists?.firstOrNull() ?: "")
        smtc.setMusicTrack(metadata.track_number ?: 0)

        smtc.setThumbnail((metadata.art_url ?: "").replace("/", "\\"), true)
        _art_url = metadata.art_url

        smtc.clearMusicGenres()
        for (genre in metadata.genres ?: emptyList()) {
            smtc.addMusicGenre(genre)
        }

        val length: Long = metadata.length_ms ?: 0
        smtc.setTimelineProperties(0, length, 0, length)

        update()
    }

    private fun update() {
        if (enabled) {
            smtc.update()
        }
    }
}

private fun Int.toLoopMode(): MediaSessionLoopMode =
    when (this) {
        0 -> MediaSessionLoopMode.NONE
        1 -> MediaSessionLoopMode.ONE
        2 -> MediaSessionLoopMode.ALL
        else -> throw NotImplementedError(this.toString())
    }


private fun MediaSessionLoopMode.toInt(): Int =
    when (this) {
        MediaSessionLoopMode.NONE -> 0
        MediaSessionLoopMode.ONE -> 1
        MediaSessionLoopMode.ALL -> 2
    }
