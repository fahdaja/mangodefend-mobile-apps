package com.muhammadfahd0121.mangodefend.domain.repository

import com.muhammadfahd0121.mangodefend.domain.model.ScanResult
import com.muhammadfahd0121.mangodefend.sdk.MangoDefendClient
import com.muhammadfahd0121.mangodefend.sdk.model.ScanResponseDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.mapCatching

// ... imports

class ScanRepositoryImpl(
    private val sdk: MangoDefendClient
) : ScanRepository {

    override suspend fun scanFile(file: File, originalFileName: String): Result<ScanResult> {
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        // --- TRIK UTAMA ADA DI SINI ---
        // Parameter ke-2 adalah 'filename' yang akan dibaca oleh Server/Database.
        // Kita masukkan nama asli (misal: "Skripsi.pdf"), BUKAN file.name ("temp_123.tmp")
        val body = MultipartBody.Part.createFormData("file", originalFileName, requestFile)

        return sdk.scanFile(body).mapCatching { responseDto ->
            val serverId = responseDto.data?.taskId ?: ""
            // Mapping balik juga pake nama asli
            mapToDomain(responseDto, serverId, originalFileName)
        }
    }

    override suspend fun checkScanStatus(taskId: String): Result<ScanResult> {
        return sdk.checkStatus(taskId).mapCatching { responseDto ->
            // Paksa pakai taskId yang kita request
            val finalId = responseDto.data?.taskId ?: taskId
            mapToDomain(responseDto, finalId, "Unknown File")
        }
    }

    private fun mapToDomain(
        dtoWrapper: ScanResponseDto,
        forcedId: String,
        fileNameFallback: String
    ): ScanResult {
        val innerData = dtoWrapper.data
        val status = dtoWrapper.status ?: "unknown"
        val isCachedResult = status == "completed_cached"

        val rawPrediction = innerData?.prediction
        val displayText = rawPrediction ?: "Scanning..."
        val isMalwareDetected = rawPrediction?.equals("Malware", ignoreCase = true) == true

        return ScanResult(
            id = forcedId, // ID dari server (db_18)
            fileName = innerData?.fileName ?: fileNameFallback,
            fileSize = "${(innerData?.fileSize ?: 0) / 1024} KB",
            isMalware = isMalwareDetected,
            prediction = displayText,
            isCached = isCachedResult,
            isHighlighted = false
        )
    }
}