package com.gbs.wristbandnfc.ui.screens.scan

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gbs.wristbandnfc.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onScanSuccess: (uid: String, token: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle scan result
    LaunchedEffect(uiState.scanResult) {
        uiState.scanResult?.let { result ->
            onScanSuccess(result.first, result.second)
        }
    }

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
            // Animated NFC Icon
            val infiniteTransition = rememberInfiniteTransition(label = "nfc_animation")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale),
            ) {
                // Outer circle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = PrimaryBlue.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                )
                // Inner circle
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(
                            color = PrimaryBlue.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                )
                // Icon
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = PrimaryBlue
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
                text = if (uiState.isScanning) "Mendeteksi Wristband..." else "Tap Wristband",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dekatkan wristband ke bagian NFC HP",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Loading indicator
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator()
            }

            // Error message
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(24.dp))
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
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Manual NFC ID input for testing
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = { viewModel.simulateScan() },
                enabled = !uiState.isLoading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulate NFC Scan (Demo)")
            }
        }
    }
}
