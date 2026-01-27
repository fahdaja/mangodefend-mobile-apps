package com.muhammadfahd0121.mangodefend.domain.model

data class ScanResult(
    val id: String,         // Tambahin ini kalau belum ada
    val fileName: String,
    val fileSize: String,    // Tambahin ini buat info size
    val prediction: String, // Buat nampilin teks "Malware"/"Benign"
    val isMalware: Boolean, // Buat nentuin warna Merah/Hijau
    val isCached: Boolean = false,
    val isHighlighted: Boolean = false
)
