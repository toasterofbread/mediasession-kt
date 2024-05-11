package dev.toastbits.mediasession

import cnames.structs.DBusConnection
import cnames.structs.DBusMessage
import dev.toastbits.mediasession.mpris.DBusVariant
import dev.toastbits.mediasession.mpris.MprisConstants
import dev.toastbits.mediasession.mpris.MprisMediaSession
import dev.toastbits.mediasession.mpris.MprisProperties
import dev.toastbits.mediasession.mpris.MprisProperty
import dev.toastbits.mediasession.mpris.buildDBusMessage
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import libdbus.DBUS_RELEASE_NAME_REPLY_RELEASED
import libdbus.DBUS_REQUEST_NAME_REPLY_PRIMARY_OWNER
import libdbus.DBUS_TYPE_ARRAY
import libdbus.DBUS_TYPE_DICT_ENTRY
import libdbus.DBusBusType
import libdbus.DBusError
import libdbus.DBusMessageIter
import libdbus.DBusObjectPathVTable
import libdbus.appendStringToDBusIter
import libdbus.dbus_bus_get
import libdbus.dbus_bus_release_name
import libdbus.dbus_bus_request_name
import libdbus.dbus_connection_flush
import libdbus.dbus_connection_pop_message
import libdbus.dbus_connection_read_write
import libdbus.dbus_connection_register_object_path
import libdbus.dbus_connection_send
import libdbus.dbus_connection_unregister_object_path
import libdbus.dbus_error_is_set
import libdbus.dbus_message_get_interface
import libdbus.dbus_message_get_member
import libdbus.dbus_message_iter_close_container
import libdbus.dbus_message_iter_init_append
import libdbus.dbus_message_iter_open_container
import libdbus.dbus_message_new_signal
import libdbus.dbus_message_unref
import dev.toastbits.mediasession.mpris.createDBusVariant

actual fun createMediaSession(getPositionMs: (() -> Long)?): MediaSession? = 
    object : LinuxMediaSession() {
        override fun getPositionMs(): Long = getPositionMs?.invoke() ?: super.getPositionMs()
    }

open class LinuxMediaSession: MprisMediaSession(), MediaSession, MediaSessionProperties {
    private var session_enabled: Boolean = false
    private val method_handlers: Map<String, MethodHandler> =
        MprisConstants.Interface.entries.associate { mpris_interface ->
            val handler: MethodHandler =
                when (mpris_interface) {
                    MprisConstants.Interface.GENERAL -> GeneralMethodHandler(this)
                    MprisConstants.Interface.PLAYER -> PlayerMethodHandler(this)
                    MprisConstants.Interface.DBUS_PROPERTIES -> PropertyMethodHandler(this)
                }

            return@associate mpris_interface.iface to handler
        }

    private val coroutine_scope: CoroutineScope = CoroutineScope(Job())
    private var listen_job: Job? = null

    internal val connection: CPointer<DBusConnection>

    override val properties: MprisProperties =
        object : MprisProperties() {
            override val session: LinuxMediaSession
                get() = this@LinuxMediaSession

            override fun emitPropertyChange(property: MprisProperty, value: DBusVariant<*>?) {
                if (!enabled) {
                    return
                }

                memScoped {
                    val message: CPointer<DBusMessage> =
                        dbus_message_new_signal(
                            MprisConstants.OBJECT_PATH,
                            "org.freedesktop.DBus.Properties",
                            "PropertiesChanged"
                        )!!

                    buildDBusMessage(message) {
                        appendStringToDBusIter(iterator.ptr, property.getInterface().iface)

                        buildMap {
                            if (value != null) {
                                addValue(property.name, value)
                            }
                        }

                        val array: DBusMessageIter = alloc()
                        dbus_message_iter_open_container(iterator.ptr, DBUS_TYPE_ARRAY, "s", array.ptr)
                        dbus_message_iter_close_container(iterator.ptr, array.ptr)
                    }

                    val serial: UIntVar = alloc()
                    if (dbus_connection_send(session.connection, message, serial.ptr) == 0U) {
                        throw RuntimeException("Out of memory")
                    }

                    dbus_connection_flush(session.connection)
                    dbus_message_unref(message)
                }
            }
        }

