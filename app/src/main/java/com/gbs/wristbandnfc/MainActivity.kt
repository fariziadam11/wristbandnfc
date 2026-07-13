package com.gbs.wristbandnfc

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.gbs.wristbandnfc.data.nfc.NfcManager
import com.gbs.wristbandnfc.ui.navigation.NavGraph
import com.gbs.wristbandnfc.ui.navigation.Screen
import com.gbs.wristbandnfc.ui.screens.dashboard.DashboardScreen
import com.gbs.wristbandnfc.ui.theme.WristbandNFCTheme
import com.gbs.wristbandnfc.util.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    private var nfcManager: NfcManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        nfcManager = NfcManager(this)

        setContent {
            WristbandNFCTheme {
                val isLoggedIn by sessionManager.isLoggedInFlow.collectAsState(initial = false)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val startDestination = if (isLoggedIn) {
                        Screen.Dashboard.route
                    } else {
                        Screen.Login.route
                    }

                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }

        // Handle NFC intent if app was launched via NFC
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        nfcManager?.enableForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        nfcManager?.disableForegroundDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            intent?.action == NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            // Handle NFC tag discovery
            // This will be processed by the ScanScreen
            nfcManager?.handleIntent(intent)
        }
    }
}
