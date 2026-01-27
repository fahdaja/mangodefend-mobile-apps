package com.muhammadfahd0121.mangodefend.presentation.scanner.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhammadfahd0121.mangodefend.domain.model.ScanResult

// --- CONSTANTS WARNA ---
val MangoOrange = Color(0xFFFFC107)
val MangoGreen = Color(0xFF76FF03)
val DarkBlue = Color(0xFF1A237E)
val UploadBgColor = Color(0xFFFFFDE7) // Cream muda
val DashedLineColor = Color(0xFFFFD54F) // Kuning gelap

// Gradient Khas MangoDefend
val MangoGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFFA000), Color(0xFF76FF03))
)

@Composable
fun UploadArea(onUploadClick: () -> Unit) {
    val stroke = Stroke(
        width = 4f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(UploadBgColor, shape = RoundedCornerShape(24.dp))
            .drawBehind {
                drawRoundRect(
                    color = DashedLineColor,
                    style = stroke,
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Awan (Pakai warna Solid, bukan Gradient biar ga error)
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                tint = MangoOrange,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Upload Files for Scanning",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Max size: 100MB",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol dengan Gradient Background
            Button(
                onClick = onUploadClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(), // Hapus padding default
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .height(48.dp)
                    // Gradient diterapkan di modifier background
                    .background(MangoGradient, shape = RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CHOOSE FILES",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// --- FUNGSI DIPISAH KE LUAR (Supaya tidak error nested function) ---

@Composable
fun ScanResultCard(data: ScanResult) {
    // 1. Logika Status
    val isScanning = data.prediction.contains("Scanning", ignoreCase = true) ||
            data.prediction.contains("Uploading", ignoreCase = true) ||
            data.prediction.contains("Calculating", ignoreCase = true)

    val statusColor = when {
        isScanning -> Color(0xFFFFA000)
        data.isMalware -> Color(0xFFD32F2F)
        else -> Color(0xFF388E3C)
    }

    val statusIcon = when {
        isScanning -> Icons.Default.Refresh
        data.isMalware -> Icons.Default.Warning
        else -> Icons.Default.CheckCircle
    }

    // --- ANIMASI JUICY ---

    // A. Warna Background
    val defaultContainerColor = if (data.isMalware) Color(0xFFFFEBEE) else Color.White
    val highlightColor = Color(0xFFFFF9C4)

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (data.isHighlighted) highlightColor else defaultContainerColor,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ColorAnim"
    )

    // B. Scale (Membesar saat Highlight)
    val scale by animateFloatAsState(
        targetValue = if (data.isHighlighted) 1.05f else 1f, // Membesar 5%
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ScaleAnim"
    )

    // C. Border
    val borderStroke = if (data.isHighlighted) {
        BorderStroke(2.dp, Color(0xFFFBC02D))
    } else {
        null
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = animatedBackgroundColor),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (data.isHighlighted) 12.dp else 2.dp
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .scale(scale) // Terapkan animasi skala
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
        ) {
            // A. Garis Warna Indikator
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(statusColor)
            )

            // B. Konten Utama
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = data.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Size: ${data.fileSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // C. Badge Status
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = if (isScanning) "SCANNING..." else data.prediction.uppercase(),
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}