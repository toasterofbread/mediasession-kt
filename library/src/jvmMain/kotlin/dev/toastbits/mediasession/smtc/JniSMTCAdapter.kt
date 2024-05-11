package dev.toastbits.mediasession.smtc

import com.sun.jna.Native
import com.sun.jna.Library
import com.sun.jna.win32.W32APIOptions
import com.sun.jna.Pointer
import com.sun.jna.Callback
import dev.toastbits.mediasession.MediaSessionLoopMode

class JniSMTCAdapter: SMTCAdapter {
    private lateinit var libsmtc: SMTCAdapterLibrary

    private var onPauseCallback: Callback? = null
    private var onStopCallback: Callback? = null
    private var onPlayCallback: Callback? = null
    private var onNextCallback: Callback? = null
    private var onPreviousCallback: Callback? = null
    private var onSetPositionCallback: Callback? = null
    private var onSetRateCallback: Callback? = null
    private var onSetLoopCallback: Callback? = null
    private var onSetShuffleCallback: Callback? = null

    override fun init(): Int {
        libsmtc = 
            Native.loadLibrary(
                "libSMTCAdapter.dll",
                SMTCAdapterLibrary::class.java,
                W32APIOptions.DEFAULT_OPTIONS
            )

        val result: Int = libsmtc.init()
        if (result != 0) {
            return result
        }

        libsmtc.setMediaType(0)
        return 0
    }

    override var onPause: (() -> Unit)? = null
        set(value) {
            field = value
            
            val callback = value?.let { Callback1(it) }
            libsmtc.setOnPause(callback)
            onPauseCallback = callback
        }
    override var onStop: (() -> Unit)? = null
        set(value) {
            field = value
            
            val callback = value?.let { Callback1(it) }
            libsmtc.setOnStop(callback)
            onStopCallback = callback
        }
    override var onPlay: (() -> Unit)? = null
        set(value) {
            field = value
            
            val callback = value?.let { Callback1(it) }
            libsmtc.setOnPlay(callback)
            onPlayCallback = callback
        }
    override var onNext: (() -> Unit)? = null
        set(value) {
            field = value
            
            val callback = value?.let { Callback1(it) }
            libsmtc.setOnNext(callback)
            onNextCallback = callback
        }
    override var onPrevious: (() -> Unit)? = null
        set(value) {
            field = value
            
            val callback = value?.let { Callback1(it) }
            libsmtc.setOnPrevious(callback)
            onPreviousCallback = callback
        }
    override var onSetPosition: ((to_ms: Long) -> Unit)? = null
        set(value) {
            field = value
            
            val callback = value?.let { CallbackSeek(it) }
            libsmtc.setOnSeek(callback)
            onSetPositionCallback = callback
        }
    override var onSetRate: ((rate: Float) -> Unit)? = null
        set(value) {
            field = value
            
            val callback = value?.let { CallbackRate(it) }
            libsmtc.setOnRateChanged(callback)
            onSetRateCallback = callback
        }
    override var onSetLoop: ((loop_mode: Int) -> Unit)? = null
        set(value) {
            field = value
            
            val callback = value?.let { CallbackLoop(it) }
            libsmtc.setOnLoopChanged(callback)
            onSetLoopCallback = callback
        }
    override var onSetShuffle: ((shuffle_mode: Boolean) -> Unit)? = null
        set(value) {
            field = value
            
            val callback = value?.let { CallbackShuffle(it) }
            libsmtc.setOnShuffleChanged(callback)
            onSetShuffleCallback = callback
        }

    override fun revokeCallbacks() = libsmtc.revokeCallbacks()

    override fun getEnabled(): Boolean = libsmtc.getEnabled()
    override fun setEnabled(enabled: Boolean) = libsmtc.setEnabled(enabled)

    override fun getNextEnabled(): Boolean = libsmtc.getNextEnabled()
    override fun setNextEnabled(enabled: Boolean) {
        libsmtc.setNextEnabled(enabled)
    }

    override fun getPreviousEnabled(): Boolean = libsmtc.getPreviousEnabled()
    override fun setPreviousEnabled(enabled: Boolean) = libsmtc.setPreviousEnabled(enabled)

    override fun getPlayEnabled(): Boolean = libsmtc.getPlayEnabled()
    override fun setPlayEnabled(enabled: Boolean) = libsmtc.setPlayEnabled(enabled)

    override fun getPauseEnabled(): Boolean = libsmtc.getPauseEnabled()
    override fun setPauseEnabled(enabled: Boolean) = libsmtc.setPauseEnabled(enabled)

