package dev.toastbits.mediasession.mpris

enum class MprisProperty {
    CanRaise,
    CanQuit,
    CanGoNext,
    CanGoPrevious,
    CanPlay,
    CanPause,
    CanSeek,
    CanControl,

    HasTrackList,
    Identity,
    DesktopEntry,
    SupportedUriSchemes,
    SupportedMimeTypes,
    PlaybackStatus,
    LoopStatus,
    Rate,
    Shuffle,
    Metadata,
    Volume,
    Position,
    MaximumRate,
    MinimumRate;

    fun getInterface(): MprisConstants.Interface =
        when (this) {
            CanQuit,
            CanRaise,
            HasTrackList,
            Identity,
            DesktopEntry,
            SupportedUriSchemes,
            SupportedMimeTypes -> MprisConstants.Interface.GENERAL
            else -> MprisConstants.Interface.PLAYER
        }
}
