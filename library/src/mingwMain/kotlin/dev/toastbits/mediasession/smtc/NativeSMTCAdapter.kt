package dev.toastbits.mediasession.smtc

import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.wcstr
import kotlinx.cinterop.toKString

class NativeSMTCAdapter: SMTCAdapter {
    override fun init(): Int {
        val result: Int = libsmtc.init()
        if (result != 0) {
            return result
        }

        libsmtc.setCallbackData(StableRef.create(this).asCPointer())
        libsmtc.setMediaType(0)

        libsmtc.setOnNext(staticCFunction({ inst -> inst!!.asStableRef<NativeSMTCAdapter>().get().onNext?.invoke() }))
        libsmtc.setOnPrevious(staticCFunction({ inst -> inst!!.asStableRef<NativeSMTCAdapter>().get().onPrevious?.invoke() }))
        libsmtc.setOnPause(staticCFunction({ inst -> inst!!.asStableRef<NativeSMTCAdapter>().get().onPause?.invoke() }))
        libsmtc.setOnStop(staticCFunction({ inst -> inst!!.asStableRef<NativeSMTCAdapter>().get().onStop?.invoke() }))
        libsmtc.setOnPlay(staticCFunction({ inst -> inst!!.asStableRef<NativeSMTCAdapter>().get().onPlay?.invoke() }))

        libsmtc.setOnSeek(staticCFunction({ to_ms, inst -> inst!!.asStableRef<NativeSMTCAdapter>().get().onSetPosition?.invoke(to_ms) }))
        libsmtc.setOnRateChanged(staticCFunction({ rate, inst -> inst!!.asStableRef<NativeSMTCAdapter>().get().onSetRate?.invoke(rate.toFloat()) }))
        libsmtc.setOnLoopChanged(staticCFunction({ loop, inst -> inst!!.asStableRef<NativeSMTCAdapter>().get().onSetLoop?.invoke(loop.toInt()) }))
        libsmtc.setOnShuffleChanged(staticCFunction({ shuffle, inst -> inst!!.asStableRef<NativeSMTCAdapter>().get().onSetShuffle?.invoke(if (shuffle == 0) false else true) }))

        return 0
    }

    override var onPause: (() -> Unit)? = null
    override var onStop: (() -> Unit)? = null
    override var onPlay: (() -> Unit)? = null
    override var onSetPosition: ((to_ms: Long) -> Unit)? = null
    override var onSetRate: ((rate: Float) -> Unit)? = null
    override var onSetLoop: ((loop_mode: Int) -> Unit)? = null
    override var onSetShuffle: ((shuffle_mode: Boolean) -> Unit)? = null

    override var onNext: (() -> Unit)? = null
    override var onPrevious: (() -> Unit)? = null

    override fun revokeCallbacks() = libsmtc.revokeCallbacks()

    override fun getEnabled(): Boolean = libsmtc.getEnabled().toBoolean()
    override fun setEnabled(enabled: Boolean) = libsmtc.setEnabled(if (enabled) 1 else 0)

    override fun getNextEnabled(): Boolean = libsmtc.getNextEnabled().toBoolean()
    override fun setNextEnabled(enabled: Boolean) = libsmtc.setNextEnabled(if (enabled) 1 else 0)

    override fun getPreviousEnabled(): Boolean = libsmtc.getPreviousEnabled().toBoolean()
    override fun setPreviousEnabled(enabled: Boolean) = libsmtc.setPreviousEnabled(if (enabled) 1 else 0)

    override fun getPlayEnabled(): Boolean = libsmtc.getPlayEnabled().toBoolean()
    override fun setPlayEnabled(enabled: Boolean) = libsmtc.setPlayEnabled(if (enabled) 1 else 0)

    override fun getPauseEnabled(): Boolean = libsmtc.getPauseEnabled().toBoolean()
    override fun setPauseEnabled(enabled: Boolean) = libsmtc.setPauseEnabled(if (enabled) 1 else 0)

    override fun getStopEnabled(): Boolean = libsmtc.getStopEnabled().toBoolean()
    override fun setStopEnabled(enabled: Boolean) = libsmtc.setStopEnabled(if (enabled) 1 else 0)

    override fun getRate(): Double = libsmtc.getRate()
    override fun setRate(rate: Double) = libsmtc.setRate(rate)

    override fun getShuffle(): Boolean = libsmtc.getShuffle().toBoolean()
    override fun setShuffle(shuffle: Boolean) = libsmtc.setShuffle(shuffle.toInt())

    override fun getLoop(): Int = libsmtc.getLoop().toInt()
    override fun setLoop(loop: Int) = libsmtc.setLoop(loop.toUInt())

    override fun getPlaybackState(): Int = libsmtc.getPlaybackState().toInt()
    override fun setPlaybackState(state: Int) = libsmtc.setPlaybackState(state.toUInt())

    override fun setTimelineProperties(start: Long, end: Long, seek_start: Long, seek_end: Long) = libsmtc.setTimelineProperties(start, end, seek_start, seek_end)
    override fun setPosition(position: Long) = libsmtc.setPosition(position)

    override fun update() = libsmtc.update()
    override fun reset() = libsmtc.reset()

    override fun getMediaType(): Int = libsmtc.getMediaType()
    override fun setMediaType(type: Int) = libsmtc.setMediaType(type)

    override fun thumbnailLoaded(): Boolean = libsmtc.thumbnailLoaded().toBoolean()
    override fun setThumbnail(path: String, update_when_loaded: Boolean) = libsmtc.setThumbnail(path.wcstr, update_when_loaded.toInt())

    override fun getMusicTitle(): String = libsmtc.getMusicTitle()!!.toKString()
    override fun setMusicTitle(title: String) = libsmtc.setMusicTitle(title.wcstr)

    override fun getMusicArtist(): String = libsmtc.getMusicArtist()!!.toKString()
    override fun setMusicArtist(artist: String) = libsmtc.setMusicArtist(artist.wcstr)

    override fun getMusicAlbumTitle(): String = libsmtc.getMusicAlbumTitle()!!.toKString()
    override fun setMusicAlbumTitle(title: String) = libsmtc.setMusicAlbumTitle(title.wcstr)

    override fun getMusicAlbumArtist(): String = libsmtc.getMusicAlbumArtist()!!.toKString()
    override fun setMusicAlbumArtist(artist: String) = libsmtc.setMusicAlbumArtist(artist.wcstr)

    override fun getMusicGenresSize(): Int = libsmtc.getMusicGenresSize().toInt()
    override fun getMusicGenreAt(index: Int): String = libsmtc.getMusicGenreAt(index.toUInt())!!.toKString()
    override fun addMusicGenre(genre: String) = libsmtc.addMusicGenre(genre.wcstr)
    override fun clearMusicGenres() = libsmtc.clearMusicGenres()

    override fun getMusicAlbumTrackCount(): Int = libsmtc.getMusicAlbumTrackCount().toInt()
    override fun setMusicAlbumTrackCount(count: Int) = libsmtc.setMusicAlbumTrackCount(count.toUInt())

    override fun getMusicTrack(): Int = libsmtc.getMusicTrack().toInt()
    override fun setMusicTrack(track: Int) = libsmtc.setMusicTrack(track.toUInt())
}

private fun Boolean.toInt(): Int =
    if (this) 1 else 0

private fun Int.toBoolean(): Boolean =
    if (this == 0) false else true
