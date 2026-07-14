package com.gbs.wristbandnfc.data.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build

/**
 * NFC Manager - Handles NFC foreground dispatch and tag detection
 */
class NfcManager(private val activity: Activity) {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    // Check if NFC is available and enabled
    val isNfcSupported: Boolean get() = nfcAdapter != null
    val isNfcEnabled: Boolean get() = nfcAdapter?.isEnabled == true

    /**
     * Enable foreground dispatch to receive NFC intents
     * Call this in Activity.onResume()
     */
    fun enableForegroundDispatch() {
        if (!isNfcSupported || !isNfcEnabled) return

        val intent = Intent(activity, activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(activity, 0, intent, flags)

        // Intent filters for NFC discovery
        val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                // Ignore
            }
        }

        val tagFilter = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val techFilter = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)

        val filters = arrayOf(ndefFilter, tagFilter, techFilter)

        // Tech lists for specific tag types (NTAG213/215/216)
        val techLists = arrayOf(
            arrayOf("android.nfc.tech.NfcA"),
            arrayOf("android.nfc.tech.NfcB"),
            arrayOf("android.nfc.tech.NfcF"),
            arrayOf("android.nfc.tech.NfcV"),
            arrayOf("android.nfc.tech.IsoDep"),
            arrayOf("android.nfc.tech.MifareClassic"),
            arrayOf("android.nfc.tech.MifareUltralight"),
            arrayOf("android.nfc.tech.Ndef"),
            arrayOf("android.nfc.tech.NdefFormatable")
        )

        nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, filters, techLists)
    }

    /**
     * Disable foreground dispatch
     * Call this in Activity.onPause()
     */
    fun disableForegroundDispatch() {
        try {
            nfcAdapter?.disableForegroundDispatch(activity)
        } catch (e: Exception) {
            // Ignore errors when disabling
        }
    }

    /**
     * Process NFC intent from onNewIntent
     * Call this in Activity.onNewIntent()
     */
    fun processIntent(intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            NfcAdapter.ACTION_TAG_DISCOVERED,
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED -> {
                val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                }

                tag?.let { processTag(it) }
            }
        }
    }

    /**
     * Process detected NFC tag and emit to event bus
     */
    private fun processTag(tag: Tag) {
        val uid = tag.id.toHexString()
        val techList = tag.techList.joinToString(",")

        // Emit NFC event to global event bus
        NfcEventBus.emit(NfcEvent.TagDiscovered(uid, techList))
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }
}

/**
 * NFC Event types
 */
sealed class NfcEvent {
    data class TagDiscovered(
        val uid: String,
        val techList: String
    ) : NfcEvent()
}
