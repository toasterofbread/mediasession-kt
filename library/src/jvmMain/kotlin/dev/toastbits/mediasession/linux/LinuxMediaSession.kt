package dev.toastbits.mediasession.linux

import dev.toastbits.mediasession.*
import dev.toastbits.mediasession.mpris.MprisMediaSession
import dev.toastbits.mediasession.mpris.MprisProperties
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder

@Suppress("UNCHECKED_CAST")
open class LinuxMediaSession: MprisMediaSession(), MediaSession, MediaSessionProperties {
    private var session_enabled: Boolean = false
    private val connection: DBusConnection = DBusConnectionBuilder.forSessionBus().build()

    private val session_interface: SessionInterface = SessionInterface(this, connection)
    override val properties: MprisProperties = session_interface

    override val enabled: Boolean
        get() = session_enabled

    override fun setEnabled(enabled: Boolean) {
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

    override var onRaise: (() -> Unit)? = null
    override var onQuit: (() -> Unit)? = null
    override var onNext: (() -> Unit)? = null
    override var onPrevious: (() -> Unit)? = null
    override var onPause: (() -> Unit)? = null
    override var onPlayPause: (() -> Unit)? = null
    override var onStop: (() -> Unit)? = null
    override var onPlay: (() -> Unit)? = null
    override var onSeek: ((by_ms: Long) -> Unit)? = null
    override var onSetPosition: ((to_ms: Long) -> Unit)? = null
    override var onOpenUri: ((uri: String) -> Unit)? = null
    override var onSetRate: ((rate: Float) -> Unit)? = null
    override var onSetLoop: ((loop_mode: MediaSessionLoopMode) -> Unit)? = null
    override var onSetShuffle: ((shuffle_mode: Boolean) -> Unit)? = null

    override fun onPositionChanged() {
        connection.sendMessage(
            PlayerInterface.Seeked(
                session_interface.getObjectPath(),
                getPositionMs()
            )
        )
    }
}
