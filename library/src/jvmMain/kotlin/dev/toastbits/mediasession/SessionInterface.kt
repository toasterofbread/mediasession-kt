package dev.toastbits.mediasession

import dev.toastbits.mediasession.mpris.DBusVariant
import dev.toastbits.mediasession.mpris.MprisConstants
import dev.toastbits.mediasession.mpris.MprisProperties
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.Variant
import dev.toastbits.mediasession.mpris.MprisProperty

internal class SessionInterface(
    override val session: MediaSession,
    private val connection: DBusConnection
): MprisProperties(), MediaInterface, PlayerInterface, Properties {
    override fun emitPropertyChange(property: MprisProperty, value: DBusVariant<*>?) {
        connection.sendMessage(
            Properties.PropertiesChanged(
                objectPath,
                property.getInterface().iface,
                mapOf(
                    property.name to value
                ),
                emptyList()
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <A> Get(interface_name: String, property_name: String): A? {
        val property: MprisProperty =
            MprisProperty.entries.firstOrNull { it.name == property_name } ?: return null
        return getProperty(property) as A?
    }

    override fun <A> Set(interface_name: String, property_name: String, value: A) {
        val property: MprisProperty =
            MprisProperty.entries.firstOrNull { it.name == property_name } ?: return
        onPropertySet(property, Variant(value))
    }

    override fun GetAll(interface_name: String): Map<String, Variant<*>> =
        buildMap {
            forEachProperty { key, value ->
                put(key.name, value)
            }
        }

    override fun Raise() {
        session.onRaise?.invoke()
    }
    override fun Quit() {
        session.onQuit?.invoke()
    }
    override fun Next() {
        session.onNext?.invoke()
    }
    override fun Previous() {
        session.onPrevious?.invoke()
    }
    override fun Pause() {
        session.onPause?.invoke()
    }
    override fun PlayPause() {
        session.onPlayPause?.invoke()
    }
    override fun Stop() {
        session.onStop?.invoke()
    }
    override fun Play() {
        session.onPlay?.invoke()
    }
    override fun Seek(by_us: Long) {
        session.onSeek?.invoke(by_us / 1000L)
    }
    override fun SetPosition(arg0: DBusPath, to_us: Long) {
        session.onSetPosition?.invoke(to_us / 1000L)
    }
    override fun OpenUri(uri: String) {
        session.onOpenUri?.invoke(uri)
    }

    override fun getObjectPath(): String =
        MprisConstants.OBJECT_PATH
}
