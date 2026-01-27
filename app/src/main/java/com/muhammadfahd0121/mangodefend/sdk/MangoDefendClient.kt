package com.muhammadfahd0121.mangodefend.sdk
import com.muhammadfahd0121.mangodefend.sdk.model.ScanResponseDto
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MangoDefendClient(
    private val baseUrl: String,
    private val apiKey: String? = null // Persiapan kalau nanti butuh API Key
) {
    // Private variable biar ga diakses dari luar
    private val api: MangoApi

    init {
        // 1. Setup Logging (Biar kelihatan request/response di Logcat)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // 2. Setup Client (Timeout, Header, dll)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // Upload file butuh waktu lama
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                // Otomatis nambahin API Key ke setiap request (jika ada)
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                if (apiKey != null) {
                    requestBuilder.header("Authorization", "Bearer $apiKey")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()

        // 3. Setup Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(MangoApi::class.java)
    }

    // --- PUBLIC FUNCTIONS (Ini yang dipanggil Mobile App) ---

    // Mengembalikan Result<T> agar mobile app tahu sukses/gagal
    suspend fun scanFile(filePart: MultipartBody.Part): Result<ScanResponseDto> {
        return try {
            val response = api.scanFile(filePart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Parse error body jika perlu
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun checkStatus(taskId: String): Result<ScanResponseDto> {
        return try {
            // Memanggil endpoint @GET di MangoApi
            val response = api.checkStatus(taskId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Check Status Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

