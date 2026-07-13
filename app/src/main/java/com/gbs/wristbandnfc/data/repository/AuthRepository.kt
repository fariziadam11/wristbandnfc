package com.gbs.wristbandnfc.data.repository

import com.gbs.wristbandnfc.data.api.ApiService
import com.gbs.wristbandnfc.data.model.*
import com.gbs.wristbandnfc.util.Result
import com.gbs.wristbandnfc.util.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    sessionManager.saveSession(
                        token = data.token,
                        userId = data.user.id,
                        userName = data.user.name,
                        userEmail = data.user.email
                    )
                    Result.Success(data)
                } else {
                    Result.Error("Login failed: No data received")
                }
            } else {
                Result.Error(response.body()?.error ?: "Login failed")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getProfile(): Result<User> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.Success(data)
                } else {
                    Result.Error("Failed to get profile")
                }
            } else {
                Result.Error(response.body()?.error ?: "Failed to get profile")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }
}
