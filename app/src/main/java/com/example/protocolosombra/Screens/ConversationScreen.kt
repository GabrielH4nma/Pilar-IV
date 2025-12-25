package com.example.protocolosombra.ui

import android.app.Activity
import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protocolosombra.data.GameData
import com.example.protocolosombra.data.Message
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun ConversationScreen(contactId: String, onBack: () -> Unit) {
    val contact = GameData.getContact(contactId)
    val context = LocalContext.current
    var backPressed by remember { mutableStateOf(false) }
    val isGhostChat = contactId == "sofia_ghost"
    val isDesconhecido = contactId == "desconhecido"
    var showFinalGlitch by remember { mutableStateOf(false) }

    // Bloqueia saída em momentos chave
    BackHandler(enabled = isGhostChat || GameData.showHauntedMarks.value) {}

    if (contact == null) { Text("Erro: Contacto não encontrado", color = Color.White); return }

    LaunchedEffect(Unit) { GameData.markAsRead(contactId) }

    // --- SEQUÊNCIA DO DESCONHECIDO (OLHO GIGANTE) ---
    if (isDesconhecido && GameData.isSecretPhotoRevealed && !GameData.trackerSequenceFinished.value) {
        LaunchedEffect(Unit) {
            delay(2000)
            GameData.showHauntedMarks.value = true
            // REMOVIDO: O som de "static_burst" foi retirado daqui.
            // O som de desenho (draw_line) será tocado pelo componente HauntedQuestionMarkOverlay.

            // Aumentamos o tempo de espera para 6 segundos para dar tempo à animação lenta
            delay(6000)
            GameData.showHauntedMarks.value = false
            GameData.triggerForcedNavigation.value = true
        }
    }

    val chatState = rememberChatState(contact)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val messages = chatState.messages
    val typingPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) { onDispose { try { typingPlayer.value?.stop(); typingPlayer.value?.release() } catch (e: Exception) {} } }

    fun playSound(fileName: String) {
        val resId = context.resources.getIdentifier(fileName, "raw", context.packageName)
        if (resId != 0) {
            try {
                if (fileName == "typing") {
                    if (typingPlayer.value?.isPlaying == true) return
                    val mp = MediaPlayer.create(context, resId)
                    typingPlayer.value = mp
                    mp.setOnCompletionListener { it.release(); if (typingPlayer.value == it) typingPlayer.value = null }
                    mp.start()
                } else {
                    typingPlayer.value?.let { if (it.isPlaying) it.stop(); it.release() }
                    typingPlayer.value = null
                    val mp = MediaPlayer.create(context, resId)
                    mp.setOnCompletionListener { it.release() }
                    mp.start()
                }
            } catch (e: Exception) {}
        }
    }

    if (isGhostChat) {
        LaunchedEffect(Unit) {
            if (messages.isEmpty()) {
                val ghostMessages = listOf("Tu não estás a ver a obra, pois não?", "Tu estás a ver a minha memória.", "Eu sou o Pilar 4. E agora tu estás cá dentro comigo.")
                delay(2000)
                ghostMessages.forEach { msg ->
                    chatState.isTyping.value = true; playSound("typing"); delay(2000)
                    chatState.isTyping.value = false; playSound("received")
                    messages.add(Message(content = msg, isFromPlayer = false, timestamp = "Agora", isRead = true))
                    delay(2500)
                }
                delay(4000)
                showFinalGlitch = true
                val resId = context.resources.getIdentifier("static_burst", "raw", context.packageName)
                if (resId != 0) MediaPlayer.create(context, resId).start()
                delay(3000)
                (context as? Activity)?.finish()
            }
        }
    }

    LaunchedEffect(messages.size, chatState.isTyping.value) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F1510))) {
            FakeStatusBar()
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1F2C34)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowBack, "Voltar", tint = if (isGhostChat) Color.Gray else Color.White, modifier = Modifier.padding(4.dp).clickable(enabled = !backPressed && !isGhostChat && !GameData.showHauntedMarks.value) { backPressed = true; onBack() })
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(35.dp).clip(CircleShape).background(Color.Gray), contentAlignment = Alignment.Center) { Text(contact.name.first().toString(), color = Color.White, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(contact.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                    Text(if (chatState.isTyping.value) "A escrever..." else contact.status, color = if (chatState.isTyping.value) Color(0xFF25D366) else Color(0xFFA0A0A0), fontSize = 12.sp, fontWeight = if (chatState.isTyping.value) FontWeight.Bold else FontWeight.Normal)
                }
                Icon(Icons.Default.MoreVert, "Menu", tint = Color.White)
            }

            LazyColumn(state = listState, modifier = Modifier.weight(1f).padding(horizontal = 8.dp), contentPadding = PaddingValues(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(messages) { message -> MessageBubble(message) }
                if (chatState.isTyping.value) item { TypingIndicator() }
            }

            if (!isGhostChat && !isDesconhecido) {
                AnimatedVisibility(visible = chatState.currentOptions.value.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1F2C34)).padding(10.dp)) {
                        Text("A tua resposta:", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        chatState.currentOptions.value.forEach { option ->
                            Button(
                                onClick = { chatState.selectOption(option, scope, onTypingSound = { playSound("typing") }, onMessageSound = { playSound("received") }) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A3942), contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text(option.text, modifier = Modifier.fillMaxWidth(), fontSize = 14.sp) }
                        }
                    }
                }
            }
            if (!isGhostChat && chatState.currentOptions.value.isEmpty() && !chatState.isTyping.value) {
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1F2C34)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Aguardando contacto...", color = Color.Gray, fontSize = 14.sp, fontStyle = FontStyle.Italic, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Sobreposição do Olho Gigante
        if (GameData.showHauntedMarks.value) {
            HauntedQuestionMarkOverlay()
        }

        if (showFinalGlitch) FinalGlitchOverlay()
    }
}

