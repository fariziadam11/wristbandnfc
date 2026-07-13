package com.gbs.wristbandnfc.data.repository

import com.gbs.wristbandnfc.data.api.ApiService
import com.gbs.wristbandnfc.data.model.*
import com.gbs.wristbandnfc.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WristbandRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun registerWristband(customerName: String, customerEmail: String? = null): Result<RegisterWristbandResponse> {
        return try {
            val response = apiService.registerWristband(
                RegisterWristbandRequest(customerName, customerEmail)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.Success(data)
                } else {
                    Result.Error("Failed to register wristband")
                }
            } else {
                Result.Error(response.body()?.error ?: "Failed to register wristband")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun writeWristband(uid: String): Result<WriteWristbandResponse> {
        return try {
            val response = apiService.writeWristband(WriteWristbandRequest(uid))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.Success(data)
                } else {
                    Result.Error("Failed to write wristband")
                }
            } else {
                Result.Error(response.body()?.error ?: "Failed to write wristband")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun scanWristband(uid: String, token: String): Result<ScanWristbandResponse> {
        return try {
            val response = apiService.scanWristband(ScanWristbandRequest(uid, token))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.Success(data)
                } else {
                    Result.Error("Failed to scan wristband")
                }
            } else {
                Result.Error(response.body()?.error ?: "Wristband not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
}
