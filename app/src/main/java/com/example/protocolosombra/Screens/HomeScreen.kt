package com.example.protocolosombra.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.protocolosombra.data.GameData
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToBank: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToSiteCam: () -> Unit,
    onNavigateToEmail: () -> Unit,
    onNavigateToRadio: () -> Unit,
    onNavigateToCave: () -> Unit = {}
) {
    val isSystemRebooted = GameData.isSystemRebooted.value

    if (isSystemRebooted) {
        HauntedHomeScreen(onNavigateToCave)
    } else {
        NormalHomeScreen(
            onNavigateToChat, onNavigateToGallery, onNavigateToBank,
            onNavigateToNotes, onNavigateToTracker, onNavigateToSiteCam,
            onNavigateToEmail, onNavigateToRadio
        )
    }
}

@Composable
fun HauntedHomeScreen(onNavigateToCave: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "haunted_bg")
    val bgColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF1A0000),
        targetValue = Color(0xFF330000),
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "bg_color"
    )

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            FakeStatusBar()

            Spacer(modifier = Modifier.height(100.dp))

            Text(
                "00:00",
                color = Color.Red,
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                "DOMINGO ETERNO",
                color = Color(0xFFB71C1C),
                fontSize = 16.sp,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNavigateToCave() }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Red, RoundedCornerShape(20.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("A CAVE", color = Color.Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun NormalHomeScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToBank: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToSiteCam: () -> Unit,
    onNavigateToEmail: () -> Unit,
    onNavigateToRadio: () -> Unit
) {
    var showInstallationDialog by remember { mutableStateOf(false) }
    val isSiteCamInstalled = GameData.isSiteCamInstalled.value
    val trackerSequenceFinished = GameData.trackerSequenceFinished.value

    LaunchedEffect(trackerSequenceFinished, isSiteCamInstalled) {
        if (trackerSequenceFinished && !isSiteCamInstalled) {
            delay(1000)
            showInstallationDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        Column(modifier = Modifier.fillMaxSize()) {
            FakeStatusBar()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("19:42", color = Color(0xFFE0E0E0), fontSize = 72.sp, fontWeight = FontWeight.Light)
                Text("Segunda, 23 Out", color = Color(0xFFA0A0A0), fontSize = 18.sp, letterSpacing = 1.sp)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { AppIcon("Galeria", Color(0xFF2196F3), onNavigateToGallery) }
                item { AppIcon("Banco", Color(0xFFFFC107), onNavigateToBank) }
                item { AppIcon("Notas", Color(0xFFFF5722), onNavigateToNotes) }
                item {
                    AppIcon("MyTrack", Color(0xFF9C27B0), onNavigateToTracker)
                }
                item {
                    val hasEmailNotification = GameData.hasUnreadEmails()
                    AppIcon("Gmail", Color(0xFFD44638), onNavigateToEmail, hasNotification = hasEmailNotification)
                }
                item {
                    if (isSiteCamInstalled) {
                        AppIcon("SiteCam", Color(0xFF607D8B), onNavigateToSiteCam)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .shadow(10.dp, RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF252525), Color(0xFF1E1E1E))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(
                        name = "Rádio",
                        color = Color(0xFFE91E63),
                        onClick = onNavigateToRadio,
                        iconSize = 55.dp
                    )

                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.DarkGray))

                    val hasChatNotification = GameData.hasUnreadMessages()
                    AppIcon(
                        name = "Chat",
                        color = Color(0xFF4CAF50),
                        onClick = onNavigateToChat,
                        hasNotification = hasChatNotification,
                        iconSize = 55.dp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showInstallationDialog) {
        SystemInstallationDialog(onFinished = { GameData.isSiteCamInstalled.value = true; showInstallationDialog = false })
    }
}

// ... Resto das funções auxiliares (AppIcon, SystemInstallationDialog) mantêm-se iguais ...
@Composable
fun AppIcon(
    name: String,
    color: Color,
    onClick: () -> Unit,
    hasNotification: Boolean = false,
    isUrgent: Boolean = false,
    iconSize: androidx.compose.ui.unit.Dp = 60.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "urgent")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isUrgent) {
                Box(modifier = Modifier.size(iconSize + 10.dp).background(Color.Red.copy(alpha = alpha), RoundedCornerShape(20.dp)))
            }
            Box(modifier = Modifier.size(iconSize).background(color, shape = RoundedCornerShape(16.dp)))
            if (hasNotification) {
                Box(modifier = Modifier.size(14.dp).background(Color.Red, CircleShape).align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = name, color = Color.White, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
fun SystemInstallationDialog(onFinished: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    var text by remember { mutableStateOf("A Conectar a CONST_LUZ_SECURE...") }
    var titleColor by remember { mutableStateOf(Color.White) }

    LaunchedEffect(Unit) {
        delay(1500)
        text = "A descarregar Ferramentas de Admin..."
        for (i in 1..10) { progress = i / 20f; delay(100) }
        text = "NAO_ME_DEIXES_AQUI.apk"
        titleColor = Color.Red
        delay(1000)
        text = "A instalar SiteCam Viewer..."
        titleColor = Color.White
        for (i in 11..20) { progress = i / 20f; delay(50) }
        delay(500)
        onFinished()
    }

    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF222222), RoundedCornerShape(12.dp)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Download, null, tint = titleColor, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("ATUALIZAÇÃO DE SISTEMA", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text, color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(bottom = 20.dp))
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(8.dp), color = titleColor, trackColor = Color.Black)
        }
    }
}

@Composable
fun InvisibleAppIcon() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
        Box(modifier = Modifier.size(60.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Placeholder", color = Color.Transparent, fontSize = 14.sp)
    }
}