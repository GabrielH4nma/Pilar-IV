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
fun ConversationScreen(
    contactId: String,
    onBack: () -> Unit
) {
    val contact = GameData.getContact(contactId)
    val context = LocalContext.current
    var backPressed by remember { mutableStateOf(false) }

    val isGhostChat = contactId == "sofia_ghost"
    val isDesconhecido = contactId == "desconhecido"

    var showFinalGlitch by remember { mutableStateOf(false) }

    // Bloqueia saída se for o chat final OU se estiver na sequência do Desconhecido
    BackHandler(enabled = isGhostChat || GameData.showHauntedMarks.value) {}

    if (contact == null) {
        Text("Erro: Contacto não encontrado", color = Color.White)
        return
    }

    LaunchedEffect(Unit) {
        GameData.markAsRead(contactId)
    }

    // --- SEQUÊNCIA DO DESCONHECIDO (PONTOS DE INTERROGAÇÃO) ---
    // Se entramos no chat do Desconhecido e a foto secreta já foi revelada (logo, a mensagem chegou)
    if (isDesconhecido && GameData.isSecretPhotoRevealed && !GameData.trackerSequenceFinished.value) {
        LaunchedEffect(Unit) {
            delay(2000) // Tempo para ler a mensagem

            // 1. Ativa os pontos de interrogação
            GameData.showHauntedMarks.value = true

            // Toca som de susto se houver
            val resId = context.resources.getIdentifier("static_burst", "raw", context.packageName)
            if (resId != 0) {
                MediaPlayer.create(context, resId).start()
            }

            delay(3000) // Ficam na tela 3 segundos

            // 2. Inicia a navegação forçada
            GameData.showHauntedMarks.value = false
            GameData.triggerForcedNavigation.value = true // O MainActivity vai capturar isto
        }
    }

    val chatState = rememberChatState(contact)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val messages = chatState.messages

    val typingPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                typingPlayer.value?.stop()
                typingPlayer.value?.release()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun playSound(fileName: String) {
        val resId = context.resources.getIdentifier(fileName, "raw", context.packageName)
        if (resId != 0) {
            try {
                if (fileName == "typing") {
                    if (typingPlayer.value?.isPlaying == true) return
                    val mp = MediaPlayer.create(context, resId)
                    typingPlayer.value = mp
                    mp.setOnCompletionListener {
                        it.release()
                        if (typingPlayer.value == it) typingPlayer.value = null
                    }
                    mp.start()
                } else {
                    typingPlayer.value?.let {
                        if (it.isPlaying) it.stop()
                        it.release()
                    }
                    typingPlayer.value = null
                    val mp = MediaPlayer.create(context, resId)
                    mp.setOnCompletionListener { it.release() }
                    mp.start()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    if (isGhostChat) {
        LaunchedEffect(Unit) {
            if (messages.isEmpty()) {
                val ghostMessages = listOf(
                    "Tu não estás a ver a obra, pois não?",
                    "Tu estás a ver a minha memória.",
                    "Eu sou o Pilar 4. E agora tu estás cá dentro comigo."
                )
                delay(2000)
                ghostMessages.forEach { msg ->
                    chatState.isTyping.value = true
                    playSound("typing")
                    delay(2000)
                    chatState.isTyping.value = false
                    playSound("received")
                    messages.add(Message(content = msg, isFromPlayer = false, timestamp = "Agora", isRead = true))
                    delay(2500)
                }
                delay(4000)
                showFinalGlitch = true
                val resId = context.resources.getIdentifier("static_burst", "raw", context.packageName)
                if (resId != 0) {
                    val mp = MediaPlayer.create(context, resId)
                    mp.start()
                }
                delay(3000)
                (context as? Activity)?.finish()
            }
        }
    }

    LaunchedEffect(messages.size, chatState.isTyping.value) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F1510))
        ) {
            FakeStatusBar()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F2C34))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = if (isGhostChat) Color.Gray else Color.White,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable(enabled = !backPressed && !isGhostChat && !GameData.showHauntedMarks.value) {
                            backPressed = true
                            onBack()
                        }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.first().toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                    Text(
                        text = if (chatState.isTyping.value) "A escrever..." else contact.status,
                        color = if (chatState.isTyping.value) Color(0xFF25D366) else Color(0xFFA0A0A0),
                        fontSize = 12.sp,
                        fontWeight = if (chatState.isTyping.value) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message)
                }

                if (chatState.isTyping.value) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            if (!isGhostChat && !isDesconhecido) {
                AnimatedVisibility(visible = chatState.currentOptions.value.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F2C34))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "A tua resposta:",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        chatState.currentOptions.value.forEach { option ->
                            Button(
                                onClick = {
                                    chatState.selectOption(
                                        option,
                                        scope,
                                        onTypingSound = { playSound("typing") },
                                        onMessageSound = { playSound("received") }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2A3942),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = option.text,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            if (!isGhostChat && chatState.currentOptions.value.isEmpty() && !chatState.isTyping.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F2C34))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aguardando contacto...",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- OVERLAY: PONTOS DE INTERROGAÇÃO (ASSOMBRAÇÃO) ---
        if (GameData.showHauntedMarks.value) {
            HauntedQuestionMarksOverlay()
        }

        if (showFinalGlitch) {
            FinalGlitchOverlay()
        }
    }
}

