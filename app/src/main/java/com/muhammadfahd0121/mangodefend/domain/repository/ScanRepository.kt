package com.muhammadfahd0121.mangodefend.domain.repository

import com.muhammadfahd0121.mangodefend.domain.model.ScanResult
import java.io.File

interface ScanRepository {
    // Fungsi suspend untuk operasi async (misal ke API)
    suspend fun scanFile(file: File, originalFileName: String): Result<ScanResult>
    suspend fun checkScanStatus(taskId: String): Result<ScanResult>
}