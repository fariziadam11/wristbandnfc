package com.gbs.wristbandnfc.data.nfc

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global NFC Event Bus
 * Singleton that holds NFC events from MainActivity
 * ViewModels can collect from this
 */
object NfcEventBus {

    private val _events = MutableSharedFlow<NfcEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<NfcEvent> = _events.asSharedFlow()

    /**
     * Emit an NFC event from MainActivity
     */
    fun emit(event: NfcEvent) {
        _events.tryEmit(event)
    }
}