@Composable
fun HauntedQuestionMarkOverlay() {
    val context = LocalContext.current
    // Media Player para o som do desenho
    val drawingSoundPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    // Flags de estado
    var hasPlayedDotSound by remember { mutableStateOf(false) }
    var hasStoppedDrawingSound by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "alpha"
    )

    // Animação de progresso do desenho (0 a 1)
    val drawProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Tocar som de desenho (agora usando draw_line.mp3)
        val resId = context.resources.getIdentifier("draw_line", "raw", context.packageName)
        if (resId != 0) {
            try {
                val mp = MediaPlayer.create(context, resId)
                mp.isLooping = true // Loop enquanto desenha a linha
                mp.start()
                drawingSoundPlayer.value = mp
            } catch (e: Exception) {}
        }

        // Aumentei para 4.5 segundos para ser mais lento e dramático
        drawProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 4500, easing = LinearEasing)
        )

        // Limpeza final (caso sobre algum som)
        try {
            drawingSoundPlayer.value?.stop()
            drawingSoundPlayer.value?.release()
            drawingSoundPlayer.value = null
        } catch (e: Exception) {}
    }

    // Monitorizar o progresso para gerir os sons
    LaunchedEffect(drawProgress.value) {
        // 1. Parar o som de desenho quando o contorno do olho acaba (0.7f)
        if (drawProgress.value >= 0.7f && !hasStoppedDrawingSound) {
            hasStoppedDrawingSound = true
            try {
                if (drawingSoundPlayer.value?.isPlaying == true) {
                    drawingSoundPlayer.value?.stop()
                    drawingSoundPlayer.value?.release()
                    drawingSoundPlayer.value = null
                }
            } catch (e: Exception) {}
        }

        // 2. Tocar o som da pupila quando ela aparece (0.85f)
        if (drawProgress.value >= 0.85f && !hasPlayedDotSound) {
            hasPlayedDotSound = true

            // Tocar o som do ponto (ponto.mp3)
            val dotResId = context.resources.getIdentifier("ponto", "raw", context.packageName)
            if (dotResId != 0) {
                try {
                    val dotMp = MediaPlayer.create(context, dotResId)
                    dotMp.setOnCompletionListener { it.release() }
                    dotMp.start()
                } catch (e: Exception) {}
            }
        }
    }

    // Garante que o som para se o composable for removido antes de acabar
    DisposableEffect(Unit) {
        onDispose {
            try {
                drawingSoundPlayer.value?.stop()
                drawingSoundPlayer.value?.release()
                drawingSoundPlayer.value = null
            } catch (e: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        // Canvas para o OLHO
        Canvas(modifier = Modifier.size(300.dp)) {
            val w = size.width
            val h = size.height
            val strokeWidth = 35f
            val paintColor = Color(0xFFB71C1C).copy(alpha = alpha)

            // 1. Definir o Caminho do Olho
            val fullPath = androidx.compose.ui.graphics.Path().apply {
                // Começa no canto esquerdo
                moveTo(w * 0.15f, h * 0.5f)
                // Pálpebra superior (arco para cima)
                cubicTo(w * 0.35f, h * 0.2f, w * 0.65f, h * 0.2f, w * 0.85f, h * 0.5f)
                // Pálpebra inferior (arco para baixo, voltando ao início)
                cubicTo(w * 0.65f, h * 0.8f, w * 0.35f, h * 0.8f, w * 0.15f, h * 0.5f)
            }

            // 2. Calcular o comprimento para animar o desenho
            val pathMeasure = PathMeasure()
            pathMeasure.setPath(fullPath, false)
            val pathLength = pathMeasure.length

            // 3. Progresso do contorno do olho (até 0.7 do tempo total)
            val lineProgress = (drawProgress.value / 0.7f).coerceIn(0f, 1f)

            val drawnPath = androidx.compose.ui.graphics.Path()
            pathMeasure.getSegment(0f, pathLength * lineProgress, drawnPath, true)

            // Desenha o contorno do olho
            drawPath(
                path = drawnPath,
                color = paintColor,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )

            // 4. Desenha a Pupila (Aparece entre 0.85 e 0.95 do tempo - PAUSA INTENCIONAL)
            if (drawProgress.value > 0.85f) {
                val dotAlpha = ((drawProgress.value - 0.85f) / 0.1f).coerceIn(0f, 1f)
                val pupilCenter = Offset(w * 0.5f, h * 0.5f) // Centro do olho

                // Pupila
                drawCircle(
                    color = paintColor.copy(alpha = alpha * dotAlpha),
                    radius = strokeWidth * 0.8f, // Um pouco maior que o traço
                    center = pupilCenter
                )
            }

            // 5. Sangue a escorrer da pupila (Aparece de 0.9 até 1.0)
            if (drawProgress.value > 0.9f) {

                val dripProgress = ((drawProgress.value - 0.9f) / 0.1f).coerceIn(0f, 1f)
                // O sangue começa no centro da pupila e desce
                val startY = h * 0.5f

                // Múltiplos fios de sangue com variações
                val mainDripLength = h * 0.4f // Desce até perto do fundo
                val endY = startY + (mainDripLength * dripProgress)

                // Fio principal (mais fino e irregular)
                val pathDrip = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.5f, startY)
                    lineTo(w * 0.5f, endY)
                }

                // Desenha o rastro do sangue
                drawPath(
                    path = pathDrip,
                    color = paintColor.copy(alpha = 0.8f),
                    style = Stroke(
                        width = strokeWidth * 0.3f,
                        cap = StrokeCap.Round
                    )
                )

                // Gota na ponta
                val dropRadius = strokeWidth * 0.45f * (0.5f + dripProgress * 0.5f)
                val dropCenter = Offset(w * 0.5f, endY)

                val dropPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(dropCenter.x, dropCenter.y - dropRadius * 1.5f)
                    quadraticBezierTo(dropCenter.x + dropRadius, dropCenter.y + dropRadius * 0.5f, dropCenter.x, dropCenter.y + dropRadius)
                    quadraticBezierTo(dropCenter.x - dropRadius, dropCenter.y + dropRadius * 0.5f, dropCenter.x, dropCenter.y - dropRadius * 1.5f)
                    close()
                }

                drawPath(path = dropPath, color = paintColor)

                // Fio secundário (lágrima de sangue)
                if (dripProgress > 0.3f) {
                    val subDripProgress = ((dripProgress - 0.3f) / 0.7f)
                    val subEndY = startY + (mainDripLength * 0.6f * subDripProgress)

                    drawLine(
                        color = paintColor.copy(alpha = 0.6f),
                        start = Offset(w * 0.5f + 12f, startY + 10f),
                        end = Offset(w * 0.5f + 12f, subEndY),
                        strokeWidth = strokeWidth * 0.15f,
                        cap = StrokeCap.Round
                    )

                    drawCircle(
                        color = paintColor.copy(alpha = 0.8f),
                        radius = strokeWidth * 0.2f,
                        center = Offset(w * 0.5f + 12f, subEndY)
                    )
                }
            }
        }
    }
}

