package dev.toastbits.mediasession

import org.freedesktop.dbus.annotations.DBusInterfaceName
import org.freedesktop.dbus.interfaces.DBusInterface

@DBusInterfaceName("org.mpris.MediaPlayer2")
internal interface MediaInterface: DBusInterface {
    fun Raise()
    fun Quit()
}
