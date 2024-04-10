package dev.toastbits.mediasession

import dev.toastbits.mediasession.mpris.MprisMediaSession
import dev.toastbits.mediasession.mpris.MprisProperties
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder

@Suppress("UNCHECKED_CAST")
actual open class MediaSession: MprisMediaSession(), MediaSessionProperties {
    private var session_enabled: Boolean = false
    private val connection: DBusConnection = DBusConnectionBuilder.forSessionBus().build()

    private val session_interface: SessionInterface = SessionInterface(this, connection)
    override val properties: MprisProperties = session_interface

    actual val enabled: Boolean
        get() = session_enabled

    actual fun setEnabled(enabled: Boolean) {
        if (enabled == session_enabled) {
            return
        }

        if (enabled) {
            connection.requestBusName("org.mpris.MediaPlayer2.$identity")
            connection.exportObject(session_interface.getObjectPath(), session_interface)
            session_enabled = true
        }
        else {
            connection.unExportObject(session_interface.getObjectPath())
            connection.releaseBusName("org.mpris.MediaPlayer2.$identity")
            session_enabled = false
        }
    }

    actual var onRaise: (() -> Unit)? = null
    actual var onQuit: (() -> Unit)? = null
    actual var onSetFullscreen: (() -> Unit)? = null
    actual var onNext: (() -> Unit)? = null
    actual var onPrevious: (() -> Unit)? = null
    actual var onPause: (() -> Unit)? = null
    actual var onPlayPause: (() -> Unit)? = null
    actual var onStop: (() -> Unit)? = null
    actual var onPlay: (() -> Unit)? = null
    actual var onSeek: ((by_ms: Long) -> Unit)? = null
    actual var onSetPosition: ((to_ms: Long) -> Unit)? = null
    actual var onOpenUri: ((uri: String) -> Unit)? = null
    actual var onSetRate: ((rate: Float) -> Unit)? = null

    actual open fun getPositionMs(): Long = 0
    actual fun onPositionChanged() {
        connection.sendMessage(
            PlayerInterface.Seeked(
                session_interface.getObjectPath(),
                getPositionMs()
            )
        )
    }
}