    override val enabled: Boolean
        get() = session_enabled

    init {
        var connection: CPointer<DBusConnection>? = null
        memScoped {
            val error: DBusError = alloc()
            connection = dbus_bus_get(DBusBusType.DBUS_BUS_SESSION, error.ptr)

            if (dbus_error_is_set(error.ptr) == 1U) {
                val message: String = error.message?.toKString() ?: "No message"
                throw RuntimeException("Getting DBus connection failed ($message)")
            }
        }

        this@LinuxMediaSession.connection = connection ?: throw NullPointerException("Connection is null")
    }

    override fun setEnabled(enabled: Boolean) {
        if (enabled == session_enabled) {
            return
        }

        if (enabled) {
            memScoped {
                val error: DBusError = alloc()

                val result: Int = dbus_bus_request_name(connection, MprisConstants.getBusName(identity), 0U, error.ptr)
                if (result != DBUS_REQUEST_NAME_REPLY_PRIMARY_OWNER) {
                    val message: String = error.message?.toKString() ?: "No message"
                    throw RuntimeException("Requesting name for DBus connection failed ($message)")
                }

                val server_vtable: DBusObjectPathVTable = alloc()
                if (dbus_connection_register_object_path(connection, MprisConstants.OBJECT_PATH, server_vtable.ptr, null) == 0U) {
                    throw RuntimeException("Failed to register an object path for connection")
                }

                session_enabled = true

                listen_job =
                    coroutine_scope.launch(Dispatchers.IO) {
                        listen()
                    }
            }
        }
        else {
            dbus_connection_unregister_object_path(connection, MprisConstants.OBJECT_PATH)

            memScoped {
                listen_job?.cancel()
                listen_job = null

                val error: DBusError = alloc()

                val result: Int = dbus_bus_release_name(connection, MprisConstants.getBusName(identity), error.ptr)
                if (result != DBUS_RELEASE_NAME_REPLY_RELEASED) {
                    val message: String = error.message?.toKString() ?: "No message"
                    throw RuntimeException("Releasing name for DBus connection failed ($message)")
                }

                session_enabled = false
            }
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

    override fun onPositionChanged() = memScoped {
        val message: CPointer<DBusMessage> =
            dbus_message_new_signal(
                MprisConstants.OBJECT_PATH,
                MprisConstants.Interface.PLAYER.iface,
                "Seeked"
            )!!

        buildDBusMessage(message) {
            createDBusVariant(getPositionMs() * 1000L).appendToDBusMessageIterator(iterator.ptr)
        }

        val serial: UIntVar = alloc()
        if (dbus_connection_send(connection, message, serial.ptr) == 0U) {
            throw RuntimeException("Out of memory")
        }

        dbus_connection_flush(connection)
        dbus_message_unref(message)
    }

    private suspend fun listen() {
        memScoped {
            val msg: CPointerVarOf<CPointer<DBusMessage>> = allocPointerTo()
            while (true) {
                dbus_connection_read_write(connection,0)
                msg.value = dbus_connection_pop_message(connection)

                if (msg.value == null) {
                    delay(1)
                    continue
                }

                try {
                    processDBusMessage(msg.value!!)
                }
                catch (e: Throwable) {
                    e.printStackTrace()
                    throw e
                }
                finally {
                    dbus_message_unref(msg.value!!)
                }
            }
        }
    }

    private fun processDBusMessage(message: CValuesRef<DBusMessage>) {
        val iface: String = dbus_message_get_interface(message)?.toKString() ?: return
        val method: String = dbus_message_get_member(message)?.toKString() ?: return

        val handler: MethodHandler? = method_handlers[iface]
        if (handler == null) {
            // println("Ignoring unknown interface $iface and method $method")
            replyToMessage(connection, message)
            return
        }

        handler.processMethod(method, message)
    }
}
