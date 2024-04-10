package dev.toastbits.mediasession

import cnames.structs.DBusConnection
import cnames.structs.DBusMessage
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import libdbus.DBUS_TYPE_INT64
import libdbus.DBUS_TYPE_INVALID
import libdbus.DBUS_TYPE_OBJECT_PATH
import libdbus.DBUS_TYPE_STRING
import libdbus.DBusError
import libdbus.dbus_message_get_args

internal class PlayerMethodHandler(val session: MediaSession): MethodHandler {
    override fun getConnection(): CPointer<DBusConnection> = session.connection

    override fun processMethod(method_name: String, message: CValuesRef<DBusMessage>) {
        when (method_name) {
            "Next" -> session.onNext?.invoke()
            "Previous" -> session.onPrevious?.invoke()
            "Pause" -> session.onPause?.invoke()
            "PlayPause" -> session.onPlayPause?.invoke()
            "Stop" -> session.onStop?.invoke()
            "Play" -> session.onPlay?.invoke()
            "Seek" -> memScoped {
                val error: DBusError = alloc()
                val offset: LongVar = alloc()

                if (dbus_message_get_args(message, error.ptr, DBUS_TYPE_INT64, offset, DBUS_TYPE_INVALID) == 0U) {
                    val error_message: String = error.message?.toKString() ?: "No message"
                    throw RuntimeException("Getting Seek argument(s) failed ($error_message)")
                }

                session.onSeek?.invoke(offset.value / 1000)
            }
            "SetPosition" -> memScoped {
                val error: DBusError = alloc()
                val id_bytes: CPointerVarOf<CPointer<ByteVar>> = allocPointerTo()
                val position: LongVar = alloc()

                if (dbus_message_get_args(message, error.ptr, DBUS_TYPE_OBJECT_PATH, id_bytes, DBUS_TYPE_INT64, position, DBUS_TYPE_INVALID) == 0U) {
                    val error_message: String = error.message?.toKString() ?: "No message"
                    throw RuntimeException("Getting Seek argument(s) failed ($error_message)")
                }

                session.onSetPosition?.invoke(position.value / 1000)
            }
            "OpenUri" -> memScoped {
                val error: DBusError = alloc()
                val uri_bytes: CPointerVarOf<CPointer<ByteVar>> = allocPointerTo()

                if (dbus_message_get_args(message, error.ptr, DBUS_TYPE_STRING, uri_bytes, DBUS_TYPE_INVALID) == 0U) {
                    val error_message: String = error.message?.toKString() ?: "No message"
                    throw RuntimeException("Getting OpenUri argument(s) failed ($error_message)")
                }

                val uri: String? = uri_bytes.toKString()
                if (uri != null) {
                    session.onOpenUri?.invoke(uri)
                }
            }
            else -> {
                replyToUnknownMethod(method_name, message)
                return
            }
        }

        replyToMessage(message)
    }
}
