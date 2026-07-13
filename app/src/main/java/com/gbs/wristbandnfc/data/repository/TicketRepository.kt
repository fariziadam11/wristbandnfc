package com.gbs.wristbandnfc.data.repository

import com.gbs.wristbandnfc.data.api.ApiService
import com.gbs.wristbandnfc.data.model.*
import com.gbs.wristbandnfc.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun validateTicket(uid: String, token: String): Result<ValidateTicketResponse> {
        return try {
            val response = apiService.validateTicket(ValidateTicketRequest(uid, token))
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) {
                    Result.Success(data)
                } else {
                    Result.Error("Failed to validate ticket")
                }
            } else {
                Result.Error(response.body()?.error ?: "Failed to validate ticket")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getTickets(wristbandId: String): Result<List<Ticket>> {
        return try {
            val response = apiService.getTickets(wristbandId)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.Success(data)
                } else {
                    Result.Success(emptyList())
                }
            } else {
                Result.Error(response.body()?.error ?: "Failed to get tickets")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
}
