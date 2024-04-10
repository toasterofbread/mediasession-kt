package dev.toastbits.mediasession.mpris

import org.freedesktop.dbus.types.Variant

actual typealias DBusVariant<T> = Variant<T>

actual val <T> DBusVariant<T>.value: T get() = this.value

actual fun <T> createDBusVariant(
    value: T,
    signature: String
): DBusVariant<T> =
    Variant(value, signature)

actual inline fun <reified T> createDBusVariant(value: T): DBusVariant<T> =
    Variant(value)

actual inline fun <reified T : Any> createDBusVariant(value: Array<T>): DBusVariant<Array<DBusVariant<T>>> =
    Variant(Array(value.size) { Variant(value[it]) })