// NOVO COMPONENTE: PONTOS DE INTERROGAÇÃO VERMELHOS
@Composable
fun HauntedQuestionMarksOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(200, easing = LinearEasing), RepeatMode.Reverse),
        label = "alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {
        val w = size.width
        val h = size.height
        val random = Random(System.currentTimeMillis())

        // Desenha '?' vermelhos aleatórios
        repeat(50) {
            val x = random.nextFloat() * w
            val y = random.nextFloat() * h

            // Simulação de texto '?' (duas linhas e um ponto)
            // Curva
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(x, y)
                quadraticBezierTo(x + 20, y - 20, x + 40, y)
                lineTo(x + 20, y + 40)
            }
            drawPath(path, Color.Red.copy(alpha = alpha), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))
            // Ponto
            drawCircle(Color.Red.copy(alpha = alpha), radius = 4f, center = Offset(x + 20, y + 55))
        }
    }
}

// ... FinalGlitchOverlay, MessageBubble e TypingIndicator mantêm-se iguais
@Composable
fun FinalGlitchOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "glitch")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(50, easing = LinearEasing), RepeatMode.Restart),
        label = "offset"
    )

    Canvas(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val w = size.width
        val h = size.height
        val random = Random(System.currentTimeMillis())

        repeat(500) {
            val x = random.nextFloat() * w
            val y = random.nextFloat() * h
            val color = if (random.nextBoolean()) Color.Red else Color.White
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(random.nextFloat() * 50f, 2f)
            )
        }

        repeat(20) {
            val x = random.nextFloat() * w
            val y = random.nextFloat() * h
            drawRect(
                color = Color.White.copy(alpha = 0.8f),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(random.nextFloat() * 100f, random.nextFloat() * 20f)
            )
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val bubbleColor = if (message.isFromPlayer) Color(0xFF005C4B) else Color(0xFF1F2C34)
    val alignment = if (message.isFromPlayer) Alignment.End else Alignment.Start
    val shape = if (message.isFromPlayer) {
        RoundedCornerShape(topStart = 12.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
    } else {
        RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(bubbleColor, shape)
                .padding(10.dp)
        ) {
            Column {
                if (message.imageResId != null) {
                    Image(
                        painter = painterResource(id = message.imageResId),
                        contentDescription = "Anexo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(bottom = 8.dp)
                    )
                }

                if (message.content.isNotEmpty()) {
                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.timestamp,
                    color = Color(0xFF8696A0),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .background(Color(0xFF1F2C34), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "...",
            color = Color(0xFF25D366),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}