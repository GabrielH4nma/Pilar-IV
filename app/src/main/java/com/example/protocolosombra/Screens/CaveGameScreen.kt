package com.example.protocolosombra.ui

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.protocolosombra.R

enum class CaveState {
    BOOT,
    INTRO,
    ESCOLHA_1,
    LABIRINTO,
    ROTA_A,
    ROTA_B,
    PERSEGUICAO,
    ESCONDER,
    CORRER,
    NUCLEO,
    CLIMAX,
    FINAL_A,
    FINAL_B,
    FINAL_C
}

@Composable
fun CaveGameScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    BackHandler(enabled = true) { }

    var gameState by remember { mutableStateOf(CaveState.BOOT) }

    // Controlo de Narrativa
    var currentDialogueLines by remember { mutableStateOf(listOf<String>()) }
    val displayedLines = remember { mutableStateListOf<String>() }
    var nextLineIndex by remember { mutableStateOf(0) }
    var isTyping by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }

    // Efeitos Visuais
    var isFinalBCorrupted by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val redFlashAlpha = remember { Animatable(0f) }

    // Variáveis para a Sequência Final (Blackout)
    val blackoutAlpha = remember { Animatable(0f) }
    var finalMessage by remember { mutableStateOf("") }
    var finalMessageColor by remember { mutableStateOf(Color.White) }

    // Players de áudio
    val ambiencePlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    val typingPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

    // --- FUNÇÕES DE ÁUDIO ---
    fun playAmbience() {
        val resId = context.resources.getIdentifier("cave_ambience", "raw", context.packageName)
        if (resId != 0) {
            try {
                if (ambiencePlayer.value == null) {
                    val mp = MediaPlayer.create(context, resId)
                    mp.isLooping = true
                    mp.setVolume(0.6f, 0.6f)
                    mp.start()
                    ambiencePlayer.value = mp
                }
            } catch (e: Exception) {}
        }
    }

    // Inicia o som de escrita em loop
    fun startTypingSound(isHard: Boolean) {
        val soundName = if (isHard) "terminal_typing_hard" else "terminal_typing"
        val resId = context.resources.getIdentifier(soundName, "raw", context.packageName)
        if (resId != 0) {
            try {
                if (typingPlayer.value == null) {
                    typingPlayer.value = MediaPlayer.create(context, resId).apply {
                        isLooping = true // Loop contínuo enquanto escreve
                        setVolume(0.8f, 0.8f)
                        start()
                    }
                } else if (typingPlayer.value?.isPlaying == false) {
                    typingPlayer.value?.start()
                }
            } catch (e: Exception) {}
        }
    }

    // Para o som de escrita imediatamente
    fun stopTypingSound() {
        try {
            if (typingPlayer.value?.isPlaying == true) {
                typingPlayer.value?.pause()
                typingPlayer.value?.seekTo(0)
            }
        } catch (e: Exception) {}
    }

    // --- FUNÇÃO DE EFEITO DE ESCRITA (TYPEWRITER) MELHORADA ---
    suspend fun typeText(text: String) {
        isTyping = true
        displayedLines.add("")
        val currentIndex = displayedLines.lastIndex

        // Inicia o áudio
        startTypingSound(gameState == CaveState.PERSEGUICAO)

        text.forEachIndexed { index, char ->
            displayedLines[currentIndex] = displayedLines[currentIndex] + char

            // Lógica de Ritmo:
            // 1. Pausa em pontuação para realismo
            val punctuationDelay = if (char in listOf('.', '!', '?', ':')) 200L else 0L
            // 2. Hesitação aleatória humana
            val randomJitter = Random.nextLong(10, 50)
            // 3. Velocidade base (mais rápido em perseguição)
            val baseSpeed = if (gameState == CaveState.PERSEGUICAO) 10L else 30L

            delay(baseSpeed + randomJitter + punctuationDelay)

            // Vibração em palavras chave ou fim de frases tensas
            if (gameState == CaveState.PERSEGUICAO && (char == '!' || char == '.')) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }

        // Corta o som imediatamente ao acabar a linha
        stopTypingSound()
        isTyping = false
    }

    // --- EFEITOS DE AMBIENTE ---
    LaunchedEffect(gameState) {
        // Tremor
        launch {
            if (gameState == CaveState.PERSEGUICAO || gameState == CaveState.CLIMAX) {
                while (true) {
                    shakeOffset.animateTo(3f, animationSpec = tween(50))
                    shakeOffset.animateTo(-3f, animationSpec = tween(50))
                    shakeOffset.animateTo(0f, animationSpec = tween(50))
                    delay(Random.nextLong(100, 2500))
                }
            } else {
                shakeOffset.snapTo(0f)
            }
        }

        // Flash Vermelho
        launch {
            if (gameState == CaveState.PERSEGUICAO) {
                while(true) {
                    redFlashAlpha.animateTo(0.2f, tween(100))
                    redFlashAlpha.animateTo(0f, tween(300))
                    delay(Random.nextLong(2000, 5000))
                }
            } else {
                redFlashAlpha.snapTo(0f)
            }
        }
    }

    // --- SEQUÊNCIA DE FINAL (BLACKOUT) ---
    LaunchedEffect(showOptions, gameState) {
        if (showOptions && (gameState == CaveState.FINAL_A || gameState == CaveState.FINAL_B || gameState == CaveState.FINAL_C)) {
            // Espera 5 segundos após o texto terminar
            delay(5000)

            // Define a mensagem final baseada no final escolhido
            when (gameState) {
                CaveState.FINAL_A -> {
                    finalMessage = "RELATÓRIO KRONOS:\nSUJEITO 04 ESCAPOU.\nESTADO MENTAL: COMPROMETIDO."
                    finalMessageColor = Color.LightGray
                }
                CaveState.FINAL_B -> {
                    finalMessage = "> INTEGRAÇÃO COMPLETA.\n> BEM-VINDA À REDE, SOFIA."
                    finalMessageColor = Color(0xFF00FF00) // Verde Matrix
                }
                CaveState.FINAL_C -> {
                    finalMessage = "SINAL PERDIDO\n----------------\nSEM DADOS DE TELEMETRIA\nESTRUTURA COLAPSADA"
                    finalMessageColor = Color(0xFFFF0000) // Vermelho Alerta
                }
                else -> {}
            }

            // Inicia o fade out para preto (3 segundos)
            blackoutAlpha.animateTo(1f, animationSpec = tween(3000))
        }
    }

    // --- CONTEÚDO DA NARRATIVA ---
    fun loadSceneData(state: CaveState): List<String> {
        return when (state) {
            CaveState.BOOT -> listOf(
                "> INICIANDO SISTEMA DE SEGURANÇA KRONOS v.4.0...",
                "> KERNEL DE MEMÓRIA CORROMPIDO.",
                "> RECUPERANDO SETOR: \"SOFIA_MENDES.DAT\"",
                "> Sincronização Neural: 100%."
            )
            CaveState.INTRO -> listOf(
                "Tu não estás no teu corpo.",
                "Onde deviam estar as tuas mãos, sentes apenas frio.",
                "Onde deviam estar os teus pulmões, sentes pó de pedra.",
                "O ar é pesado. Cheira a humidade antiga e a cobre queimado.",
                "Uma voz ecoa na tua cabeça...",
                "VOZ (SOFIA): \"Eles... não me deixam... dormir.\""
            )
            CaveState.ESCOLHA_1 -> listOf(
                "> ACESSO CONCEDIDO.",
                "O corredor à tua frente estica-se infinitamente.",
                "Luzes de emergência piscam num ritmo cardíaco.",
                "TUM-TUM. TUM-TUM.",
                "VOZ (SOFIA): \"O Dr. Luz disse que o prédio não falava.\"",
                "VOZ (SOFIA): \"Mas ele tem fome.\"",
                "Chegas a uma bifurcação na memória da Sofia."
            )
            CaveState.ROTA_A -> listOf(
                "O chão muda para azulejos brancos imaculados.",
                "Mas estão cobertos de um líquido viscoso.",
                "Encontras um relatório médico no chão.",
                "Paciente: Sofia M | Sintomas: Paranoia.",
                "Sentes uma picada fantasma no braço.",
                "Estavam a preparar o corpo dela para a fundação.",
                "VOZ (SOFIA): \"O remédio fazia as paredes aproximarem-se.\""
            )
            CaveState.ROTA_B -> listOf(
                "O ambiente fica escuro.",
                "Vês capacetes de proteção enterrados no chão como crânios.",
                "Encontras a ferramenta de nível do Tiago.",
                "Está dobrada ao meio, derretida.",
                "Ouve-se o som de uma betoneira... mas o som é húmido.",
                "VOZ (SOFIA): \"O Tiago encontrou dentes na mistura...\""
            )
            CaveState.PERSEGUICAO -> listOf(
                "> ALERTA: ENTIDADE HOSTIL DETETADA",
                "Passos. Pesados. Lentos.",
                "Botas de couro a arrastar em vidro.",
                "Ele não corre. Ele não precisa.",
                "Na rede neural, ele consegue cheirar o teu medo.",
                "VOZ (SOFIA): \"FOGE!\""
            )
            CaveState.ESCONDER -> listOf(
                "> Desligas os processos.",
                "> Escuro absoluto.",
                "Os passos passam por ti...",
                "Param...",
                "E continuam.",
                "Sobreviveste. Bateria: 90%."
            )
            CaveState.CORRER -> listOf(
                "> Corres pelos cabos de fibra ótica.",
                "Sentes o calor do processador.",
                "Ele tenta agarrar o teu sinal...",
                "Mas és mais rápido.",
                "Escapas. Sistema sobreaquecido."
            )
            CaveState.NUCLEO -> listOf(
                "O Pilar Mestre.",
                "Não há paredes aqui.",
                "Apenas betão pulsante e bioluminescência.",
                "Ela está à tua frente. Fundida com a estrutura.",
                "Fios de cobre entram pelas suas têmporas.",
                "VOZ (SOFIA): \"Eu sou a fundação. Eu seguro os 40 andares.\"",
                "*O telemóvel vibra violentamente*",
                "BANG. BANG. BANG.",
                "Ele encontrou-te.",
                "VOZ (SOFIA): \"Posso fazer uma coisa por ti. Só uma.\""
            )
            CaveState.CLIMAX -> listOf(
                "A porta está a ceder.",
                "*O metal range*"
            )
            CaveState.FINAL_A -> listOf(
                "> DRENAR ENERGIA DO NÚCLEO?",
                "> CONFIRMADO.",
                "Silvo hidráulico.",
                "As portas do lobby abrem-se lá em baixo.",
                "VOZ (SOFIA): \"Ah... tu queres viver. Corre.\"",
                "O telemóvel morre.",
                "Estás no escuro, mas a saída está aberta.",
                "[ FIM: O SOBREVIVENTE COBARDE ]"
            )
            CaveState.FINAL_B -> listOf(
                "> UPLOAD PARA SERVIDOR GLOBAL...",
                "A porta abre-se. Ele entra.",
                "VOZ (SOFIA): \"Obrigada.\"",
                "> UPLOAD: 100%.",
                "Ele esmaga o telemóvel.",
                "Mas tu já não estás lá.",
                "A tua mente está na rede.",
                "[ FIM: O MÁRTIR DE BETÃO ]"
            )
            else -> listOf()
        }
    }

    // --- RESET DE CENA ---
    LaunchedEffect(gameState) {
        currentDialogueLines = loadSceneData(gameState)
        displayedLines.clear()
        nextLineIndex = 0
        showOptions = false

        // BOOT automático
        if (gameState == CaveState.BOOT) {
            loadSceneData(gameState).forEach { line ->
                typeText(line)
                delay(200)
            }
            delay(1000)
            gameState = CaveState.INTRO
        } else {
            // Inicia primeira linha automaticamente
            if (currentDialogueLines.isNotEmpty()) {
                delay(300)
                val line = currentDialogueLines[0]
                nextLineIndex = 1
                typeText(line)
            }
        }
        playAmbience()

        if (gameState == CaveState.FINAL_B) {
            delay(2000)
            isFinalBCorrupted = true
        }
    }

    // --- AVANÇAR TEXTO ---
    fun advanceText() {
        if (isTyping || showOptions) return

        if (nextLineIndex < currentDialogueLines.size) {
            val line = currentDialogueLines[nextLineIndex]
            nextLineIndex++
            scope.launch { typeText(line) }
        } else {
            showOptions = true
        }
    }

    // --- UI ---
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camada de Flash Vermelho
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red.copy(alpha = redFlashAlpha.value))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .graphicsLayer { translationX = shakeOffset.value }
        ) {
            // ÁREA VISUAL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(bottom = 16.dp)
                    .background(Color(0xFF050505), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF004400), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (gameState == CaveState.BOOT) {
                    BootAnimation()
                } else {
                    val imageResName = when(gameState) {
                        CaveState.INTRO -> "cave_hand_wires"
                        CaveState.ESCOLHA_1, CaveState.LABIRINTO -> "cave_hallway"
                        CaveState.ROTA_A -> "cave_xray"
                        CaveState.ROTA_B -> "cave_boots_melted"
                        CaveState.PERSEGUICAO -> "cave_red_coat_silhouette"
                        CaveState.NUCLEO, CaveState.CLIMAX -> "cave_sofia_face_wall"
                        CaveState.FINAL_A -> "cave_face_petrified"
                        CaveState.FINAL_B -> if (isFinalBCorrupted) "cave_binary_code2" else "cave_binary_code"
                        CaveState.FINAL_C -> "cave_building_collapse"
                        else -> null
                    }

                    if (imageResName != null) {
                        val imageResId = context.resources.getIdentifier(imageResName, "drawable", context.packageName)
                        if (imageResId != 0) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                            )
                            ScanlinesOverlay()
                        }
                    }
                }
            }

            // ÁREA DE TEXTO
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { advanceText() }
            ) {
                val listState = rememberLazyListState()

                LaunchedEffect(displayedLines.size) {
                    if (displayedLines.isNotEmpty()) listState.animateScrollToItem(displayedLines.size - 1)
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(displayedLines) { index, line ->
                        val isLastLine = index == displayedLines.size - 1

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = line,
                                color = if (gameState == CaveState.PERSEGUICAO) Color(0xFFFF4444) else Color(0xFF00FF00),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                fontWeight = if (isLastLine) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(vertical = 4.dp)
                                    .alpha(if (isLastLine) 1f else 0.5f)
                            )
                        }
                    }

                    // Cursor a piscar na última linha (indica que está à espera)
                    if (!showOptions && !isTyping && displayedLines.isNotEmpty() && nextLineIndex < currentDialogueLines.size) {
                        item {
                            val cursorAlpha by rememberInfiniteTransition(label = "cursor").animateFloat(
                                initialValue = 0f, targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "alpha"
                            )
                            Text("█", color = Color(0xFF00FF00).copy(alpha = cursorAlpha), fontSize = 16.sp)
                        }
                    }
                }
            }

            // OPÇÕES
            if (showOptions && blackoutAlpha.value == 0f) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (gameState) {
                        CaveState.INTRO -> {
                            CaveOption(">> PING: LOCALIZAR SOFIA") { gameState = CaveState.ESCOLHA_1 }
                            CaveOption(">> DIAGNÓSTICO: AMBIENTE") { gameState = CaveState.ESCOLHA_1 }
                        }
                        CaveState.ESCOLHA_1 -> {
                            CaveOption(">> ROTA A: CHEIRO A ANTISSÉTICO") { gameState = CaveState.ROTA_A }
                            CaveOption(">> ROTA B: SOM DE MÁQUINAS") { gameState = CaveState.ROTA_B }
                        }
                        CaveState.ROTA_A, CaveState.ROTA_B -> {
                            CaveOption(">> AVANÇAR") { gameState = CaveState.PERSEGUICAO }
                        }
                        CaveState.PERSEGUICAO -> {
                            CaveOption(">> ESCONDER") { gameState = CaveState.ESCONDER }
                            CaveOption(">> CORRER") { gameState = CaveState.CORRER }
                        }
                        CaveState.ESCONDER, CaveState.CORRER -> {
                            LaunchedEffect(Unit) { delay(1500); gameState = CaveState.NUCLEO }
                        }
                        CaveState.NUCLEO -> {
                            CaveOption(">> ESCUTAR SOFIA") { gameState = CaveState.CLIMAX }
                        }
                        CaveState.CLIMAX -> {
                            CaveOption(">> /OPEN_DOORS (SACRIFÍCIO)") { gameState = CaveState.FINAL_A }
                            CaveOption(">> /UPLOAD_DATA (VERDADE)") { gameState = CaveState.FINAL_B }
                        }
                        else -> {}
                    }
                }
            }
        }

        // --- OVERLAY DE FINAL (BLACKOUT) ---
        if (blackoutAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = blackoutAlpha.value))
                    .clickable(enabled = false) {}, // Bloqueia cliques
                contentAlignment = Alignment.Center
            ) {
                if (blackoutAlpha.value >= 0.9f) {
                    Text(
                        text = finalMessage,
                        color = finalMessageColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

// --- FUNÇÕES AUXILIARES ---

@Composable
fun CaveOption(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color(0xFF005500), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BootAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "boot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFF00FF00).copy(alpha = alpha),
            radius = 10f,
            center = center
        )
        drawCircle(
            color = Color(0xFF00FF00).copy(alpha = alpha * 0.5f),
            radius = 30f,
            center = center,
            style = Stroke(width = 2f)
        )
    }
}

@Composable
fun ScanlinesOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineHeight = 4.dp.toPx()
        for (y in 0..size.height.toInt() step lineHeight.toInt() * 2) {
            drawRect(
                color = Color.Black.copy(alpha = 0.3f),
                topLeft = Offset(0f, y.toFloat()),
                size = androidx.compose.ui.geometry.Size(size.width, lineHeight)
            )
        }
    }
}