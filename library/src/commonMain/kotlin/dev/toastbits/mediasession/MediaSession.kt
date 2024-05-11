package dev.toastbits.mediasession

interface MediaSession: MediaSessionProperties {
    val enabled: Boolean
    fun setEnabled(enabled: Boolean)

    var onRaise: (() -> Unit)?
    var onQuit: (() -> Unit)?
    var onNext: (() -> Unit)?
    var onPrevious: (() -> Unit)?
    var onPause: (() -> Unit)?
    var onPlayPause: (() -> Unit)?
    var onStop: (() -> Unit)?
    var onPlay: (() -> Unit)?
    var onSeek: ((by_ms: Long) -> Unit)?
    var onSetPosition: ((to_ms: Long) -> Unit)?
    var onOpenUri: ((uri: String) -> Unit)?
    var onSetRate: ((rate: Float) -> Unit)?
    var onSetLoop: ((loop_mode: MediaSessionLoopMode) -> Unit)?
    var onSetShuffle: ((shuffle_mode: Boolean) -> Unit)?

    fun getPositionMs(): Long = 0
    fun onPositionChanged()

    companion object {
        fun create(getPositionMs: (() -> Long)? = null): MediaSession? = createMediaSession(getPositionMs)
    }
}

expect fun createMediaSession(getPositionMs: (() -> Long)? = null): MediaSession?