    override fun getStopEnabled(): Boolean = libsmtc.getStopEnabled()
    override fun setStopEnabled(enabled: Boolean) = libsmtc.setStopEnabled(enabled)

    override fun getRate(): Double = libsmtc.getRate()
    override fun setRate(rate: Double) = libsmtc.setRate(rate)

    override fun getShuffle(): Boolean = libsmtc.getShuffle()
    override fun setShuffle(shuffle: Boolean) = libsmtc.setShuffle(shuffle)

    override fun getLoop(): Int = libsmtc.getLoop()
    override fun setLoop(loop: Int) = libsmtc.setLoop(loop)

    override fun getPlaybackState(): Int = libsmtc.getPlaybackState()
    override fun setPlaybackState(state: Int) = libsmtc.setPlaybackState(state)

    override fun setTimelineProperties(start: Long, end: Long, seek_start: Long, seek_end: Long) = libsmtc.setTimelineProperties(start, end, seek_start, seek_end)
    override fun setPosition(position: Long) = libsmtc.setPosition(position)

    override fun update() = libsmtc.update()
    override fun reset() = libsmtc.reset()

    override fun getMediaType(): Int = libsmtc.getMediaType()
    override fun setMediaType(type: Int) = libsmtc.setMediaType(type)

    override fun thumbnailLoaded(): Boolean = libsmtc.thumbnailLoaded()
    override fun setThumbnail(path: String, update_when_loaded: Boolean) = libsmtc.setThumbnail(path, update_when_loaded)

    override fun getMusicTitle(): String = libsmtc.getMusicTitle()
    override fun setMusicTitle(title: String) = libsmtc.setMusicTitle(title)

    override fun getMusicArtist(): String = libsmtc.getMusicArtist()
    override fun setMusicArtist(artist: String) = libsmtc.setMusicArtist(artist)

    override fun getMusicAlbumTitle(): String = libsmtc.getMusicAlbumTitle()
    override fun setMusicAlbumTitle(title: String) = libsmtc.setMusicAlbumTitle(title)

    override fun getMusicAlbumArtist(): String = libsmtc.getMusicAlbumArtist()
    override fun setMusicAlbumArtist(artist: String) = libsmtc.setMusicAlbumArtist(artist)

    override fun getMusicGenresSize(): Int = libsmtc.getMusicGenresSize()
    override fun getMusicGenreAt(index: Int): String = libsmtc.getMusicGenreAt(index)
    override fun addMusicGenre(genre: String) = libsmtc.addMusicGenre(genre)
    override fun clearMusicGenres() = libsmtc.clearMusicGenres()

    override fun getMusicAlbumTrackCount(): Int = libsmtc.getMusicAlbumTrackCount()
    override fun setMusicAlbumTrackCount(count: Int) = libsmtc.setMusicAlbumTrackCount(count)

    override fun getMusicTrack(): Int = libsmtc.getMusicTrack()
    override fun setMusicTrack(track: Int) = libsmtc.setMusicTrack(track)
}

private interface SMTCAdapterLibrary: SMTCAdapter, Library {
    fun setOnPause(callback: Callback1?)
    fun setOnStop(callback: Callback1?)
    fun setOnPlay(callback: Callback1?)
    fun setOnNext(callback: Callback1?)
    fun setOnPrevious(callback: Callback1?)
    fun setOnSeek(callback: CallbackSeek?)
    fun setOnRateChanged(callback: CallbackRate?)
    fun setOnLoopChanged(callback: CallbackLoop?)
    fun setOnShuffleChanged(callback: CallbackShuffle?)
}

@Suppress("UNUSED_PARAMETER")
private class Callback1(val callback: () -> Unit): Callback {
    fun callback(data: Int?) { callback.invoke() }
}

@Suppress("UNUSED_PARAMETER")
private class CallbackSeek(val callback: (Long) -> Unit): Callback {
    fun callback(value: Long, data: Int?) { callback.invoke(value) }
}

@Suppress("UNUSED_PARAMETER")
private class CallbackRate(val callback: (Float) -> Unit): Callback {
    fun callback(value: Float, data: Int?) { callback.invoke(value) }
}

@Suppress("UNUSED_PARAMETER")
private class CallbackLoop(val callback: (Int) -> Unit): Callback {
    fun callback(value: Int, data: Int?) { callback.invoke(value) }
}

@Suppress("UNUSED_PARAMETER")
private class CallbackShuffle(val callback: (Boolean) -> Unit): Callback {
    fun callback(value: Boolean, data: Int?) { callback.invoke(value) }
}
