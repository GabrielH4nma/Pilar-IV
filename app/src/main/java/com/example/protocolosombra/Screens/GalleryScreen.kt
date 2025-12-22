package com.example.protocolosombra.ui

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.protocolosombra.R
import com.example.protocolosombra.data.GameData

data class Album(val name: String, val count: Int, val isLocked: Boolean, val color: Color)

@Composable
fun GalleryScreen(onBack: () -> Unit) {
    val capturedEvidence = GameData.capturedEvidence

    val baseAlbums = listOf(
        Album("Câmara", 124, false, Color(0xFF42A5F5)),
        Album("WhatsApp", 450, false, Color(0xFF66BB6A)),
        Album("Instagram", 89, false, Color(0xFFAB47BC)),
        Album("Oculto", 1, true, Color(0xFFEF5350))
    )

    val albums = if (capturedEvidence.isNotEmpty()) {
        baseAlbums + Album("Provas", capturedEvidence.size, false, Color(0xFFFFA726))
    } else {
        baseAlbums
    }

    var showPinDialog by remember { mutableStateOf(false) }
    var showSecretImage by remember { mutableStateOf(GameData.isSecretPhotoRevealed) }
    var showEvidenceAlbum by remember { mutableStateOf(false) }

    var backPressed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        FakeStatusBar()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(enabled = !backPressed) {
                        backPressed = true
                        onBack()
                    }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Galeria",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (showSecretImage) {
            SecretContentView(onClose = { showSecretImage = false })
        } else if (showEvidenceAlbum) {
            EvidenceAlbumView(
                evidenceIds = capturedEvidence,
                onClose = { showEvidenceAlbum = false }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(albums) { album ->
                    AlbumCard(album, onClick = {
                        when (album.name) {
                            "Oculto" -> showPinDialog = true
                            "Provas" -> showEvidenceAlbum = true
                            "Câmara" -> {
                                // GATILHO: Ver fotos da Câmara dispara evento da Mãe
                                GameData.triggerGalleryStalkerEvent()
                            }
                            else -> { }
                        }
                    })
                }
            }
        }
    }

    if (showPinDialog) {
        PinDialog(
            onDismiss = { showPinDialog = false },
            onUnlock = {
                showPinDialog = false
                showSecretImage = true

                Handler(Looper.getMainLooper()).postDelayed({
                    GameData.triggerPhotoRevealedEvent()
                }, 5000)
            }
        )
    }
}

// ... Resto das funções (EvidenceAlbumView, AlbumCard, PinDialog, SecretContentView) mantêm-se iguais
@Composable
fun EvidenceAlbumView(evidenceIds: List<Int>, onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Pasta: Provas",
                color = Color(0xFFFFA726),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(evidenceIds) { resId ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Prova",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumCard(album: Album, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (album.isLocked) Icons.Default.Lock else Icons.Default.Folder,
                contentDescription = null,
                tint = album.color,
                modifier = Modifier.size(60.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = album.name, color = Color.White, fontWeight = FontWeight.Bold)
        Text(text = "${album.count} fotos", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun PinDialog(onDismiss: () -> Unit, onUnlock: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val secretPin = "2231"

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(Color(0xFF2C2C2C), RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Pasta Segura", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = pin.ifEmpty { "Insira PIN" },
                color = if (error) Color.Red else Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            val rows = listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("C","0","OK"))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { key ->
                            Button(
                                onClick = {
                                    when(key) {
                                        "C" -> { pin = ""; error = false }
                                        "OK" -> {
                                            if (pin == secretPin) onUnlock() else { error = true; pin = "" }
                                        }
                                        else -> if (pin.length < 4) pin += key
                                    }
                                },
                                modifier = Modifier.size(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242)),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(key, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecretContentView(onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("PROVA ENCONTRADA", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(4f/3f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.secret_image),
                contentDescription = "Prova do Crime",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Text(
                text = "DADOS CORROMPIDOS",
                color = Color.Red.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Fundação - Setor 4\n22 Outubro - 03:15 AM",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Há algo dentro do pilar...",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))) {
            Text("Fechar")
        }
    }
}