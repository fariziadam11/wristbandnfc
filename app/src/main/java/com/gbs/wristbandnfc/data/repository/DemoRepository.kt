package com.gbs.wristbandnfc.data.repository

import com.gbs.wristbandnfc.data.api.ApiService
import com.gbs.wristbandnfc.data.model.*
import com.gbs.wristbandnfc.util.Result
import com.gbs.wristbandnfc.util.SessionManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Demo repository that provides mock data for testing without backend
 */
@Singleton
class DemoRepository @Inject constructor(
    private val sessionManager: SessionManager
) {
    private val demoCustomers = listOf(
        DemoCustomer(
            id = "cust-001",
            name = "Budi Santoso",
            uid = "DEMO-UID-001",
            token = "TKN-DEMO1",
            balance = 150000L,
            ticketStatus = "valid"
        ),
        DemoCustomer(
            id = "cust-002",
            name = "Ani Wijaya",
            uid = "DEMO-UID-002",
            token = "TKN-DEMO2",
            balance = 75000L,
            ticketStatus = "valid"
        ),
        DemoCustomer(
            id = "cust-003",
            name = "Dewi Lestari",
            uid = "DEMO-UID-003",
            token = "TKN-DEMO3",
            balance = 200000L,
            ticketStatus = "used"
        )
    )

    private val transactions = mutableListOf<Transaction>()

    suspend fun loginDemo(): Result<LoginResponse> {
        sessionManager.saveSession(
            token = "demo-token-123",
            userId = "petugas-001",
            userName = "Petugas Demo",
            userEmail = "petugas@demo.com"
        )
        return Result.Success(
            LoginResponse(
                token = "demo-token-123",
                user = User(
                    id = "petugas-001",
                    name = "Petugas Demo",
                    email = "petugas@demo.com",
                    role = "petugas",
                    createdAt = "2024-01-01T00:00:00Z"
                )
            )
        )
    }

    fun scanWristband(uid: String, token: String): Result<ScanWristbandResponse> {
        val customer = demoCustomers.find { it.uid == uid && it.token == token }
        return if (customer != null) {
            Result.Success(
                ScanWristbandResponse(
                    wristbandId = customer.id,
                    uid = customer.uid,
                    token = customer.token,
                    status = "active",
                    customer = Customer(id = customer.id, name = customer.name),
                    wallet = Wallet(id = "wallet-${customer.id}", balance = customer.balance, currency = "IDR"),
                    activeTickets = listOf(
                        Ticket(
                            id = "ticket-001",
                            ticketType = "1-day-pass",
                            description = "Tiket masuk 1 hari",
                            price = 50000,
                            status = customer.ticketStatus,
                            validFrom = "2024-01-01T00:00:00Z",
                            validUntil = "2024-12-31T23:59:59Z",
                            usedAt = if (customer.ticketStatus == "used") "2024-01-01T10:00:00Z" else null
                        )
                    )
                )
            )
        } else {
            Result.Error("Wristband tidak ditemukan")
        }
    }

    fun payment(uid: String, token: String, amount: Long): Result<PaymentResponse> {
        val customer = demoCustomers.find { it.uid == uid && it.token == token }
        return when {
            customer == null -> Result.Error("Wristband tidak ditemukan")
            customer.balance < amount -> Result.Error("Saldo tidak mencukupi")
            else -> {
                val newBalance = customer.balance - amount
                val transactionId = "txn-${System.currentTimeMillis()}"
                transactions.add(
                    Transaction(
                        id = transactionId,
                        type = "payment",
                        amount = amount,
                        balanceBefore = customer.balance,
                        balanceAfter = newBalance,
                        referenceId = "REF-${System.currentTimeMillis()}",
                        status = "success",
                        createdAt = java.time.Instant.now().toString()
                    )
                )
                Result.Success(
                    PaymentResponse(
                        transactionId = transactionId,
                        status = "success",
                        previousBalance = customer.balance,
                        amount = amount,
                        newBalance = newBalance
                    )
                )
            }
        }
    }

    fun topup(uid: String, token: String, amount: Long): Result<TopupResponse> {
        val customer = demoCustomers.find { it.uid == uid && it.token == token }
        return when {
            customer == null -> Result.Error("Wristband tidak ditemukan")
            else -> {
                val newBalance = customer.balance + amount
                val transactionId = "txn-${System.currentTimeMillis()}"
                transactions.add(
                    Transaction(
                        id = transactionId,
                        type = "topup",
                        amount = amount,
                        balanceBefore = customer.balance,
                        balanceAfter = newBalance,
                        referenceId = null,
                        status = "success",
                        createdAt = java.time.Instant.now().toString()
                    )
                )
                Result.Success(
                    TopupResponse(
                        transactionId = transactionId,
                        wristbandId = customer.id,
                        previousBalance = customer.balance,
                        amount = amount,
                        newBalance = newBalance
                    )
                )
            }
        }
    }

    fun validateTicket(uid: String, token: String): Result<ValidateTicketResponse> {
        val customer = demoCustomers.find { it.uid == uid && it.token == token }
        return when {
            customer == null -> Result.Error("Wristband tidak ditemukan")
            customer.ticketStatus == "used" -> Result.Success(
                ValidateTicketResponse(
                    result = "already_used",
                    ticketType = "1-day-pass",
                    validUntil = "2024-12-31T23:59:59Z",
                    usedAt = "2024-01-01T10:00:00Z",
                    message = "Tiket sudah digunakan"
                )
            )
            else -> Result.Success(
                ValidateTicketResponse(
                    result = "valid",
                    ticketType = "1-day-pass",
                    validUntil = "2024-12-31T23:59:59Z",
                    usedAt = null,
                    message = "Tiket valid"
                )
            )
        }
    }

    fun getTransactions(wristbandId: String): Result<List<Transaction>> {
        val wristbandTransactions = transactions.filter { it.id.contains(wristbandId) }
        return Result.Success(wristbandTransactions)
    }

    fun registerWristband(customerName: String): Result<RegisterWristbandResponse> {
        val newId = "cust-${System.currentTimeMillis()}"
        val newUid = "UID-${System.currentTimeMillis().toString().take(8)}"
        val newToken = "TKN-${System.currentTimeMillis().toString().take(6).uppercase()}"
        return Result.Success(
            RegisterWristbandResponse(
                id = newId,
                uid = newUid,
                token = newToken,
                customerId = newId,
                customerName = customerName,
                registeredBy = "petugas-001"
            )
        )
    }
}

data class DemoCustomer(
    val id: String,
    val name: String,
    val uid: String,
    val token: String,
    val balance: Long,
    val ticketStatus: String
)