@Composable
fun FinalGlitchOverlay() {
    Canvas(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val w = size.width; val h = size.height; val random = Random(System.currentTimeMillis())
        repeat(500) { drawRect(color = if (random.nextBoolean()) Color.Red else Color.White, topLeft = Offset(random.nextFloat() * w, random.nextFloat() * h), size = androidx.compose.ui.geometry.Size(random.nextFloat() * 50f, 2f)) }
        repeat(20) { drawRect(color = Color.White.copy(alpha = 0.8f), topLeft = Offset(random.nextFloat() * w, random.nextFloat() * h), size = androidx.compose.ui.geometry.Size(random.nextFloat() * 100f, random.nextFloat() * 20f)) }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val bubbleColor = if (message.isFromPlayer) Color(0xFF005C4B) else Color(0xFF1F2C34)
    val alignment = if (message.isFromPlayer) Alignment.End else Alignment.Start
    val shape = if (message.isFromPlayer) RoundedCornerShape(12.dp, 0.dp, 12.dp, 12.dp) else RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp)
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Box(modifier = Modifier.widthIn(max = 280.dp).background(bubbleColor, shape).padding(10.dp)) {
            Column {
                if (message.imageResId != null) { Image(painter = painterResource(id = message.imageResId), contentDescription = "Anexo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)).padding(bottom = 8.dp)) }
                if (message.content.isNotEmpty()) { Text(message.content, color = Color.White, fontSize = 15.sp); Spacer(modifier = Modifier.height(4.dp)) }
                Text(message.timestamp, color = Color(0xFF8696A0), fontSize = 10.sp, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(modifier = Modifier.padding(vertical = 8.dp).background(Color(0xFF1F2C34), RoundedCornerShape(12.dp)).padding(12.dp)) { Text("...", color = Color(0xFF25D366), fontWeight = FontWeight.Bold, fontSize = 18.sp) }
}