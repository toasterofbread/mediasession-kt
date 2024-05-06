package dev.toastbits.mediasession.mpris

class DummyDBusVariant<T>

actual typealias DBusVariant<T> = DummyDBusVariant<T>

actual val <T> DBusVariant<T>.value: T get() = throw IllegalStateException()

actual fun <T> createDBusVariant(
    value: T,
    signature: String
): DBusVariant<T> =
    throw IllegalStateException()

actual inline fun <reified T> createDBusVariant(value: T): DBusVariant<T> =
    throw IllegalStateException()

actual inline fun <reified T : Any> createDBusVariant(value: Array<T>): DBusVariant<Array<DBusVariant<T>>> =
    throw IllegalStateException()
