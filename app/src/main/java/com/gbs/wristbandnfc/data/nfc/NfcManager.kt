package com.gbs.wristbandnfc.data.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NfcState(
    val isEnabled: Boolean = false,
    val isReading: Boolean = false,
    val isWriting: Boolean = false,
    val lastTag: Tag? = null,
    val lastError: String? = null
)

class NfcManager(private val activity: Activity) {

    private val _nfcState = MutableStateFlow(NfcState())
    val nfcState: StateFlow<NfcState> = _nfcState.asStateFlow()

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFilters: Array<IntentFilter>? = null

    init {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        setupNfc()
    }

    private fun setupNfc() {
        pendingIntent = PendingIntent.getActivity(
            activity,
            0,
            Intent(activity, activity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("Failed to add MIME type", e)
            }
        }

        val tagFilter = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)

        intentFilters = arrayOf(ndefFilter, tagFilter)
    }

    fun isNfcSupported(): Boolean = nfcAdapter != null

    fun isNfcEnabled(): Boolean = nfcAdapter?.isEnabled == true

    fun enableForegroundDispatch() {
        _nfcState.value = _nfcState.value.copy(isEnabled = true)
        nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, intentFilters, null)
    }

    fun disableForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(activity)
        _nfcState.value = _nfcState.value.copy(isEnabled = false, isReading = false, isWriting = false)
    }

    fun handleIntent(intent: Intent) {
        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED, NfcAdapter.ACTION_TAG_DISCOVERED -> {
                val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                }

                if (tag != null) {
                    _nfcState.value = _nfcState.value.copy(
                        lastTag = tag,
                        lastError = null
                    )
                }
            }
        }
    }

    fun startReading() {
        _nfcState.value = _nfcState.value.copy(isReading = true, isWriting = false)
    }

    fun stopReading() {
        _nfcState.value = _nfcState.value.copy(isReading = false)
    }

    fun startWriting() {
        _nfcState.value = _nfcState.value.copy(isWriting = true, isReading = false)
    }

    fun stopWriting() {
        _nfcState.value = _nfcState.value.copy(isWriting = false)
    }

    fun setError(error: String?) {
        _nfcState.value = _nfcState.value.copy(lastError = error)
    }

    fun clearError() {
        _nfcState.value = _nfcState.value.copy(lastError = null)
    }

    fun getTagUid(tag: Tag): String {
        return tag.id.toHexString()
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }
}
