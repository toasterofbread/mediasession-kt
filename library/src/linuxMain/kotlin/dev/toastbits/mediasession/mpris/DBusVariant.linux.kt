package dev.toastbits.mediasession.mpris

import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.DoubleVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libdbus.DBUS_TYPE_ARRAY
import libdbus.DBUS_TYPE_BOOLEAN
import libdbus.DBUS_TYPE_DOUBLE
import libdbus.DBUS_TYPE_INT32
import libdbus.DBUS_TYPE_INT64
import libdbus.DBUS_TYPE_VARIANT
import libdbus.DBUS_TYPE_VARIANT_AS_STRING
import libdbus.DBusMessageIter
import libdbus.appendStringToDBusIter
import libdbus.dbus_message_iter_append_basic
import libdbus.dbus_message_iter_close_container
import libdbus.dbus_message_iter_open_container
import kotlin.reflect.KClass

actual data class DBusVariant<T>(val value: T, val signature: String) {
    init {
        if (value is Array<*>) {
            for (item in value) {
                check(item is DBusVariant<*>) { "Array item must be DBusVariant, not '${item?.let { it::class }}'" }
            }
        }
        else if (value is Map<*, *>) {
            for (item in value.values) {
                check(item is DBusVariant<*>) { "Map value must be DBusVariant, not '${item?.let { it::class }}'" }
            }
        }
        else if (value is DBusVariant<*>) {
            throw IllegalStateException("Something's wrong, I can feel it...")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun appendToDBusMessageIterator(iterator: CPointer<DBusMessageIter>) {
        if (value == null) {
            return
        }

        when (value) {
            is String -> appendStringToDBusIter(iterator, value)
            is Boolean -> memScoped {
                val holder: BooleanVar = alloc()
                holder.value = value
                dbus_message_iter_append_basic(iterator, DBUS_TYPE_BOOLEAN, holder.ptr)
            }
            is Double -> memScoped {
                val holder: DoubleVar = alloc()
                holder.value = value
                dbus_message_iter_append_basic(iterator, DBUS_TYPE_DOUBLE, holder.ptr)
            }
            is Float -> memScoped {
                val holder: DoubleVar = alloc()
                holder.value = value.toDouble()
                dbus_message_iter_append_basic(iterator, DBUS_TYPE_DOUBLE, holder.ptr)
            }
            is Int -> memScoped {
                val holder: IntVar = alloc()
                holder.value = value
                dbus_message_iter_append_basic(iterator, DBUS_TYPE_INT32, holder.ptr)
            }
            is Long -> memScoped {
                val holder: LongVar = alloc()
                holder.value = value
                dbus_message_iter_append_basic(iterator, DBUS_TYPE_INT64, holder.ptr)
            }
            is Array<*> -> {
                val items: Array<DBusVariant<*>> = value as Array<DBusVariant<*>>
                val item_signature: String = signature.removePrefix("a")

                appendArrayToDBusIterator(iterator, item_signature) { array ->
                    for (item in items) {
                        item.appendToDBusMessageIterator(array)
                    }
                }
            }
            is Map<*, *> -> memScoped {
                val map: Map<String, DBusVariant<*>> = value as Map<String, DBusVariant<*>>
                val item_signature: String = signature.removePrefix("a")

                appendArrayToDBusIterator(iterator, item_signature) { array ->
                    for ((key, value) in map) {
                        appendDictEntryToDBusIterator(array, key, value)
                    }
                }
            }
            else -> throw NotImplementedError("Can't append unsupported DBus variant type '${value!!::class}'")
        }
    }
}

actual val <T> DBusVariant<T>.value: T get() = this.value

actual fun <T> createDBusVariant(value: T, signature: String): DBusVariant<T> =
    DBusVariant(value, signature)

actual inline fun <reified T> createDBusVariant(value: T): DBusVariant<T> =
    try {
        DBusVariant(value, value?.getDBusSignature() ?: T::class.getDBusSignature())
    }
    catch (e: Throwable) {
        throw RuntimeException("Could not get signature for value '$value'", e)
    }

actual inline fun <reified T: Any> createDBusVariant(value: Array<T>): DBusVariant<Array<DBusVariant<T>>> {
    val item_signature: String = T::class.getDBusSignature()
    return DBusVariant(
        Array(value.size) {
            createDBusVariant(value[it], item_signature)
        },
        "a" + item_signature
    )
}

fun KClass<*>.getDBusSignature(): String =
    when (this) {
        String::class -> "s"
        Boolean::class -> "b"
        Double::class, Float::class -> "d"
        Int::class -> "i"
        Long::class -> "x"
        else -> throw NotImplementedError("Can't get signature for unsupported DBus variant type '${this::class}'")
    }

fun <T: Any> T.getDBusSignature(): String =
    when (this) {
        is String -> "s"
        is Boolean -> "b"
        is Double, is Float -> "d"
        is Int -> "i"
        is Long -> "x"
        else -> throw NotImplementedError("Can't get signature for unsupported DBus variant type '${this::class}'")
    }
