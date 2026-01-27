package com.muhammadfahd0121.mangodefend.presentation.scanner

import com.muhammadfahd0121.mangodefend.domain.model.ScanResult

data class ScannerState(
    val isLoading: Boolean = false,
    val scanResults: List<ScanResult> = emptyList(),
    val errorMessage: String? = null
)