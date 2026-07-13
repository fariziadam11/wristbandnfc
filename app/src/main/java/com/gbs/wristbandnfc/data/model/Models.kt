package com.gbs.wristbandnfc.data.model

import com.google.gson.annotations.SerializedName

// API Response wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: String?,
    val code: String?
)

// Auth
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    @SerializedName("created_at") val createdAt: String
)

// Wristband
data class RegisterWristbandRequest(
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("customer_email") val customerEmail: String? = null
)

data class RegisterWristbandResponse(
    val id: String,
    val uid: String,
    val token: String,
    @SerializedName("customer_id") val customerId: String,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("registered_by") val registeredBy: String
)

data class WriteWristbandRequest(
    val uid: String
)

data class WriteWristbandResponse(
    val uid: String,
    val token: String,
    val message: String
)

data class ScanWristbandRequest(
    val uid: String,
    val token: String
)

data class ScanWristbandResponse(
    @SerializedName("wristband_id") val wristbandId: String,
    val uid: String,
    val token: String,
    val status: String,
    val customer: Customer,
    val wallet: Wallet,
    @SerializedName("active_tickets") val activeTickets: List<Ticket>? = null
)

data class Customer(
    val id: String,
    val name: String
)

// Wallet
data class Wallet(
    val id: String,
    val balance: Long,
    val currency: String
)

data class TopupRequest(
    val uid: String,
    val token: String,
    val amount: Long
)

data class TopupResponse(
    @SerializedName("transaction_id") val transactionId: String,
    @SerializedName("wristband_id") val wristbandId: String,
    @SerializedName("previous_balance") val previousBalance: Long,
    val amount: Long,
    @SerializedName("new_balance") val newBalance: Long
)

// Payment
data class PaymentRequest(
    val uid: String,
    val token: String,
    val amount: Long,
    @SerializedName("reference_id") val referenceId: String? = null
)

data class PaymentResponse(
    @SerializedName("transaction_id") val transactionId: String,
    val status: String,
    @SerializedName("previous_balance") val previousBalance: Long,
    val amount: Long,
    @SerializedName("new_balance") val newBalance: Long
)

// Ticket
data class Ticket(
    val id: String,
    @SerializedName("ticket_type") val ticketType: String,
    val description: String?,
    val price: Long,
    val status: String,
    @SerializedName("valid_from") val validFrom: String,
    @SerializedName("valid_until") val validUntil: String?,
    @SerializedName("used_at") val usedAt: String?
)

data class ValidateTicketRequest(
    val uid: String,
    val token: String
)

data class ValidateTicketResponse(
    val result: String,
    @SerializedName("ticket_type") val ticketType: String?,
    @SerializedName("valid_until") val validUntil: String?,
    @SerializedName("used_at") val usedAt: String?,
    val message: String
)

// Transaction
data class Transaction(
    val id: String,
    val type: String,
    val amount: Long,
    @SerializedName("balance_before") val balanceBefore: Long,
    @SerializedName("balance_after") val balanceAfter: Long,
    @SerializedName("reference_id") val referenceId: String?,
    val status: String,
    @SerializedName("created_at") val createdAt: String
)

data class TransactionListResponse(
    val transactions: List<Transaction>,
    val total: Long
)
