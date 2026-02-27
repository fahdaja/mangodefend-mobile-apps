package com.muhammadfahd0121.mangodefend.domain.repository

import com.muhammadfahd0121.mangodefend.domain.model.ScanResult
import com.muhammadfahd0121.mangodefend.sdk.MangoDefendClient
import com.muhammadfahd0121.mangodefend.sdk.model.ScanResponseDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ScanRepositoryImpl(
    private val sdk: MangoDefendClient
) : ScanRepository {

    override suspend fun scanFile(
        file: File,
        originalFileName: String,
        deviceId: String,
    ): Result<ScanResult> {

        // 1. Siapkan File (Dengan Trik Nama Asli)
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", originalFileName, requestFile)

        // 2. Siapkan Device Info (Convert String ke RequestBody)
        // Ini yang KETINGGALAN di kode Anda sebelumnya
        val deviceId = deviceId.toRequestBody("text/plain".toMediaTypeOrNull())
        val deviceType = "android".toRequestBody("text/plain".toMediaTypeOrNull())

        // 3. Kirim ke SDK (Pastikan Interface SDK sudah update menerima 3 parameter)
        return sdk.scanFile(body, deviceId, deviceType).mapCatching { responseDto ->
            val serverId = responseDto.data?.taskId ?: ""
            mapToDomain(responseDto, serverId, originalFileName)
        }
    }

    override suspend fun checkScanStatus(taskId: String): Result<ScanResult> {
        return sdk.checkStatus(taskId).mapCatching { responseDto ->
            // Paksa pakai taskId yang kita request (Preventing null ID issue)
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
            id = forcedId,
            fileName = innerData?.fileName ?: fileNameFallback,
            fileSize = "${(innerData?.fileSize ?: 0) / 1024} KB",
            isMalware = isMalwareDetected,
            prediction = displayText,
            isCached = isCachedResult,
            isHighlighted = false
        )
    }
}