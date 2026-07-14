package com.gbs.wristbandnfc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.gbs.wristbandnfc.data.nfc.NfcManager
import com.gbs.wristbandnfc.ui.navigation.NavGraph
import com.gbs.wristbandnfc.ui.navigation.Screen
import com.gbs.wristbandnfc.ui.theme.WristbandNFCTheme
import com.gbs.wristbandnfc.util.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    // NFC Manager
    lateinit var nfcManager: NfcManager
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize NFC Manager
        nfcManager = NfcManager(this)

        // Handle intent if launched via NFC
        handleIntent(intent)

        setContent {
            WristbandNFCTheme {
                val isLoggedIn by sessionManager.isLoggedInFlow.collectAsState(initial = false)
                val navController = rememberNavController()

                val startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route

                NavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Enable NFC foreground dispatch
        nfcManager.enableForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        // Disable NFC foreground dispatch
        nfcManager.disableForegroundDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle new NFC intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            // Process NFC intent
            nfcManager.processIntent(intent)
        }
    }

    fun isNfcSupported(): Boolean = nfcManager.isNfcSupported
    fun isNfcEnabled(): Boolean = nfcManager.isNfcEnabled
}
