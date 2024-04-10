package dev.toastbits.mediasession.mpris

object MprisConstants {
    val OBJECT_PATH: String = "/org/mpris/MediaPlayer2"

    enum class Interface(val iface: String) {
        GENERAL("org.mpris.MediaPlayer2"),
        PLAYER("org.mpris.MediaPlayer2.Player"),
        DBUS_PROPERTIES("org.freedesktop.DBus.Properties")
    }

    fun getBusName(identity: String): String =
        "org.mpris.MediaPlayer2.$identity"
}
