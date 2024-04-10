package dev.toastbits.mediasession.mpris

import cnames.structs.DBusMessage
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import libdbus.DBUS_TYPE_ARRAY
import libdbus.DBusMessageIter
import libdbus.dbus_message_iter_close_container
import libdbus.dbus_message_iter_init_append
import libdbus.dbus_message_iter_open_container

fun buildDBusMessage(
    message: CPointer<DBusMessage>,
    build: DBusMessageBuildScope.() -> Unit
) = memScoped {
    val iter: DBusMessageIter = alloc()
    dbus_message_iter_init_append(message, iter.ptr)
    build(DBusMessageBuildScope(iter))
}

class DBusMessageBuildScope(
    val iterator: DBusMessageIter
) {
    fun buildMap(build: MapBuildScope.() -> Unit) = memScoped {
        val map: DBusMessageIter = alloc()
        dbus_message_iter_open_container(iterator.ptr, DBUS_TYPE_ARRAY, "{sv}", map.ptr)

        try {
            build(MapBuildScope(map))
        }
        finally {
            dbus_message_iter_close_container(iterator.ptr, map.ptr)
        }
    }

    class MapBuildScope(private val iterator: DBusMessageIter) {
        fun addValue(key: String, value: DBusVariant<*>) = memScoped {
            appendDictEntryToDBusIterator(iterator.ptr, key, value)
        }
    }
}
