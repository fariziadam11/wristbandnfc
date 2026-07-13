package com.gbs.wristbandnfc.data.api

import com.gbs.wristbandnfc.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @GET("profile/me")
    suspend fun getProfile(): Response<ApiResponse<User>>

    // Wristband
    @POST("wristbands/register")
    suspend fun registerWristband(@Body request: RegisterWristbandRequest): Response<ApiResponse<RegisterWristbandResponse>>

    @POST("wristbands/write")
    suspend fun writeWristband(@Body request: WriteWristbandRequest): Response<ApiResponse<WriteWristbandResponse>>

    @POST("wristbands/scan")
    suspend fun scanWristband(@Body request: ScanWristbandRequest): Response<ApiResponse<ScanWristbandResponse>>

    @GET("wristbands/{id}")
    suspend fun getWristband(@Path("id") id: String): Response<ApiResponse<ScanWristbandResponse>>

    // Wallet
    @POST("wallet/topup")
    suspend fun topup(@Body request: TopupRequest): Response<ApiResponse<TopupResponse>>

    @GET("wallet/{id}")
    suspend fun getWallet(@Path("id") id: String): Response<ApiResponse<Wallet>>

    // Payment
    @POST("payments")
    suspend fun payment(@Body request: PaymentRequest): Response<ApiResponse<PaymentResponse>>

    // Transactions
    @GET("transactions")
    suspend fun getTransactions(@Query("wristband_id") wristbandId: String): Response<ApiResponse<TransactionListResponse>>

    // Tickets
    @POST("tickets/validate")
    suspend fun validateTicket(@Body request: ValidateTicketRequest): Response<ApiResponse<ValidateTicketResponse>>

    @GET("tickets/{id}")
    suspend fun getTickets(@Path("id") id: String): Response<ApiResponse<List<Ticket>>>
}
