package com.muhammadfahd0121.mangodefend.presentation.scanner.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Warna Constants ---
@Composable
fun ScanningAnimation(
    modifier: Modifier = Modifier
) {
    // 1. ANIMASI RADAR (Tetap Loop / Infinite biar terlihat bekerja)
    val infiniteTransition = rememberInfiniteTransition(label = "RadarAnim")

    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ),
        label = "Rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    // 2. LOGIKA PERSENAN PINTAR (One-Shot / Tidak Loop)
    // Kita pakai Animatable, bukan InfiniteTransition
    val progressAnim = remember { Animatable(0f) }

    // Jalankan skenario animasi saat komponen muncul
    LaunchedEffect(Unit) {
        // Fase 1: Cepat (0 -> 65% dalam 1.5 detik) - Anggap ini Uploading
        progressAnim.animateTo(
            targetValue = 65f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )

        // Fase 2: Sedang (65% -> 85% dalam 2 detik) - Anggap ini Processing
        progressAnim.animateTo(
            targetValue = 85f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )

        // Fase 3: Lambat Sekali (85% -> 99% dalam 15 detik) - Menunggu Polling
        // Dia akan pelan banget sampe server selesai. Kalau server selesai duluan,
        // komponen ini bakal hilang dari layar (karena isLoading = false), jadi aman.
        progressAnim.animateTo(
            targetValue = 99f,
            animationSpec = tween(durationMillis = 15000, easing = LinearOutSlowInEasing)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(180.dp)
        ) {
            // LAYER A: Lingkaran Pulse
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(MangoOrange.copy(alpha = 0.1f))
            )

            // LAYER B: Radar Sweep
            Canvas(modifier = Modifier.size(160.dp)) {
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    style = Stroke(width = 2.dp.toPx())
                )
                rotate(degrees = angle) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                MangoOrange.copy(alpha = 0.2f),
                                MangoOrange
                            )
                        ),
                        radius = size.minDimension / 2
                    )
                }
            }

            // LAYER C: Info Tengah
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(4.dp, Brush.linearGradient(listOf(MangoOrange, MangoGreen)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // TAMPILKAN VALUE DARI ANIMATABLE (Diubah jadi Int biar gak ada koma)
                    Text(
                        text = "${progressAnim.value.toInt()}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MangoOrange,
                        fontSize = 36.sp
                    )

                    Text(
                        text = "Scanning...",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}