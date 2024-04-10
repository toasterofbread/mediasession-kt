package dev.toastbits.mediasession

import cnames.structs.DBusConnection
import cnames.structs.DBusMessage
import dev.toastbits.mediasession.mpris.DBusMessageBuildScope
import dev.toastbits.mediasession.mpris.buildDBusMessage
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import libdbus.dbus_connection_flush
import libdbus.dbus_connection_send
import libdbus.dbus_message_new_method_return
import libdbus.dbus_message_unref

internal interface MethodHandler {
    fun getConnection(): CPointer<DBusConnection>

    fun processMethod(method: String, message: CValuesRef<DBusMessage>)

    fun replyToMessage(
        message: CValuesRef<DBusMessage>,
        buildReply: (DBusMessageBuildScope.() -> Unit)? = null
    ) {
        replyToMessage(getConnection(), message, buildReply)
    }

    fun replyToUnknownMethod(method_name: String, message: CValuesRef<DBusMessage>) {
        // println("Ignoring unknown method '$method_name' in $this")
        replyToMessage(message)
    }
}

fun CPointerVarOf<CPointer<ByteVar>>.toKString(): String? =
    pointed?.ptr?.toKString()

fun replyToMessage(
    connection: CPointer<DBusConnection>,
    message: CValuesRef<DBusMessage>,
    buildReply: (DBusMessageBuildScope.() -> Unit)? = null
) = memScoped {
    val reply: CPointer<DBusMessage> = dbus_message_new_method_return(message)!!

    if (buildReply != null) {
        buildDBusMessage(reply, build = buildReply)
    }

    val serial: UIntVar = alloc()
    if (dbus_connection_send(connection, reply, serial.ptr) == 0U) {
        throw RuntimeException("Out of memory")
    }

    dbus_connection_flush(connection)
    dbus_message_unref(reply)
}
