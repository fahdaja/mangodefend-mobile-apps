package com.muhammadfahd0121.mangodefend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.muhammadfahd0121.mangodefend.domain.repository.ScanRepositoryImpl
import com.muhammadfahd0121.mangodefend.presentation.scanner.ScannerScreen
import com.muhammadfahd0121.mangodefend.presentation.scanner.ScannerViewModel
import com.muhammadfahd0121.mangodefend.sdk.MangoDefendClient
import com.muhammadfahd0121.mangodefend.ui.theme.MangodefendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Setup SDK (Single Instance)
        // Arahkan ke IP Laptop/Server Anda (JANGAN localhost/127.0.0.1 kalau di Emulator)
        // Gunakan 10.0.2.2 untuk emulator Android Studio
        val mangoSdk = MangoDefendClient(baseUrl = "http://10.183.244.156:8000")

        // 2. Masukkan SDK ke Repository
        val repository = ScanRepositoryImpl(sdk = mangoSdk)

        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ScannerViewModel(repository) as T
            }
        }

        val viewModel = ViewModelProvider(this, viewModelFactory)[ScannerViewModel::class.java]

        setContent {
            ScannerScreen(viewModel = viewModel)
        }
    }
}