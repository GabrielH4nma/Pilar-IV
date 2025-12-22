package com.example.protocolosombra.ui

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protocolosombra.data.GameData
import kotlinx.coroutines.delay

enum class TrackerState { LIST, PLAYBACK }

data class ActivityRecord(
    val id: Int,
    val title: String,
    val date: String,
    val distance: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isSuspicious: Boolean = false
)

@Composable
fun TrackerScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var currentState by remember { mutableStateOf(if (GameData.isHauntedPlaybackActive.value) TrackerState.PLAYBACK else TrackerState.LIST) }

    // Desbloqueia automaticamente se for assombração (via Chat do Tiago ou Final)
    if (GameData.isHauntedPlaybackActive.value) {
        GameData.isTrackerRecordingUnlocked.value = true
    }

    var showLockedFeedback by remember { mutableStateOf(false) }

    val activities = listOf(
        ActivityRecord(1, "Corrida Matinal", "22 Out - 07:00", "5.2 km", Icons.Default.DirectionsRun),
        ActivityRecord(2, "Caminhada", "21 Out - 18:30", "2.1 km", Icons.Default.DirectionsWalk),
        // A ATIVIDADE "ANOMALIA"
        ActivityRecord(3, "Atividade Desconhecida", "23 Out - 03:15", "---", Icons.Default.Warning, isSuspicious = true),
        ActivityRecord(4, "Corrida", "20 Out - 07:15", "4.8 km", Icons.Default.DirectionsRun)
    )

    BackHandler(enabled = currentState == TrackerState.PLAYBACK) {
        if (!GameData.isHauntedPlaybackActive.value) {
            currentState = TrackerState.LIST
        }
    }

    LaunchedEffect(showLockedFeedback) {
        if (showLockedFeedback) {
            delay(2000)
            showLockedFeedback = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
        FakeStatusBar()

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ArrowBack,
                "Voltar",
                tint = if (GameData.isHauntedPlaybackActive.value) Color.DarkGray else Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(enabled = !GameData.isHauntedPlaybackActive.value) {
                        if (currentState == TrackerState.PLAYBACK) {
                            currentState = TrackerState.LIST
                        } else {
                            onBack()
                        }
                    }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("MyTrack", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        if (currentState == TrackerState.LIST) {
            Text(
                "HISTÓRICO DE ATIVIDADES",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn {
                    items(activities) { activity ->
                        ActivityItem(activity) {
                            if (activity.isSuspicious) {
                                if (GameData.isTrackerRecordingUnlocked.value) {
                                    currentState = TrackerState.PLAYBACK
                                } else {
                                    showLockedFeedback = true
                                }
                            }
                        }
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showLockedFeedback,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ACESSO NEGADO: FICHEIRO ENCRIPTADO", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            TrackerPlaybackView(
                onFinished = {
                    GameData.trackerSequenceFinished.value = true
                    GameData.isTrackerActive.value = false
                    GameData.isHauntedPlaybackActive.value = false
                }
            )
        }
    }
}

// ... ActivityItem, TrackerPlaybackView, DataWidget, MapVisualization mantêm-se iguais
@Composable
fun ActivityItem(activity: ActivityRecord, onClick: () -> Unit) {
    val isLocked = activity.isSuspicious && !GameData.isTrackerRecordingUnlocked.value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isLocked) Icons.Default.Lock else activity.icon,
            null,
            tint = if (activity.isSuspicious) Color.Red else Color(0xFF9C27B0), // Roxo MyTrack
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(if (isLocked) "DADOS ENCRIPTADOS" else activity.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(activity.date, color = Color.Gray, fontSize = 14.sp)
        }
        if (isLocked) {
            Text("---", color = Color.Red, fontWeight = FontWeight.Bold)
        } else {
            Text(activity.distance, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TrackerPlaybackView(onFinished: () -> Unit) {
    val context = LocalContext.current

    var isLive by remember { mutableStateOf(false) }
    var signalStrength by remember { mutableStateOf("A CARREGAR...") }
    var heartRate by remember { mutableStateOf(0) }
    var isSignalLost by remember { mutableStateOf(false) }

    val movementProgress = remember { Animatable(0f) }

    BackHandler(enabled = isLive) { }

    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

    fun playSound(fileName: String, loop: Boolean) {
        try {
            mediaPlayer.value?.stop()
            mediaPlayer.value?.release()
        } catch (e: Exception) { e.printStackTrace() }

        val resId = context.resources.getIdentifier(fileName, "raw", context.packageName)
        if (resId != 0) {
            try {
                val mp = MediaPlayer.create(context, resId)
                mp.setVolume(1.0f, 1.0f)
                mp.isLooping = loop
                mp.start()
                mediaPlayer.value = mp
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer.value?.stop()
                mediaPlayer.value?.release()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    LaunchedEffect(Unit) {
        if (GameData.trackerSequenceFinished.value) {
            signalStrength = "FALHA DE LIGAÇÃO"
            isSignalLost = true
            movementProgress.snapTo(1f)
        } else {
            delay(1000)
            signalStrength = "A REPRODUZIR..."
            isLive = true
            heartRate = 0 // MORTE CONFIRMADA

            playSound("tracking_beep", loop = true)

            movementProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 15000, easing = LinearEasing)
            )

            isLive = false
            signalStrength = "FALHA DE LIGAÇÃO"
            isSignalLost = true

            playSound("flatline", loop = false)
            onFinished()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth().weight(1f).padding(16.dp)
                .border(1.dp, if (isSignalLost) Color.Red else Color(0xFF333333), RoundedCornerShape(4.dp))
                .background(Color(0xFF111111)).padding(2.dp)
        ) {
            MapVisualization(isLive, movementProgress.value, isSignalLost)

            if (isLive) {
                Box(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp)
                        .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MOVIMENTO ATÍPICO", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (isSignalLost) {
                Box(
                    modifier = Modifier.align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SINAL PERDIDO", color = Color.Red, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("FALHA DE LIGAÇÃO", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DataWidget("BPM", if (isLive || isSignalLost) "0" else "--", "bpm", if (isLive || isSignalLost) Color.Red else Color.Gray)
                DataWidget("DISTÂNCIA", if (isSignalLost) "---" else if (isLive) "%.1f".format(movementProgress.value * 5.0f) else "---", "km", Color.White)
                DataWidget("TEMPO", if (isLive) "GRAVAÇÃO" else "03:15", "", Color.White)
            }
        }
    }
}

@Composable
fun DataWidget(title: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(unit, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
fun MapVisualization(isLive: Boolean, progress: Float, isSignalLost: Boolean) {
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val streetColor = Color(0xFF333333)

        drawLine(streetColor, Offset(w * 0.2f, 0f), Offset(w * 0.2f, h), strokeWidth = 2f)
        drawLine(streetColor, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeWidth = 2f)
        drawLine(streetColor, Offset(w * 0.8f, 0f), Offset(w * 0.8f, h), strokeWidth = 2f)
        drawLine(streetColor, Offset(0f, h * 0.3f), Offset(w, h * 0.3f), strokeWidth = 2f)
        drawLine(streetColor, Offset(0f, h * 0.6f), Offset(w, h * 0.6f), strokeWidth = 2f)
        drawLine(streetColor, Offset(0f, h * 0.8f), Offset(w, h * 0.8f), strokeWidth = 2f)

        val startPoint = Offset(w * 0.2f, h * 0.8f)
        val endPoint = Offset(w * 0.8f, h * 0.3f)

        if (!isLive && !isSignalLost) {
            drawCircle(Color(0xFF4CAF50), radius = 15f, center = startPoint)
        }

        if (isSignalLost) {
            drawCircle(Color.Gray, radius = 15f, center = endPoint)
        }

        if (isLive || isSignalLost) {
            val currentX: Float
            val currentY: Float

            if (progress < 0.4f) {
                val p = progress / 0.4f
                currentX = startPoint.x + (w * 0.5f - startPoint.x) * p
                currentY = startPoint.y
            } else if (progress < 0.8f) {
                val p = (progress - 0.4f) / 0.4f
                currentX = w * 0.5f
                currentY = startPoint.y - (startPoint.y - endPoint.y) * p
            } else {
                val p = (progress - 0.8f) / 0.2f
                currentX = w * 0.5f + (endPoint.x - w * 0.5f) * p
                currentY = endPoint.y
            }
            val currentPos = Offset(currentX, currentY)

            val path = Path().apply {
                moveTo(startPoint.x, startPoint.y)
                if (progress >= 0.4f) lineTo(w * 0.5f, startPoint.y)
                if (progress >= 0.8f) lineTo(w * 0.5f, endPoint.y)
                lineTo(currentX, currentY)
            }
            drawPath(path, Color.Red.copy(alpha = 0.5f), style = Stroke(width = 4f))

            if (!isSignalLost) {
                drawCircle(Color.Red.copy(alpha = pulseAlpha), radius = 25f, center = currentPos)
                drawCircle(Color.Red, radius = 10f, center = currentPos)
            } else {
                drawCircle(Color.Gray, radius = 10f, center = currentPos)
                drawLine(Color.Red, Offset(currentPos.x - 10, currentPos.y - 10), Offset(currentPos.x + 10, currentPos.y + 10), strokeWidth = 3f)
                drawLine(Color.Red, Offset(currentPos.x + 10, currentPos.y - 10), Offset(currentPos.x - 10, currentPos.y + 10), strokeWidth = 3f)
            }
        }
    }
}