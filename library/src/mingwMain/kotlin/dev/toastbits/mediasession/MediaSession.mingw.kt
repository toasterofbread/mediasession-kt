package dev.toastbits.mediasession

import dev.toastbits.mediasession.smtc.SMTCMediaSession
import dev.toastbits.mediasession.smtc.NativeSMTCAdapter

actual fun createMediaSession(getPositionMs: (() -> Long)?): MediaSession? =
    object : SMTCMediaSession(NativeSMTCAdapter()) {
        override fun getPositionMs(): Long = getPositionMs?.invoke() ?: super.getPositionMs()
    }
