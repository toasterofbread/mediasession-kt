package dev.toastbits.mediasession

import dev.toastbits.mediasession.linux.LinuxMediaSession
import dev.toastbits.mediasession.smtc.SMTCMediaSession
import dev.toastbits.mediasession.smtc.JniSMTCAdapter

actual fun createMediaSession(getPositionMs: (() -> Long)?): MediaSession? {
    val os: String = System.getProperty("os.name").lowercase()

    if (os.startsWith("windows")) {
        return object : SMTCMediaSession(JniSMTCAdapter()) {
            override fun getPositionMs(): Long = getPositionMs?.invoke() ?: super.getPositionMs()
        }
    }

    if (os.contains("linux")) {
        return object : LinuxMediaSession() {
            override fun getPositionMs(): Long = getPositionMs?.invoke() ?: super.getPositionMs()
        }
    }

    return null
}
