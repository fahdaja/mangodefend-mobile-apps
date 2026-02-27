package com.muhammadfahd0121.mangodefend.sdk.model
import com.google.gson.annotations.SerializedName

data class ScanResponseDto(
    @SerializedName("status") val status: String?, // "queued", "processing", "completed"
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ScanDataDto?
)

// Ini isi dalam object "data"
data class ScanDataDto(
    // Saat Upload (Queued)
    @SerializedName("task_id") val taskId: String?,
    @SerializedName("check_status_url") val checkStatusUrl: String?,
    @SerializedName("device_id") val deviceId: String?,
    @SerializedName("device_type") val deviceType: String?,
    // Saat Completed (Hasil Scan)
    @SerializedName("filename") val fileName: String?,
    @SerializedName("prediction") val prediction: String?,
    @SerializedName("file_size") val fileSize: Long?
)