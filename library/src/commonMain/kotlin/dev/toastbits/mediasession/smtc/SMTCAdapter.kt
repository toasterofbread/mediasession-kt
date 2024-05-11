package dev.toastbits.mediasession.smtc

import dev.toastbits.mediasession.MediaSessionLoopMode

interface SMTCAdapter {
    fun init(): Int

    var onPause: (() -> Unit)?
    var onStop: (() -> Unit)?
    var onPlay: (() -> Unit)?
    var onNext: (() -> Unit)?
    var onPrevious: (() -> Unit)?
    var onSetPosition: ((to_ms: Long) -> Unit)?
    var onSetRate: ((rate: Float) -> Unit)?
    var onSetLoop: ((loop_mode: Int) -> Unit)?
    var onSetShuffle: ((shuffle_mode: Boolean) -> Unit)?

    fun revokeCallbacks()

    fun getEnabled(): Boolean
    fun setEnabled(enabled: Boolean)

    fun getNextEnabled(): Boolean
    fun setNextEnabled(enabled: Boolean)

    fun getPreviousEnabled(): Boolean
    fun setPreviousEnabled(enabled: Boolean)

    fun getPlayEnabled(): Boolean
    fun setPlayEnabled(enabled: Boolean)

    fun getPauseEnabled(): Boolean
    fun setPauseEnabled(enabled: Boolean)

    fun getStopEnabled(): Boolean
    fun setStopEnabled(enabled: Boolean)

    fun getRate(): Double
    fun setRate(rate: Double)

    fun getShuffle(): Boolean
    fun setShuffle(shuffle: Boolean)

    fun getLoop(): Int
    fun setLoop(loop: Int)

    fun getPlaybackState(): Int
    fun setPlaybackState(state: Int)

    fun setTimelineProperties(start: Long, end: Long, seek_start: Long, seek_end: Long)
    fun setPosition(position: Long)

    fun update()
    fun reset()

    fun getMediaType(): Int
    fun setMediaType(type: Int)

    fun thumbnailLoaded(): Boolean
    fun setThumbnail(path: String, update_when_loaded: Boolean)

    fun getMusicTitle(): String
    fun setMusicTitle(title: String)

    fun getMusicArtist(): String
    fun setMusicArtist(artist: String)

    fun getMusicAlbumTitle(): String
    fun setMusicAlbumTitle(title: String)

    fun getMusicAlbumArtist(): String
    fun setMusicAlbumArtist(artist: String)

    fun getMusicGenresSize(): Int
    fun getMusicGenreAt(index: Int): String
    fun addMusicGenre(genre: String)
    fun clearMusicGenres()

    fun getMusicAlbumTrackCount(): Int
    fun setMusicAlbumTrackCount(count: Int)

    fun getMusicTrack(): Int
    fun setMusicTrack(track: Int)
}
