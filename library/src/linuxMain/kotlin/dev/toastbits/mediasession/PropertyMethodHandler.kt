package dev.toastbits.mediasession

import cnames.structs.DBusConnection
import cnames.structs.DBusMessage
import dev.toastbits.mediasession.mpris.DBusVariant
import dev.toastbits.mediasession.mpris.MprisProperty
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.CValuesRef
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
            else -> {
                replyToUnknownMethod(method, message)
                return
            }
        }

        replyToMessage(message)
    }
}
