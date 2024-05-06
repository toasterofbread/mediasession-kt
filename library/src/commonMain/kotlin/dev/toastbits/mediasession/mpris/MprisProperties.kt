package dev.toastbits.mediasession.mpris

import dev.toastbits.mediasession.MediaSession
import dev.toastbits.mediasession.MediaSessionMetadata

abstract class MprisProperties {
    protected abstract val session: MediaSession
    protected abstract fun emitPropertyChange(property: MprisProperty, value: DBusVariant<*>?)

    var metadata: MediaSessionMetadata = MediaSessionMetadata()
        private set

    private val properties: MutableMap<MprisProperty, DBusVariant<*>?> =
        mutableMapOf(
            MprisProperty.HasTrackList to createDBusVariant(false),
            MprisProperty.Identity to createDBusVariant("mediasessionkt"),
            MprisProperty.DesktopEntry to null,
            MprisProperty.SupportedUriSchemes to createDBusVariant(emptyArray<String>()),
            MprisProperty.SupportedMimeTypes to createDBusVariant(emptyArray<String>()),
            MprisProperty.PlaybackStatus to createDBusVariant("Stopped"),
            MprisProperty.LoopStatus to createDBusVariant("None"),
            MprisProperty.Rate to createDBusVariant(1.0),
            MprisProperty.Shuffle to createDBusVariant(false),
            MprisProperty.Metadata to createDBusVariant(emptyMap<String, DBusVariant<*>>(), "a{sv}"),
            MprisProperty.Volume to createDBusVariant(1.0),
            MprisProperty.MaximumRate to createDBusVariant(1.0),
            MprisProperty.MinimumRate to createDBusVariant(1.0),
        )

    fun getProperty(property: MprisProperty): DBusVariant<*>? =
        when (property) {
            MprisProperty.Position -> createDBusVariant(session.getPositionMs() * 1000)
            MprisProperty.CanRaise -> createDBusVariant(session.onRaise != null)
            MprisProperty.CanQuit -> createDBusVariant(session.onQuit != null)
            MprisProperty.CanGoNext -> createDBusVariant(session.onNext != null)
            MprisProperty.CanGoPrevious -> createDBusVariant(session.onPrevious != null)
            MprisProperty.CanPlay -> createDBusVariant(session.onPlay != null)
            MprisProperty.CanPause -> createDBusVariant(session.onPause != null)
            MprisProperty.CanSeek -> createDBusVariant(session.onSeek != null)
            MprisProperty.CanControl -> createDBusVariant(true)
            else -> {
                if (!properties.contains(property)) {
                    throw NotImplementedError(property.toString())
                }

                properties[property]
            }
        }

    inline fun forEachProperty(action: (MprisProperty, DBusVariant<*>) -> Unit) {
        for (property in MprisProperty.entries) {
            val value: DBusVariant<*> = getProperty(property) ?: continue
            action(property, value)
        }
    }

    fun setProperty(property: MprisProperty, value: DBusVariant<*>?) {
        onPropertySet(property, value)
        emitPropertyChange(property, value)
    }

    protected fun onPropertySet(property: MprisProperty, value: DBusVariant<*>?) {
        if (properties.contains(property)) {
            properties[property] = value
        }
    }
}
