package com.gbs.wristbandnfc.ui.screens.ticket

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gbs.wristbandnfc.ui.theme.SuccessGreen
import com.gbs.wristbandnfc.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidateTicketScreen(
    uid: String,
    token: String,
    onNavigateBack: () -> Unit,
    viewModel: ValidateTicketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uid, token) {
        viewModel.validateTicket(uid, token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Validasi Tiket") },
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
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Memvalidasi tiket...")
                }
                uiState.result != null -> {
                    val result = uiState.result!!

                    val (icon, color, title) = when (result.result) {
                        "valid" -> Triple(Icons.Default.CheckCircle, SuccessGreen, "Tiket Valid!")
                        "already_used" -> Triple(Icons.Default.Cancel, WarningOrange, "Sudah Digunakan")
                        "expired" -> Triple(Icons.Default.AccessTime, MaterialTheme.colorScheme.error, "Tiket Expired")
                        else -> Triple(Icons.Default.Error, MaterialTheme.colorScheme.error, "Error")
                    }

                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = color.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = color
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = result.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (result.ticketType != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tipe Tiket", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        result.ticketType ?: "-",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (result.usedAt != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Digunakan Pada", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            result.usedAt?.take(19)?.replace("T", " ") ?: "-",
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                uiState.error != null -> {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { viewModel.validateTicket(uid, token) }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Coba Lagi")
                    }
                }
            }
        }
    }
}
