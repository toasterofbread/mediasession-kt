package dev.toastbits.mediasession

expect open class MediaSession(): MediaSessionProperties {
    val enabled: Boolean
    fun setEnabled(enabled: Boolean)

    var onRaise: (() -> Unit)?
    var onQuit: (() -> Unit)?
    var onSetFullscreen: (() -> Unit)?
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

    open fun getPositionMs(): Long
    fun onPositionChanged()
}
