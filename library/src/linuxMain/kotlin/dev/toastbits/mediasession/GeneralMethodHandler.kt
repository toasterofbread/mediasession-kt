package dev.toastbits.mediasession

import cnames.structs.DBusConnection
import cnames.structs.DBusMessage
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef

internal class GeneralMethodHandler(val session: LinuxMediaSession): MethodHandler {
    override fun getConnection(): CPointer<DBusConnection> = session.connection

    override fun processMethod(method: String, message: CValuesRef<DBusMessage>) {
        when (method) {
            "Raise" -> session.onRaise?.invoke()
            "Quit" -> session.onQuit?.invoke()
            else -> {
                replyToUnknownMethod(method, message)
                return
            }
        }

        replyToMessage(message)
    }
}
