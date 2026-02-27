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

        val mangoSdk = MangoDefendClient(baseUrl = "http://10.117.236.156:8000")
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