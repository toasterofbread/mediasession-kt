package dev.toastbits.mediasession.mpris

import dev.toastbits.mediasession.MediaSessionProperties
import dev.toastbits.mediasession.MediaSessionLoopStatus
import dev.toastbits.mediasession.MediaSessionMetadata
import dev.toastbits.mediasession.MediaSessionPlaybackStatus

abstract class MprisMediaSession: MediaSessionProperties {
    abstract val properties: MprisProperties

    override val identity: String
        get() = (properties.getProperty(MprisProperty.Identity) as DBusVariant<String>).value
    override val fullscreen: Boolean?
        get() = (properties.getProperty(MprisProperty.Fullscreen) as DBusVariant<Boolean?>).value
    override val desktop_entry: String?
        get() = (properties.getProperty(MprisProperty.DesktopEntry) as DBusVariant<String?>).value
    override val supported_uri_schemes: List<String>
        get() = (properties.getProperty(MprisProperty.SupportedUriSchemes) as DBusVariant<List<String>>).value
    override val supported_mime_types: List<String>
        get() = (properties.getProperty(MprisProperty.SupportedMimeTypes) as DBusVariant<List<String>>).value
    override val loop_status: MediaSessionLoopStatus?
        get() = (properties.getProperty(MprisProperty.LoopStatus) as DBusVariant<String?>).value?.fromMprisLoopStatus()
    override val shuffle: Boolean?
        get() = (properties.getProperty(MprisProperty.Shuffle) as DBusVariant<Boolean?>).value
    override val volume: Float
        get() = (properties.getProperty(MprisProperty.Volume) as DBusVariant<Float>).value
    override val rate: Float
        get() = (properties.getProperty(MprisProperty.Rate) as DBusVariant<Float>).value
    override val metadata: MediaSessionMetadata
        get() = (properties.getProperty(MprisProperty.Metadata) as DBusVariant<Map<String, DBusVariant<*>>>?)?.value?.fromMprisPlayerMetadata(identity) ?: MediaSessionMetadata()
    override val playback_status: MediaSessionPlaybackStatus
        get() = (properties.getProperty(MprisProperty.PlaybackStatus) as DBusVariant<String>).value.fromMprisPlaybackStatus()
    override val maximum_rate: Float
        get() = (properties.getProperty(MprisProperty.MaximumRate) as DBusVariant<Float>).value
    override val minimum_rate: Float
        get() = (properties.getProperty(MprisProperty.MinimumRate) as DBusVariant<Float>).value

    override fun setIdentity(identity: String) = properties.setProperty(MprisProperty.Identity, createDBusVariant(identity))
    override fun setFullscreen(fullscreen: Boolean?) = properties.setProperty(MprisProperty.Fullscreen, createDBusVariant(fullscreen))
    override fun setDesktopEntry(desktop_entry: String?) = properties.setProperty(MprisProperty.DesktopEntry, createDBusVariant(desktop_entry))
    override fun setSupportedUriSchemes(supported_uri_schemes: List<String>) = properties.setProperty(MprisProperty.SupportedUriSchemes, createDBusVariant(supported_uri_schemes.toTypedArray()))
    override fun setSupportedMimeTypes(supported_mime_types: List<String>) = properties.setProperty(MprisProperty.SupportedMimeTypes, createDBusVariant(supported_mime_types.toTypedArray()))
    override fun setLoopStatus(loop_status: MediaSessionLoopStatus?) = properties.setProperty(MprisProperty.LoopStatus, createDBusVariant(loop_status?.toMprisLoopStatus()))
    override fun setShuffle(shuffle: Boolean?) = properties.setProperty(MprisProperty.Shuffle, createDBusVariant(shuffle))
    override fun setVolume(volume: Float) = properties.setProperty(MprisProperty.Volume, createDBusVariant(volume))
    override fun setRate(rate: Float) = properties.setProperty(MprisProperty.Rate, createDBusVariant(rate))
    override fun setPlaybackStatus(status: MediaSessionPlaybackStatus) = properties.setProperty(MprisProperty.PlaybackStatus, createDBusVariant(status.toMprisPlaybackStatus()))
    override fun setMaximumRate(maximum_rate: Float) = properties.setProperty(MprisProperty.MaximumRate, createDBusVariant(maximum_rate))
    override fun setMinimumRate(minimum_rate: Float) = properties.setProperty(MprisProperty.MinimumRate, createDBusVariant(minimum_rate))

    override fun setMetadata(metadata: MediaSessionMetadata) =
        properties.setProperty(
            MprisProperty.Metadata,
            createDBusVariant(
                metadata
                    .toMprisPlayerMetadata(identity)
                    .entries
                    .associate {
                        val value = it.value

                        try {
                            if (value is List<*>) {
                                return@associate it.key to createDBusVariant((value as List<String>).toTypedArray())
                            }

                            return@associate it.key to createDBusVariant(value)
                        }
                        catch (e: Throwable) {
                            throw RuntimeException("Could not convert metadata key '${it.key}' with value '$value'", e)
                        }
                    },
                "a{sv}"
            )
        )
}
