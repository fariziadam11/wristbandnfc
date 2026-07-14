package com.gbs.wristbandnfc.ui.screens.scan

import android.app.Activity
import android.nfc.NfcAdapter
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gbs.wristbandnfc.data.nfc.NfcEventBus
import com.gbs.wristbandnfc.ui.theme.PrimaryBlue
import com.gbs.wristbandnfc.ui.theme.SuccessGreen
import com.gbs.wristbandnfc.ui.theme.WarningOrange
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onScanSuccess: (uid: String, token: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // Check NFC status
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(activity) }
    val isNfcSupported = nfcAdapter != null
    val isNfcEnabled = nfcAdapter?.isEnabled == true

    // Handle scan result - navigate on success
    LaunchedEffect(uiState.scanResult) {
        uiState.scanResult?.let { result ->
            onScanSuccess(result.first, result.second)
        }
    }

    // Collect NFC events from event bus
    LaunchedEffect(Unit) {
        NfcEventBus.events.collectLatest { event ->
            when (event) {
                is com.gbs.wristbandnfc.data.nfc.NfcEvent.TagDiscovered -> {
                    viewModel.onNfcTagScanned(event.uid, event.techList)
                }
            }
        }
    }

    // Animation
    val infiniteTransition = rememberInfiniteTransition(label = "nfc_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Wristband") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // NFC Status Card
            if (!isNfcSupported) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Nfc,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "NFC tidak tersedia di HP ini",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else if (!isNfcEnabled) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = WarningOrange.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Nfc,
                            contentDescription = null,
                            tint = WarningOrange
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "NFC Belum Aktif",
                                fontWeight = FontWeight.Bold,
                                color = WarningOrange
                            )
                            Text(
                                text = "Aktifkan NFC di Settings HP",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarningOrange.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated NFC Icon
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(if (isNfcEnabled && !uiState.isLoading) scale else 1f),
                contentAlignment = Alignment.Center
            ) {
                // Outer circle - pulse animation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = if (isNfcEnabled) PrimaryBlue.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                )
                // Inner circle
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(
                            color = if (isNfcEnabled) PrimaryBlue.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )
                // Icon
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = if (isNfcEnabled) PrimaryBlue else Color.Gray
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = when {
                    uiState.isLoading || uiState.isScanning -> "Mendeteksi Wristband..."
                    !isNfcSupported -> "NFC Tidak Tersedia"
                    !isNfcEnabled -> "NFC Belum Aktif"
                    else -> "Tap Wristband"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    uiState.isLoading -> "Mohon tunggu..."
                    !isNfcEnabled -> "Aktifkan NFC di Settings HP"
                    else -> "Dekatkan wristband ke bagian NFC HP"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Loading indicator
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(color = PrimaryBlue)
            }

            // Success indicator
            if (uiState.scanResult != null && !uiState.isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = SuccessGreen
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // Error/Not Found message with UID info
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Wristband Tidak Ditemukan",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Show the UID that was scanned
                        if (uiState.scannedUid != null) {
                            Text(
                                text = "UID yang terdeteksi:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.scannedUid ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Show tech type
                        if (uiState.scannedTechList != null) {
                            Text(
                                text = "Tipe Tag: ${uiState.scannedTechList}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Testing Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Testing Manual",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Klik tombol di bawah untuk testing:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick test buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "DEMO-UID-001" to "Budi",
                            "DEMO-UID-002" to "Ani",
                            "DEMO-UID-003" to "Dewi"
                        ).forEach { (uid, name) ->
                            OutlinedButton(
                                onClick = { viewModel.simulateScanWithUid(uid) },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isLoading
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.simulateScan() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Demo Wristband")
                    }
                }
            }
        }
    }
}
