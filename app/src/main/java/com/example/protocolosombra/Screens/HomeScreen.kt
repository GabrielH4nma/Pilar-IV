package com.example.protocolosombra.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.protocolosombra.data.GameData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToBank: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToSiteCam: () -> Unit,
    onNavigateToEmail: () -> Unit
) {
    var showInstallationDialog by remember { mutableStateOf(false) }
    val isSiteCamInstalled = GameData.isSiteCamInstalled.value

    val pageCount = if (isSiteCamInstalled) 2 else 1
    val pagerState = rememberPagerState(pageCount = { pageCount })

    // Gatilho de Instalação (SiteCam)
    LaunchedEffect(GameData.trackerSequenceFinished.value) {
        if (GameData.trackerSequenceFinished.value && !GameData.isSiteCamInstalled.value) {
            delay(1000)
            showInstallationDialog = true
        }
    }

    LaunchedEffect(isSiteCamInstalled) {
        if (isSiteCamInstalled && !GameData.hasShownSiteCamAnimation.value) {
            delay(500)
            pagerState.animateScrollToPage(1)
            GameData.hasShownSiteCamAnimation.value = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        FakeStatusBar()
        Spacer(modifier = Modifier.height(40.dp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("19:42", color = Color(0xFFE0E0E0), fontSize = 80.sp, fontWeight = FontWeight.Thin)
            Text("Segunda, 23 Out", color = Color(0xFFA0A0A0), fontSize = 18.sp, letterSpacing = 2.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) { page ->
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (page == 0) {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        val hasChatNotification = GameData.hasUnreadMessages()
                        AppIcon("ChatLog", Color(0xFF4CAF50), onNavigateToChat, hasNotification = hasChatNotification)
                        AppIcon("Galeria", Color(0xFF2196F3), onNavigateToGallery)
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        AppIcon("Banco", Color(0xFFFFC107), onNavigateToBank)
                        AppIcon("Notas", Color(0xFFFF5722), onNavigateToNotes)
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        // MyTrack sempre visível agora
                        AppIcon("MyTrack", Color(0xFF9C27B0), onNavigateToTracker, isUrgent = false)

                        val hasEmailNotification = GameData.hasUnreadEmails()
                        AppIcon("Gmail", Color(0xFFD44638), onNavigateToEmail, hasNotification = hasEmailNotification)
                    }
                } else if (page == 1) {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        AppIcon("SiteCam", Color(0xFF607D8B), onNavigateToSiteCam)
                        InvisibleAppIcon()
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        InvisibleAppIcon()
                        InvisibleAppIcon()
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        InvisibleAppIcon()
                        InvisibleAppIcon()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (pageCount > 1) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.White else Color.DarkGray
                    Box(modifier = Modifier.padding(4.dp).clip(CircleShape).background(color).size(8.dp))
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showInstallationDialog) {
        SystemInstallationDialog(
            onFinished = {
                // CORREÇÃO: Alteramos o valor diretamente em vez de chamar a função
                GameData.isSiteCamInstalled.value = true
                showInstallationDialog = false
            }
        )
    }
}

// ... SystemInstallationDialog, AppIcon e InvisibleAppIcon mantêm-se iguais
@Composable
fun SystemInstallationDialog(onFinished: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    var text by remember { mutableStateOf("A Conectar a CONST_LUZ_SECURE...") }
    var titleColor by remember { mutableStateOf(Color.White) }

    LaunchedEffect(Unit) {
        delay(1500)
        text = "A descarregar Ferramentas de Admin..."
        val steps = 20
        for (i in 1..10) {
            progress = i / 20f
            delay(100)
        }
        text = "NAO_ME_DEIXES_AQUI.apk"
        titleColor = Color.Red
        delay(1000)
        text = "A instalar SiteCam Viewer..."
        titleColor = Color.White
        for (i in 11..20) {
            progress = i / 20f
            delay(50)
        }
        delay(500)
        onFinished()
    }

    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF222222), RoundedCornerShape(12.dp))
                .padding(24.dp),
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
fun AppIcon(
    name: String,
    color: Color,
    onClick: () -> Unit,
    hasNotification: Boolean = false,
    isUrgent: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "urgent")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isUrgent) {
                Box(modifier = Modifier.size(70.dp).background(Color.Red.copy(alpha = alpha), RoundedCornerShape(20.dp)))
            }
            Box(modifier = Modifier.size(60.dp).background(color, shape = RoundedCornerShape(16.dp)))
            if (hasNotification) {
                Box(modifier = Modifier.size(16.dp).background(Color.Red, CircleShape).align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = name, color = Color.White, fontSize = 14.sp)
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