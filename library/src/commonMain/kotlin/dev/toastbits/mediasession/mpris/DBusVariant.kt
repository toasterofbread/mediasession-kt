package dev.toastbits.mediasession.mpris

expect class DBusVariant<T>

expect val <T> DBusVariant<T>.value: T

expect fun <T> createDBusVariant(value: T, signature: String): DBusVariant<T>
expect inline fun <reified T> createDBusVariant(value: T): DBusVariant<T>
expect inline fun <reified T: Any> createDBusVariant(value: Array<T>): DBusVariant<Array<DBusVariant<T>>>
