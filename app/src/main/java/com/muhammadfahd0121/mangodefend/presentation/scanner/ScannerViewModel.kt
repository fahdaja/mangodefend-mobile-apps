package com.muhammadfahd0121.mangodefend.presentation.scanner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhammadfahd0121.mangodefend.domain.model.ScanResult
import com.muhammadfahd0121.mangodefend.domain.repository.ScanRepository
import kotlinx.coroutines.delay // PENTING: Buat nunggu jeda polling
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.UUID

class ScannerViewModel(
    private val repository: ScanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScannerState())
    val state: StateFlow<ScannerState> = _state.asStateFlow()

    fun onEvent(event: ScannerEvent) {
        when (event) {
            is ScannerEvent.UploadFile -> scanFile(event.file, event.originalName)
            is ScannerEvent.ClearHistory -> {
                _state.update { it.copy(scanResults = emptyList()) }
            }
            is ScannerEvent.DismissError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    fun onFileSelectionError() {
        _state.update {
            it.copy(errorMessage = "Gagal membaca file.")
        }
    }

    private fun scanFile(file: File, originalName: String) {
        viewModelScope.launch {

            // CEK DUPLIKASI PAKE NAMA ASLI
            val duplicateByName = _state.value.scanResults.find { it.fileName == originalName }

            if (duplicateByName != null) {
                triggerHighlightEffect(duplicateByName.id, "Info: File '$originalName' sudah ada di list.")
                return@launch
            }

            // BUAT KARTU SEMENTARA (PAKE NAMA ASLI)
            val tempId = UUID.randomUUID().toString()
            val tempResult = ScanResult(
                id = tempId,
                fileName = originalName, // <-- TAMPIL DI UI SEBAGAI NAMA ASLI
                fileSize = "Calculating...", // Nanti diupdate server
                isMalware = false,
                prediction = "Uploading...",
                isCached = false,
                isHighlighted = false
            )

            _state.update {
                it.copy(
                    isLoading = true,
                    scanResults = it.scanResults + tempResult
                )
            }

            Log.d("MangoDebug", "Mulai upload: ${file.name} (Temp ID: $tempId)")

            try {
                val result = repository.scanFile(file, originalName, deviceId = 2.toString())

                result.fold(
                    onSuccess = { serverData ->

                        // --- LAPIS 2: CEK DUPLIKASI ID DARI SERVER ---
                        // Ini terjadi jika user rename file (Nama beda) tapi isinya sama (Hash sama)

                        // Cari item lain (bukan tempId) yang ID-nya sama dengan server
                        val duplicateById = _state.value.scanResults
                            .filter { it.id != tempId }
                            .find { it.id == serverData.id }

                        if (duplicateById != null) {
                            Log.d("MangoDebug", "DUPLIKAT ID SERVER! Hapus Temp Card.")

                            // 1. Hapus kartu temporary (Rollback)
                            _state.update { it.copy(scanResults = it.scanResults.filter { item -> item.id != tempId }) }

                            // 2. Highlight kartu asli yang sudah ada
                            triggerHighlightEffect(duplicateById.id, "File ini sudah ada.")

                            return@fold
                        }

                        // 3. UPDATE KARTU TEMPORARY (Jika aman/tidak duplikat)
                        Log.d("MangoDebug", "Aman. Update Temp ID ($tempId) -> Server ID (${serverData.id})")

                        _state.update { currentState ->
                            val updatedList = currentState.scanResults.map {
                                // Ganti data kartu tempId dengan data server
                                if (it.id == tempId) serverData else it
                            }
                            currentState.copy(scanResults = updatedList)
                        }

                        // 4. LANJUT POLLING
                        startPolling(serverData.id)
                    },
                    onFailure = { exception ->
                        // Hapus kartu temporary kalau gagal
                        _state.update { it.copy(scanResults = it.scanResults.filter { item -> item.id != tempId }) }
                        handleError(exception)
                    }
                )
            } catch (e: Exception) {
                // Hapus kartu temporary kalau error
                _state.update { it.copy(scanResults = it.scanResults.filter { item -> item.id != tempId }) }
                handleError(e)
            }
        }
    }

    // --- FUNGSI KHUSUS ANIMASI HIGHLIGHT ---
    // Dipisah biar kodingan di atas rapi
    private suspend fun triggerHighlightEffect(targetId: String, message: String) {
        // 1. Tampilkan Pesan & Nyalakan Highlight
        _state.update { state ->
            val highlightedList = state.scanResults.map { item ->
                if (item.id == targetId) item.copy(isHighlighted = true) else item
            }

            state.copy(
                isLoading = false,
                errorMessage = message, // TAMPILKAN PESAN
                scanResults = highlightedList
            )
        }

        // 2. TUNGGU SEBENTAR (Misal 2 Detik)
        // Kita beri waktu 2 detik: 1 detik buat animasi kedip, 1 detik lagi buat baca teksnya.
        delay(2000)

        // 3. Hapus Pesan & Matikan Highlight (RESET)
        _state.update { state ->
            val resetList = state.scanResults.map { item ->
                if (item.id == targetId) item.copy(isHighlighted = false) else item
            }

            state.copy(
                errorMessage = null, // HAPUS PESAN OTOMATIS (Auto-dismiss)
                scanResults = resetList
            )
        }
    }

    private suspend fun startPolling(taskId: String) {
        var isFinished = false
        var attempt = 0
        val maxAttempts = 20

        while (!isFinished && attempt < maxAttempts) {
            delay(if (taskId.startsWith("db_")) 500 else 2000)
            val pollingResult = repository.checkScanStatus(taskId)

            pollingResult.onSuccess { finalData ->
                if (finalData.prediction != "Scanning..." && finalData.prediction != "Uploading...") {
                    isFinished = true
                    Log.d("MangoDebug", "Polling Selesai: ${finalData.prediction}")

                    _state.update { currentState ->
                        val updatedList = currentState.scanResults.map {
                            if (it.id == taskId) finalData else it
                        }
                        currentState.copy(isLoading = false, scanResults = updatedList)
                    }
                }
            }
            attempt++
        }
        if (!isFinished) _state.update { it.copy(isLoading = false) }
    }

    private fun handleError(exception: Throwable) {
        val msg = when (exception) {
            is SocketTimeoutException -> "Koneksi Timeout."
            is IOException -> "Koneksi Gagal."
            else -> "Error: ${exception.localizedMessage}"
        }
        _state.update { it.copy(isLoading = false, errorMessage = msg) }
    }
}