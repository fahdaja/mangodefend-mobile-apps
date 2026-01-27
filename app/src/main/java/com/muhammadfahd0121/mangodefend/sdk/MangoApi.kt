package com.muhammadfahd0121.mangodefend.sdk
import com.muhammadfahd0121.mangodefend.sdk.model.ScanResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface MangoApi {
    // 1. Upload File
    @Multipart
    @POST("api/v1/scanning-file")
    suspend fun scanFile(@Part file: MultipartBody.Part): Response<ScanResponseDto>

    // 2. Cek Status (Polling)
    @GET("api/v1/status/{task_id}")
    suspend fun checkStatus(@Path("task_id") taskId: String): Response<ScanResponseDto>
}