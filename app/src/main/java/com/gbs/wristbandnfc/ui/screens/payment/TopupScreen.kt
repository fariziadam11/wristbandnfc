package com.gbs.wristbandnfc.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gbs.wristbandnfc.ui.screens.wallet.formatCurrency
import com.gbs.wristbandnfc.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopupScreen(
    uid: String,
    token: String,
    onTopupSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: TopupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var amount by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onTopupSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Top Up Saldo") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = SuccessGreen.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.AddCard,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tambah Saldo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Top up wallet wristband",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        amount = newValue
                    }
                },
                label = { Text("Nominal (IDR)") },
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Quick amounts
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(50000, 100000, 200000, 500000).forEach { quickAmount ->
                    FilterChip(
                        selected = amount == quickAmount.toString(),
                        onClick = { amount = quickAmount.toString() },
                        label = { Text(formatCurrency(quickAmount.toLong()).replace(",00", "")) }
                    )
                }
            }

            // Error message
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
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
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Top Up Button
            Button(
                onClick = {
                    val amountLong = amount.toLongOrNull() ?: 0
                    viewModel.makeTopup(uid, token, amountLong)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                enabled = !uiState.isLoading && amount.isNotEmpty() && (amount.toLongOrNull() ?: 0) > 0
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Top Up ${if (amount.isNotEmpty()) formatCurrency(amount.toLongOrNull() ?: 0) else "Rp 0"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
