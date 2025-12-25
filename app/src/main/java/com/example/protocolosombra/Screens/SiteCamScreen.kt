package com.example.protocolosombra.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protocolosombra.R
import com.example.protocolosombra.data.GameData
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun SiteCamScreen(
    onBack: () -> Unit,
    onForceNavigateToChat: () -> Unit
) {
    val context = LocalContext.current
    val isSystemOnline = GameData.hasReadGhostEmail.value

    var isFadingOut by remember { mutableStateOf(false) }
    BackHandler(enabled = isSystemOnline && !isFadingOut) {}

    var selectedCam by remember { mutableStateOf(0) }
    var lastCam by remember { mutableStateOf(0) }

    val capturedAnomalies = remember { mutableStateListOf<Int>() }
    // Esta variável auxiliar ajuda a UI a recompor quando a lista muda
    val anomaliesCount = capturedAnomalies.size

    var showCaptureFlash by remember { mutableStateOf(false) }
    var captureFeedback by remember { mutableStateOf("") }

    var isCaptureCoolingDown by remember { mutableStateOf(false) }

    var cam01HasVan by remember { mutableStateOf(false) }

    var isVanReadyToAppear by remember { mutableStateOf(false) }
    var hasSpottedVan by remember { mutableStateOf(false) }

    var currentAnomalyVisible by remember { mutableStateOf(0) }
    var showJumpscare by remember { mutableStateOf(false) }

    var currentFrequency by remember { mutableStateOf(50f) }
    var targetFrequency by remember { mutableStateOf(Random.nextFloat() * 100f) }

    var dangerLevel by remember { mutableStateOf(0f) }
    var isCamJammed by remember { mutableStateOf(false) }

    var jumpscareCount by remember { mutableIntStateOf(0) }

    var backPressed by remember { mutableStateOf(false) }

    val fadeAlpha by animateFloatAsState(
        targetValue = if (isFadingOut) 1f else 0f,
        animationSpec = tween(3000)
    )

    val cam03State = when(anomaliesCount) {
        0 -> 0
        1 -> 1
        else -> 2
    }

    val scope = rememberCoroutineScope()

    val staticPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    val buildupPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

    fun playStaticBurst() {
        val resId = context.resources.getIdentifier("static_burst", "raw", context.packageName)
        if (resId != 0) {
            try {
                val mp = MediaPlayer.create(context, resId)
                mp.setOnCompletionListener { it.release() }
                mp.start()
            } catch (e: Exception) {}
        }
    }

    fun playJumpscareSound() {
        val resId = context.resources.getIdentifier("jumpscare_sound", "raw", context.packageName)
        if (resId != 0) {
            try {
                val mp = MediaPlayer.create(context, resId)
                mp.setOnCompletionListener { it.release() }
                mp.start()
            } catch (e: Exception) {}
        }
    }

    fun updateBuildupSound(intensity: Float, isPlaying: Boolean) {
        val resId = context.resources.getIdentifier("static_buildup", "raw", context.packageName)
        if (resId != 0) {
            try {
                if (isPlaying) {
                    if (buildupPlayer.value == null) {
                        val mp = MediaPlayer.create(context, resId)
                        mp.isLooping = true
                        mp.start()
                        buildupPlayer.value = mp
                    } else {
                        try {
                            if (buildupPlayer.value?.isPlaying == false) {
                                buildupPlayer.value?.start()
                            }
                        } catch (e: IllegalStateException) {
                            // Se o player estiver num estado inválido, recriamos
                            buildupPlayer.value?.release()
                            val mp = MediaPlayer.create(context, resId)
                            mp.isLooping = true
                            mp.start()
                            buildupPlayer.value = mp
                        }
                    }
                    val volume = intensity.coerceIn(0f, 1f)
                    buildupPlayer.value?.setVolume(volume, volume)
                } else {
                    if (buildupPlayer.value?.isPlaying == true) {
                        buildupPlayer.value?.pause()
                    }
                }
            } catch (e: Exception) {
                // Em caso de erro fatal, tentamos limpar
                buildupPlayer.value = null
            }
        }
    }

    // Limpeza rigorosa ao sair do ecrã
    DisposableEffect(Unit) {
        onDispose {
            try {
                staticPlayer.value?.release()
                staticPlayer.value = null

                buildupPlayer.value?.stop()
                buildupPlayer.value?.release()
                buildupPlayer.value = null
            } catch (e: Exception) {}
        }
    }

    LaunchedEffect(isFadingOut) {
        if (isFadingOut) {
            updateBuildupSound(0f, false)
            delay(3500)
            onBack()
        }
    }

    LaunchedEffect(isCamJammed) {
        if (isCamJammed) {
            captureFeedback = "SINAL RASTREADO - BLOQUEIO DE SEGURANÇA"
            updateBuildupSound(0f, false)

            delay(8000)

            targetFrequency = Random.nextFloat() * 100f
            dangerLevel = 0f
            captureFeedback = ""
            isCamJammed = false
        }
    }

    LaunchedEffect(Unit) {
        if (isSystemOnline) {
            delay(Random.nextLong(5000, 8000))
            isVanReadyToAppear = true
        }
    }

    LaunchedEffect(selectedCam) {
        if (isSystemOnline && !isFadingOut) {
            playStaticBurst()

            if (selectedCam == 0 && isVanReadyToAppear) {
                cam01HasVan = true
                if (!hasSpottedVan) {
                    delay(500)
                    hasSpottedVan = true
                }
            }
        }
        lastCam = selectedCam
    }

    LaunchedEffect(selectedCam, isCamJammed, isFadingOut) {
        if (isSystemOnline && !isFadingOut) {
            if (selectedCam == 1 && !isCamJammed && hasSpottedVan) {
                while(selectedCam == 1 && !isCamJammed && !isFadingOut) {
                    delay(100)
                    val isTuned = abs(currentFrequency - targetFrequency) < 10f
                    val increment = if (isTuned) 0.012f else 0.025f

                    dangerLevel = (dangerLevel + increment).coerceAtMost(1.1f)
                    updateBuildupSound(dangerLevel, true)

                    if (dangerLevel >= 1.0f) {
                        playJumpscareSound()
                        showJumpscare = true
                        delay(400)
                        showJumpscare = false
                        jumpscareCount++
                        if (jumpscareCount >= 3) {
                            isFadingOut = true
                        } else {
                            isCamJammed = true
                        }
                    }
                }
            } else {
                updateBuildupSound(0f, false)
                if (!isCamJammed) {
                    while(dangerLevel > 0) {
                        delay(50)
                        dangerLevel = (dangerLevel - 0.05f).coerceAtLeast(0f)
                    }
                }
            }
        }
    }

    LaunchedEffect(selectedCam, isCamJammed) {
        if (isSystemOnline && selectedCam == 1 && !isCamJammed) {
            while(isActive && selectedCam == 1 && !isCamJammed) {
                delay(Random.nextLong(200, 800))
                val instabilityChance = 0.3f + (dangerLevel * 0.4f)
                if (Random.nextFloat() < instabilityChance) {
                    val direction = if (currentFrequency < targetFrequency) -1f else 1f
                    val driftAmount = (Random.nextFloat() * 6f + 2f) * direction
                    currentFrequency = (currentFrequency + driftAmount).coerceIn(0f, 100f)
                }
            }
        }
    }

    LaunchedEffect(anomaliesCount, hasSpottedVan, isFadingOut) {
        if (isSystemOnline && hasSpottedVan && !isFadingOut) {
            while(capturedAnomalies.size < 2) {
                val waitTime = Random.nextLong(2000, 5000)
                delay(waitTime)

                val available = listOf(1, 2).filter { !capturedAnomalies.contains(it) }
                if (available.isNotEmpty()) {
                    val next = available.random()
                    currentAnomalyVisible = next
                    delay(Random.nextLong(2000, 4000))
                    currentAnomalyVisible = 0
                }
            }
        }
    }

    val tuneDifference = abs(currentFrequency - targetFrequency)
    val isTuned = tuneDifference < 8f
    val tuneStaticAlpha = if (isCamJammed && selectedCam == 1) 1f else (tuneDifference / 40f).coerceIn(0f, 0.95f)
    val isCurrentCamBlocked = isCamJammed && selectedCam == 1

    // --- LÓGICA DE CAPTURA CORRIGIDA ---
    fun tryCapture() {
        if (!isSystemOnline || isCamJammed || isFadingOut) return
        if (isCaptureCoolingDown) return

        isCaptureCoolingDown = true
        scope.launch {
            delay(1000)
            isCaptureCoolingDown = false
        }

        showCaptureFlash = true
        scope.launch {
            delay(100)
            showCaptureFlash = false
        }

        if (selectedCam == 1) {
            if (!hasSpottedVan) {
                captureFeedback = "ERRO: SINAL LIMPO. NADA A REPORTAR."
                return
            }

            if (!isTuned) {
                captureFeedback = "ERRO: SINAL FRACO. SINTONIZE A FREQUÊNCIA."
                return
            }

            if (currentAnomalyVisible != 0) {
                if (!capturedAnomalies.contains(currentAnomalyVisible)) {
                    capturedAnomalies.add(currentAnomalyVisible)

                    dangerLevel = 0f

                    // Guarda a prova na galeria
                    if (currentAnomalyVisible == 1) {
                        GameData.saveEvidenceToGallery(R.drawable.cam02_shadow)
                        captureFeedback = "REGISTO #1: ANOMALIA TÉRMICA"
                    } else if (currentAnomalyVisible == 2) {
                        GameData.saveEvidenceToGallery(R.drawable.cam02_hand)
                        captureFeedback = "REGISTO #2: PROVA BIOLÓGICA"
                    }

                    // Esconde anomalia imediatamente
                    currentAnomalyVisible = 0

                    scope.launch {
                        delay(1000)
                        selectedCam = 2 // Muda para Escritório para ver a reação

                        // CORREÇÃO: Só termina se AMBAS as anomalias foram capturadas
                        if (capturedAnomalies.size >= 2) {
                            delay(3000)
                            GameData.triggerFinalSequence()
                            onForceNavigateToChat()
                        }
                    }
                }
            } else {
                captureFeedback = "ERRO: NADA DETETADO"
            }
        } else {
            captureFeedback = "ERRO: SINAL ESTÁVEL"
        }

        scope.launch {
            delay(2000)
            captureFeedback = ""
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
        FakeStatusBar()

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ArrowBack,
                "Voltar",
                tint = if (isSystemOnline) Color.DarkGray else Color.White,
                modifier = Modifier.clickable(enabled = !backPressed && !isSystemOnline && !isFadingOut) {
                    backPressed = true
                    onBack()
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("SiteCam Pro", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            if (isSystemOnline) {
                BlinkingLiveIndicator()
            } else {
                Text("OFFLINE", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // --- ÁREA CENTRAL (MONITOR + SLIDER) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            // 1. MONITOR
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(4.dp, if (isCurrentCamBlocked) Color.Red else Color(0xFF111111), RoundedCornerShape(8.dp))
                    .background(Color.Black)
            ) {
                if (isSystemOnline) {
                    val imageRes = when {
                        showJumpscare && selectedCam == 1 -> R.drawable.cam02_jumpscare

                        selectedCam == 0 -> if (cam01HasVan) R.drawable.cam01_van else R.drawable.cam01_empty

                        selectedCam == 1 -> when(currentAnomalyVisible) {
                            1 -> R.drawable.cam02_shadow
                            2 -> R.drawable.cam02_hand
                            else -> R.drawable.cam02_base
                        }

                        selectedCam == 2 -> when(cam03State) {
                            0 -> R.drawable.cam03_off
                            1 -> R.drawable.cam03_on
                            else -> R.drawable.cam03_chair
                        }
                        else -> R.drawable.cam01_empty
                    }

                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (selectedCam == 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = tuneStaticAlpha))
                        )
                        if (!isTuned) {
                            SiteCamOverlay(dangerLevel = 0f, intensityMultiplier = 2.0f)
                        }
                    }

                    SiteCamOverlay(dangerLevel = dangerLevel, intensityMultiplier = 1.0f)

                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = when {
                                isCurrentCamBlocked -> "!!! SINAL BLOQUEADO !!!"
                                selectedCam == 0 -> "CAM 01 - ENTRADA"
                                selectedCam == 1 -> "CAM 02 - CAVE [${currentFrequency.toInt()} Hz]"
                                selectedCam == 2 -> "CAM 03 - CHEFIA"
                                else -> "NO SIGNAL"
                            },
                            color = if (isCurrentCamBlocked) Color.Red else Color.Green,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f))
                        )

                        if (selectedCam == 1 && !isCamJammed) {
                            LinearProgressIndicator(
                                progress = dangerLevel,
                                modifier = Modifier.width(100.dp).height(4.dp).padding(top = 4.dp),
                                color = Color.Red,
                                trackColor = Color.Transparent
                            )
                        }
                    }

                    if (captureFeedback.isNotEmpty()) {
                        Box(modifier = Modifier.align(Alignment.Center).background(Color.Black.copy(alpha = 0.7f)).border(1.dp, if (captureFeedback.contains("ERRO") || captureFeedback.contains("BLOQUEIO")) Color.Red else Color.Green).padding(12.dp)) {
                            Text(captureFeedback, color = if (captureFeedback.contains("ERRO") || captureFeedback.contains("BLOQUEIO")) Color.Red else Color.Green, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    if (showCaptureFlash) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.White))
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Lock, null, tint = Color.Red, modifier = Modifier.size(64.dp))
                        Text("BLOQUEADO", color = Color.Red, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("AGUARDANDO CREDENCIAIS...", color = Color.Red, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // 2. SLIDER HORIZONTAL
            if (isSystemOnline && selectedCam == 1 && !isCamJammed && !isFadingOut) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color(0xFF111111), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FREQ", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${currentFrequency.toInt()}",
                        color = if(isTuned) Color.Green else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(30.dp)
                    )

                    Slider(
                        value = currentFrequency,
                        onValueChange = { currentFrequency = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Green,
                            activeTrackColor = Color.Green,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.FiberManualRecord,
                        null,
                        tint = if(isTuned) Color.Green else Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // --- CONTROLOS INFERIORES ---
        Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF111111)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CamButton("01", selectedCam == 0) { if (isSystemOnline) selectedCam = 0 }
                    CamButton("02", selectedCam == 1) { if (isSystemOnline) selectedCam = 1 }
                    CamButton("03", selectedCam == 2) { if (isSystemOnline) selectedCam = 2 }
                }

                val btnColor = if (isCaptureCoolingDown || isCamJammed) Color.Gray else Color(0xFFD32F2F)
                Button(
                    onClick = { tryCapture() },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSystemOnline) btnColor else Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CAPTURA")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val dangerText = if (dangerLevel > 0.7f) "ALERTA: ATIVIDADE DETETADA NA LINHA" else "SISTEMA: MONITORIZAÇÃO ATIVA"
            Text(
                // CORREÇÃO AQUI: Usamos capturedAnomalies.size
                text = if (!isSystemOnline) "ERRO: SERVIDOR NÃO RESPONDE" else if (capturedAnomalies.size >= 2) "ERRO CRÍTICO: UPLOAD INICIADO..." else dangerText,
                color = if (!isSystemOnline || capturedAnomalies.size >= 2 || dangerLevel > 0.7f) Color.Red else Color(0xFF006400),
                fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
            )
        }
    }

    if (isFadingOut) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = fadeAlpha))
                .clickable(enabled = false) {}
        )
    }
}

