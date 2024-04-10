package dev.toastbits.mediasession.mpris

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import libdbus.DBUS_TYPE_ARRAY
import libdbus.DBUS_TYPE_DICT_ENTRY
import libdbus.DBUS_TYPE_VARIANT
import libdbus.DBusMessageIter
import libdbus.appendStringToDBusIter
import libdbus.dbus_message_iter_close_container
import libdbus.dbus_message_iter_open_container

internal fun appendArrayToDBusIterator(
    iterator: CPointer<DBusMessageIter>,
    item_signature: String?,
    addItems: (CPointer<DBusMessageIter>) -> Unit
) = memScoped {
    val subarray: DBusMessageIter = alloc()
    dbus_message_iter_open_container(iterator, DBUS_TYPE_ARRAY, item_signature, subarray.ptr)
    addItems(subarray.ptr)
    dbus_message_iter_close_container(iterator, subarray.ptr)
}

internal fun appendDictEntryToDBusIterator(
    iterator: CPointer<DBusMessageIter>,
    key: String,
    value: DBusVariant<*>
) = memScoped {
    val dict: DBusMessageIter = alloc()
    val variant: DBusMessageIter = alloc()

    dbus_message_iter_open_container(iterator, DBUS_TYPE_DICT_ENTRY, null, dict.ptr)
    appendStringToDBusIter(dict.ptr, key)

    dbus_message_iter_open_container(dict.ptr, DBUS_TYPE_VARIANT, value.signature, variant.ptr)
    value.appendToDBusMessageIterator(variant.ptr)
    dbus_message_iter_close_container(dict.ptr, variant.ptr)

    dbus_message_iter_close_container(iterator, dict.ptr)
}
