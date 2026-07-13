package com.gbs.wristbandnfc.data.repository

import com.gbs.wristbandnfc.data.api.ApiService
import com.gbs.wristbandnfc.data.model.*
import com.gbs.wristbandnfc.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun topup(uid: String, token: String, amount: Long): Result<TopupResponse> {
        return try {
            val response = apiService.topup(TopupRequest(uid, token, amount))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.Success(data)
                } else {
                    Result.Error("Failed to top up")
                }
            } else {
                Result.Error(response.body()?.error ?: "Failed to top up")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun payment(uid: String, token: String, amount: Long, referenceId: String? = null): Result<PaymentResponse> {
        return try {
            val response = apiService.payment(PaymentRequest(uid, token, amount, referenceId))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.Success(data)
                } else {
                    Result.Error("Payment failed")
                }
            } else {
                val errorBody = response.body()
                if (errorBody?.error == "insufficient_balance") {
                    Result.Error("Insufficient balance")
                } else {
                    Result.Error(errorBody?.error ?: "Payment failed")
                }
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getWallet(wristbandId: String): Result<Wallet> {
        return try {
            val response = apiService.getWallet(wristbandId)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.Success(data)
                } else {
                    Result.Error("Failed to get wallet")
                }
            } else {
                Result.Error(response.body()?.error ?: "Failed to get wallet")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getTransactions(wristbandId: String): Result<List<Transaction>> {
        return try {
            val response = apiService.getTransactions(wristbandId)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.Success(data.transactions)
                } else {
                    Result.Success(emptyList())
                }
            } else {
                Result.Error(response.body()?.error ?: "Failed to get transactions")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
}
