package com.muhammadfahd0121.mangodefend.presentation.scanner

import java.io.File

sealed class ScannerEvent {
    // Ubah dari String menjadi File
    data class UploadFile(val file: File, val originalName: String) : ScannerEvent()

    object ClearHistory : ScannerEvent()
    object DismissError : ScannerEvent()
}