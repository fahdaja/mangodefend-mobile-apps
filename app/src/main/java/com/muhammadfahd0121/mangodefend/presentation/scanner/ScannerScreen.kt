package com.muhammadfahd0121.mangodefend.presentation.scanner

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Pastikan import ini sesuai package Anda
import com.muhammadfahd0121.mangodefend.core.common.FileUtils
import com.muhammadfahd0121.mangodefend.presentation.scanner.components.EmptyStateView
import com.muhammadfahd0121.mangodefend.presentation.scanner.components.ScanResultCard
import com.muhammadfahd0121.mangodefend.presentation.scanner.components.ScanningAnimation
import com.muhammadfahd0121.mangodefend.presentation.scanner.components.UploadArea

// Warna Custom
val ResultBoxColor = Color(0xFFFFFFFF)
val CreamBg = Color(0xFFFFFDF5)

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // 1. Ambil File Fisik (Temp)
            val file = FileUtils.getFileFromUri(context, selectedUri)

            // 2. Ambil Nama Asli (String)
            val realName = FileUtils.getFileNameFromUri(context, selectedUri)

            if (file != null) {
                // Kirim keduanya ke ViewModel
                viewModel.onEvent(ScannerEvent.UploadFile(file, realName))
            } else {
                Toast.makeText(context, "Gagal mengambil file.", Toast.LENGTH_SHORT).show()
                viewModel.onFileSelectionError()
            }
        }
    }

    Scaffold(
        containerColor = CreamBg
    ) { paddingValues ->

        // --- ROOT BOX ---
        // Kita pakai Box agar bisa menumpuk Pesan Error di atas List
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // LAYER 1: KONTEN UTAMA (Upload & List)
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // A. Bagian Header & Upload
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MangoDefend Scanner",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.isLoading) {
                        // Tampilkan animasi scanning (Radar)
                        ScanningAnimation(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .height(150.dp) // Pastikan tingginya pas
                                .fillMaxWidth()
                        )

                        // Opsional: Kasih teks status di bawah animasi biar informatif
                        Text(
                            text = "AI Scanning...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        // Tampilkan tombol upload kalau sedang tidak loading
                        UploadArea(
                            onUploadClick = { launcher.launch("*/*") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // B. Bagian Result (Box Putih Melengkung)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Mengisi sisa layar ke bawah
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                    color = ResultBoxColor
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                    ) {
                        // Header List + Clear Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scan Results (${state.scanResults.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A237E)
                            )

                            if (state.scanResults.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(ScannerEvent.ClearHistory) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Clear",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ISI LIST (LAZY COLUMN)
                        if (state.scanResults.isEmpty() && !state.isLoading) {
                            EmptyStateView() // Tampilkan gambar kosong jika list kosong
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 80.dp) // Padding bawah biar gak ketutup pesan error
                            ) {
                                items(
                                    items = state.scanResults.reversed(), // Yang baru di atas
                                    key = { it.id } // Penting buat animasi highlight
                                ) { result ->
                                    ScanResultCard(data = result)
                                }
                            }
                        }
                    }
                }
            }


            // LAYER 2: PESAN ERROR / INFO (FLOATING DI ATAS)
            // Ini ditaruh di luar Column, tapi di dalam Box utama
            AnimatedVisibility(
                visible = state.errorMessage != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter) // POSISI DI BAWAH TENGAH LAYAR
                    .padding(bottom = 32.dp, start = 24.dp, end = 24.dp), // Jarak aman

                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    color = Color(0xFF333333),
                    shape = RoundedCornerShape(50), // Pill Shape
                    shadowElevation = 8.dp,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = state.errorMessage ?: "",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}