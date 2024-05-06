package dev.toastbits.mediasession

import cnames.structs.DBusConnection
import cnames.structs.DBusMessage
import dev.toastbits.mediasession.mpris.DBusVariant
import dev.toastbits.mediasession.mpris.MprisProperty
import dev.toastbits.mediasession.mpris.fromMprisLoopMode
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.DoubleVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.cinterop.BooleanVar
import libdbus.DBUS_TYPE_INT64
import libdbus.DBUS_TYPE_INVALID
import libdbus.DBUS_TYPE_OBJECT_PATH
import libdbus.DBUS_TYPE_STRING
import libdbus.DBusError
import libdbus.dbus_message_get_args
import libdbus.DBusMessageIter
import libdbus.dbus_message_iter_init
import libdbus.dbus_message_iter_get_basic
import libdbus.dbus_message_iter_next
import libdbus.dbus_message_iter_recurse

internal class PropertyMethodHandler(val session: MediaSession): MethodHandler {
    override fun getConnection(): CPointer<DBusConnection> = session.connection

    override fun processMethod(method: String, message: CValuesRef<DBusMessage>) {
        when (method) {
            "GetAll" -> {
                replyToMessage(message) {
                    buildMap {
                        session.properties.forEachProperty { key, value ->
                            addValue(key.name, value)
                        }
                    }
                }
                return
            }
            "Get" -> memScoped {
                val error: DBusError = alloc()
                val iface_bytes: CPointerVarOf<CPointer<ByteVar>> = allocPointerTo()
                val property_bytes: CPointerVarOf<CPointer<ByteVar>> = allocPointerTo()

                if (dbus_message_get_args(message, error.ptr, DBUS_TYPE_STRING, iface_bytes, DBUS_TYPE_STRING, property_bytes, DBUS_TYPE_INVALID) == 0U) {
                    val error_message: String = error.message?.toKString() ?: "No message"
                    throw RuntimeException("Getting Get argument(s) failed ($error_message)")
                }

                val iface: String? = iface_bytes.toKString()
                val property: String? = property_bytes.toKString()
                if (iface == null || property == null) {
                    replyToMessage(message)
                    return
                }

                val mpris_property: MprisProperty? = MprisProperty.entries.firstOrNull { it.name == property }
                if (mpris_property?.getInterface()?.iface != iface) {
                    replyToMessage(message)
                    return
                }

                val value: DBusVariant<*>? = session.properties.getProperty(mpris_property)

                replyToMessage(message) {
                    value?.appendToDBusMessageIterator(iterator.ptr)
                }

                return
            }
            "Set" -> memScoped {
                val error: DBusError = alloc()
                val iface_bytes: CPointerVarOf<CPointer<ByteVar>> = allocPointerTo()
                val property_bytes: CPointerVarOf<CPointer<ByteVar>> = allocPointerTo()

                val iter: DBusMessageIter = alloc()
                val variant_iter: DBusMessageIter = alloc()

                dbus_message_iter_init(message, iter.ptr)

                dbus_message_iter_get_basic(iter.ptr, iface_bytes.ptr)

                if (iface_bytes.toKString() != "org.mpris.MediaPlayer2.Player") {
                    replyToMessage(message)
                    return
                }

                dbus_message_iter_next(iter.ptr)
                dbus_message_iter_get_basic(iter.ptr, property_bytes.ptr)

                dbus_message_iter_next(iter.ptr)
                dbus_message_iter_recurse(iter.ptr, variant_iter.ptr)

                when (property_bytes.toKString()) {
                    "LoopStatus" -> {
                        val value: CPointerVarOf<CPointer<ByteVar>> = allocPointerTo()
                        dbus_message_iter_get_basic(variant_iter.ptr, value.ptr)
                        session.onSetLoop?.invoke(value.toKString()!!.fromMprisLoopMode())
                    }
                    "Shuffle" -> {
                        val value: BooleanVar = alloc()
                        dbus_message_iter_get_basic(variant_iter.ptr, value.ptr)
                        session.onSetShuffle?.invoke(value.value)
                    }
                    "Rate" -> {
                        val value: DoubleVar = alloc()
                        dbus_message_iter_get_basic(variant_iter.ptr, value.ptr)
                        session.onSetRate?.invoke(value.value.toFloat())
                    }
                    else -> {}
                }

                replyToMessage(message)
            }
            else -> {
                replyToUnknownMethod(method, message)
                return
            }
        }

        replyToMessage(message)
    }
}
