package dev.toastbits.mediasession

interface MediaSessionProperties {
    val identity: String
    val fullscreen: Boolean?
    val desktop_entry: String?
    val supported_uri_schemes: List<String>
    val supported_mime_types: List<String>
    val loop_status: MediaSessionLoopStatus?
    val shuffle: Boolean?
    val volume: Float
    val rate: Float
    val playback_status: MediaSessionPlaybackStatus
    val maximum_rate: Float
    val minimum_rate: Float
    val metadata: MediaSessionMetadata

    fun setIdentity(identity: String)
    fun setFullscreen(fullscreen: Boolean?)
    fun setDesktopEntry(desktop_entry: String?)
    fun setSupportedUriSchemes(supported_uri_schemes: List<String>)
    fun setSupportedMimeTypes(supported_mime_types: List<String>)
    fun setLoopStatus(loop_status: MediaSessionLoopStatus?)
    fun setShuffle(shuffle: Boolean?)
    fun setVolume(volume: Float)
    fun setRate(rate: Float)
    fun setPlaybackStatus(status: MediaSessionPlaybackStatus)
    fun setMaximumRate(maximum_rate: Float)
    fun setMinimumRate(minimum_rate: Float)
    fun setMetadata(metadata: MediaSessionMetadata)
}
