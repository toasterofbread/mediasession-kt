package dev.toastbits.mediasession

import kotlinx.cinterop.toKString
import kotlinx.cinterop.wcstr
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.pointed
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.ptr
import kotlinx.cinterop.CFunction

actual open class MediaSession: MediaSessionProperties {
    private var session_enabled: Boolean = false
    private var _art_url: String? = null
    
    actual val enabled: Boolean
        get() = session_enabled

    init {
        val result: Int = libsmtc.init()
        if (result != 0) {
            throw RuntimeException("Failed to initialise libsmtc ($result), is Windows.Media.Playback.MediaPlayer available?")
        }

        libsmtc.setCallbackData(StableRef.create(this).asCPointer())
        libsmtc.setMediaType(0)

        libsmtc.setOnNext(staticCFunction({ inst -> inst!!.asStableRef<MediaSession>().get().onNext?.invoke() }))
        libsmtc.setOnPrevious(staticCFunction({ inst -> inst!!.asStableRef<MediaSession>().get().onPrevious?.invoke() }))
        libsmtc.setOnPause(staticCFunction({ inst -> inst!!.asStableRef<MediaSession>().get().onPause?.invoke() }))
        libsmtc.setOnStop(staticCFunction({ inst -> inst!!.asStableRef<MediaSession>().get().onStop?.invoke() }))
        libsmtc.setOnPlay(staticCFunction({ inst -> inst!!.asStableRef<MediaSession>().get().onPlay?.invoke() }))

        libsmtc.setOnSeek(staticCFunction({ to_ms, inst -> inst!!.asStableRef<MediaSession>().get().onSetPosition?.invoke(to_ms) }))
        libsmtc.setOnRateChanged(staticCFunction({ rate, inst -> inst!!.asStableRef<MediaSession>().get().onSetRate?.invoke(rate.toFloat()) }))
        libsmtc.setOnLoopChanged(staticCFunction({ loop, inst -> inst!!.asStableRef<MediaSession>().get().onSetLoop?.invoke(loop.toLoopMode()) }))
        libsmtc.setOnShuffleChanged(staticCFunction({ shuffle, inst -> inst!!.asStableRef<MediaSession>().get().onSetShuffle?.invoke(if (shuffle == 0) false else true) }))
    }

    actual fun setEnabled(enabled: Boolean) {
        if (enabled == session_enabled) {
            return
        }

        libsmtc.setEnabled(if (enabled) 1 else 0)
        session_enabled = enabled

        if (enabled) {
            update()
        }
    }

    actual var onRaise: (() -> Unit)? = null
    actual var onQuit: (() -> Unit)? = null
    
    actual var onNext: (() -> Unit)? = null
        set(value) {
            field = value
            libsmtc.setNextEnabled(if (value != null) 1 else 0)
        }
    actual var onPrevious: (() -> Unit)? = null
        set(value) {
            field = value
            libsmtc.setPreviousEnabled(if (value != null) 1 else 0)
        }
    
    actual var onPause: (() -> Unit)? = null
    actual var onPlayPause: (() -> Unit)? = null
    actual var onStop: (() -> Unit)? = null
    actual var onPlay: (() -> Unit)? = null
    actual var onSeek: ((by_ms: Long) -> Unit)? = null
    actual var onSetPosition: ((to_ms: Long) -> Unit)? = null
    actual var onOpenUri: ((uri: String) -> Unit)? = null
    actual var onSetRate: ((rate: Float) -> Unit)? = null
    actual var onSetLoop: ((loop_mode: MediaSessionLoopMode) -> Unit)? = null
    actual var onSetShuffle: ((shuffle_mode: Boolean) -> Unit)? = null

    actual open fun getPositionMs(): Long = 0
    actual fun onPositionChanged() {
        libsmtc.setPosition(getPositionMs())
    }

    override val identity: String
        get() = ""
    override val desktop_entry: String?
        get() = null
    override val supported_uri_schemes: List<String>
        get() = emptyList()
    override val supported_mime_types: List<String>
        get() = emptyList()
    override val loop_status: MediaSessionLoopMode
        get() = libsmtc.getLoop().toLoopMode()
    override val shuffle: Boolean
        get() = libsmtc.getShuffle() == 1
    override val volume: Float
        get() = 0f
    override val rate: Float
        get() = libsmtc.getRate().toFloat()
    override val metadata: MediaSessionMetadata
        get() = MediaSessionMetadata(
            track_id = null,
            length_ms = null,
            art_url = _art_url,
            album = libsmtc.getMusicAlbumTitle()?.toKString(),
            album_artists = listOfNotNull(libsmtc.getMusicAlbumArtist()?.toKString()),
            artist = libsmtc.getMusicArtist()?.toKString(),
            lyrics = null,
            audio_bpm = null,
            auto_rating = null,
            comment = null,
            composer = null,
            content_created = null,
            disc_number = null,
            first_used = null,
            genres = (0U until libsmtc.getMusicGenresSize()).mapNotNull { i -> libsmtc.getMusicGenreAt(i)?.toKString() },
            last_used = null,
            lyricist = null,
            title = libsmtc.getMusicTitle()?.toKString(),
            track_number = libsmtc.getMusicTrack().toInt(),
            url = null,
            use_count = null,
            user_rating = null
        )
    override val playback_status: MediaSessionPlaybackStatus
        get() =
            when (libsmtc.getPlaybackState()) {
                0U -> MediaSessionPlaybackStatus.STOPPED
                1U -> MediaSessionPlaybackStatus.PAUSED
                2U -> MediaSessionPlaybackStatus.STOPPED
                3U -> MediaSessionPlaybackStatus.PLAYING
                4U -> MediaSessionPlaybackStatus.PAUSED
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

    override fun setLoopMode(loop_status: MediaSessionLoopMode) {
        libsmtc.setLoop(
            when (loop_status) {
                MediaSessionLoopMode.NONE -> 0U
                MediaSessionLoopMode.ONE -> 1U
                MediaSessionLoopMode.ALL -> 2U
            }
        )
        update()
    }
    override fun setShuffle(shuffle: Boolean) {
        libsmtc.setShuffle(if (shuffle) 1 else 0)
        update()
    }
    override fun setVolume(volume: Float) {
        // TODO
    }
    override fun setRate(rate: Float) {
        libsmtc.setRate(rate.toDouble())
        update()
    }
    override fun setPlaybackStatus(status: MediaSessionPlaybackStatus) {
        libsmtc.setPlaybackState(
            when (status) {
                MediaSessionPlaybackStatus.PLAYING -> 3U
                MediaSessionPlaybackStatus.PAUSED -> 1U
                MediaSessionPlaybackStatus.STOPPED -> 2U
            }
        )
        update()
    }
    override fun setMaximumRate(maximum_rate: Float) {}
    override fun setMinimumRate(minimum_rate: Float) {}

    override fun setMetadata(metadata: MediaSessionMetadata) {
        libsmtc.setMusicTitle((metadata.title ?: "").wcstr)
        libsmtc.setMusicArtist((metadata.artist ?: "").wcstr)
        libsmtc.setMusicAlbumTitle((metadata.album ?: "").wcstr)
        libsmtc.setMusicAlbumArtist((metadata.album_artists?.firstOrNull() ?: "").wcstr)
        libsmtc.setMusicTrack(metadata.track_number?.toUInt() ?: 0U)

        libsmtc.setThumbnail((metadata.art_url ?: "").replace("/", "\\").wcstr, 1)
        _art_url = metadata.art_url

        libsmtc.clearMusicGenres()
        for (genre in metadata.genres ?: emptyList()) {
            libsmtc.addMusicGenre(genre.wcstr)
        }

        val length: Long = metadata.length_ms ?: 0
        libsmtc.setTimelineProperties(0, length, 0, length)

        update()
    }

    private fun update() {
        if (enabled) {
            libsmtc.update()
        }
    }
}

private fun UInt.toLoopMode(): MediaSessionLoopMode =
    when (this) {
        0U -> MediaSessionLoopMode.NONE
        1U -> MediaSessionLoopMode.ONE
        2U -> MediaSessionLoopMode.ALL
        else -> throw NotImplementedError(libsmtc.getLoop().toString())
    }