// ... Resto das funções (SiteCamOverlay, BlinkingLiveIndicator, CamButton) mantêm-se iguais
@Composable
fun SiteCamOverlay(dangerLevel: Float, intensityMultiplier: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "noise")
    val duration = if (dangerLevel > 0.5f) 50 else 100
    val noiseOffset by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 100f, animationSpec = infiniteRepeatable(tween(duration, easing = LinearEasing), RepeatMode.Restart), label = "offset")

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (i in 0 until h.toInt() step 4) {
            drawLine(Color.Black.copy(alpha = 0.3f), Offset(0f, i.toFloat()), Offset(w, i.toFloat()), strokeWidth = 1f)
        }

        val random = Random(System.currentTimeMillis())

        val baseParticles = 500
        val maxParticles = 3000
        val particleCount = (baseParticles + (maxParticles * dangerLevel * intensityMultiplier)).toInt()

        val particleAlpha = 0.15f + (dangerLevel * 0.3f)

        repeat(particleCount) {
            val x = random.nextFloat() * w
            val y = random.nextFloat() * h
            drawCircle(Color.White.copy(alpha = particleAlpha.coerceIn(0f, 1f)), radius = 1.5f, center = Offset(x, y))
        }

        if (dangerLevel > 0.3f && random.nextFloat() > (0.8f - (dangerLevel * 0.5f))) {
            val y = random.nextFloat() * h
            val thickness = 2f + (dangerLevel * 5f)
            drawRect(Color.White.copy(alpha = 0.5f), topLeft = Offset(0f, y), size = Size(w, thickness))
        }

        drawRect(Color.Black.copy(alpha = 0.3f), topLeft = Offset.Zero, size = size, style = Stroke(width = 80f))
    }
}

@Composable
fun BlinkingLiveIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val alpha by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "alpha")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.FiberManualRecord, null, tint = Color.Red.copy(alpha = alpha), modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("LIVE", color = Color.Red.copy(alpha = alpha), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CamButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFF004400) else Color(0xFF333333), contentColor = if (isSelected) Color.Green else Color.Gray), shape = RoundedCornerShape(4.dp), modifier = Modifier.width(60.dp), contentPadding = PaddingValues(0.dp)) {
        Text(label, fontWeight = FontWeight.Bold)
    }
}