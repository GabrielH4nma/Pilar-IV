package com.example.protocolosombra.ui

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class CaveState {
    BOOT,
    INTRO,
    ESCOLHA_1, // Ping ou Diagnóstico
    LABIRINTO, // Decisão da Rota (A ou B)
    ROTA_A, // Clínica
    ROTA_B, // Obra
    PERSEGUICAO, // Encontro com o Homem do Casaco Vermelho
    ESCONDER,
    CORRER,
    NUCLEO, // Encontro com a Sofia
    CLIMAX, // A escolha final
    FINAL_A, // Sobrevivente Cobarde
    FINAL_B, // Mártir de Betão
    FINAL_C // Demolição (Secreto - Requer lógica extra no futuro)
}

@Composable
fun CaveGameScreen() {
    val context = LocalContext.current
    // Bloqueia o botão de voltar: NÃO HÁ SAÍDA
    BackHandler(enabled = true) { }

    var gameState by remember { mutableStateOf(CaveState.BOOT) }
    var textToShow by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    // Novo estado para controlar a corrupção do Final B
    var isFinalBCorrupted by remember { mutableStateOf(false) }

    // Players de áudio
    val ambiencePlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    val typingPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

    // Função para tocar som de fundo (Ambiente - Zumbido 19Hz)
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

    // Função para tocar som de escrita (Typing)
    fun playTypingSound(isHard: Boolean) {
        val soundName = if (isHard) "terminal_typing_hard" else "terminal_typing"
        val resId = context.resources.getIdentifier(soundName, "raw", context.packageName)
        if (resId != 0) {
            try {
                if (typingPlayer.value?.isPlaying == true) return

                val mp = MediaPlayer.create(context, resId)
                mp.start()
                mp.setOnCompletionListener {
                    it.release()
                    if (typingPlayer.value == it) typingPlayer.value = null
                }
                typingPlayer.value = mp
            } catch (e: Exception) {}
        }
    }

    fun stopTypingSound() {
        try {
            if (typingPlayer.value?.isPlaying == true) {
                typingPlayer.value?.stop()
            }
            typingPlayer.value?.release()
            typingPlayer.value = null
        } catch (e: Exception) {}
    }

    LaunchedEffect(Unit) {
        playAmbience()
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                ambiencePlayer.value?.stop()
                ambiencePlayer.value?.release()
                typingPlayer.value?.stop()
                typingPlayer.value?.release()
            } catch (e: Exception) {}
        }
    }

    // Texto do guião
    val fullText = when (gameState) {
        CaveState.BOOT -> """
            > INICIANDO SISTEMA DE SEGURANÇA KRONOS v.4.0...
            > ERRO CRÍTICO: KERNEL DE MEMÓRIA CORROMPIDO.
            > TENTATIVA DE RECUPERAÇÃO DE DADOS...
            > ACEDENDO AO SETOR: "SOFIA_MENDES_CONSCIOUSNESS.DAT"
            
            > Sincronização Neural: 12%... 45%... 100%.
        """.trimIndent()

        CaveState.INTRO -> """
            Tu não estás no teu corpo.
            Onde deviam estar as tuas mãos, sentes apenas frio.
            Onde deviam estar os teus pulmões, sentes pó de pedra.
            O ar é pesado.
            Cheira a humidade antiga e a cobre queimado.
            
            [IMAGEM: INTERFERÊNCIA - MÃO DE CABOS ELÉTRICOS]
            
            Uma voz ecoa na tua cabeça. É um pensamento... que não é teu.
            
            VOZ (SOFIA): "Eles... não me deixam... dormir."
            
            > O sistema oferece-te um comando básico.
        """.trimIndent()

        CaveState.ESCOLHA_1 -> """
            > COMANDO ACEITE.
            
            O corredor à tua frente estica-se infinitamente.
            As luzes de emergência piscam num ritmo cardíaco.
            TUM-TUM. TUM-TUM.
            
            VOZ (SOFIA): "O Dr. Luz disse que era stress. Disse que o prédio não falava. Mas eu ouvia-o. Ele tem fome."
            
            Chegas a uma bifurcação na memória da Sofia.
            O caminho divide-se em dois traumas distintos.
        """.trimIndent()

        CaveState.ROTA_A -> """
            O chão muda de betão bruto para azulejos brancos imaculados.
            Mas estão escorregadios.
            Não é água. É um líquido viscoso e transparente.
            
            [IMAGEM: INTERFERÊNCIA - RADIOGRAFIA COM ESTRUTURA DE FERRO]
            
            Encontras um relatório médico no chão.
            Paciente: Sofia M | Sintomas: Paranoia | Tratamento: Composto K-7 (Experimental).
            
            Sentes uma picada no braço.
            Ele estava a preparar o corpo dela para aceitar o enxerto.
            Uma agulha que já não existe.
            
            VOZ (SOFIA): "O remédio fazia o zumbido parar... mas fazia as paredes aproximarem-se. Ele sabia. Ele sempre soube."
        """.trimIndent()

        CaveState.ROTA_B -> """
            O ambiente fica escuro.
            O cheiro a terra molhada é sufocante.
            Vês capacetes de proteção amarelos semi-enterrados no chão, como crânios fósseis.
            
            [IMAGEM: INTERFERÊNCIA - BOTA FUNDIDA COM O CHÃO]
            
            Encontras a ferramenta de nível do Tiago.
            Está dobrada ao meio, como se o metal tivesse derretido.
            
            Ouve-se o som de uma betoneira a girar.
            Mas o som é húmido. Como se não estivesse a bater pedra.
            
            VOZ (SOFIA): "O Tiago encontrou dentes na mistura, dentes humanos...
            Ele quis ir à polícia.
            Foi... engolido... nessa mesma noite."
        """.trimIndent()

        CaveState.PERSEGUICAO -> """
            > ALERTA DE PROXIMIDADE
            > DETETADA ENTIDADE HOSTIL
            > NÍVEL DE AMEAÇA: EXTREMO
            
            Ouvem-se passos. Pesados. Lentos.
            O som de botas de couro a arrastar em vidro partido.
            Ele não corre. Ele não precisa.
            
            [IMAGEM: INTERFERÊNCIA - SILHUETA CASACO VERMELHO]
            
            Tu estás fisicamente escondido num poço de elevador desativado, a segurar o telemóvel.
            Mas na rede neural, ele consegue cheirar o teu medo (cortisol).
            
            VOZ (SOFIA): "Ele é o sistema imunitário do prédio, nós somos o vírus. Foge!"
        """.trimIndent()

        CaveState.ESCONDER -> """
            > Desligas os processos não essenciais.
            > Ficas no escuro.
            > Os passos passam por ti... param... e continuam.
            
            Sobreviveste, mas a bateria caiu 10%.
        """.trimIndent()

        CaveState.CORRER -> """
            > Corres pelos cabos de fibra ótica.
            > Sentes o calor do processador.
            > O Homem do Casaco Vermelho tenta agarrar o teu sinal, mas és mais rápido.
            
            Escapas, mas o sistema sobreaquece.
        """.trimIndent()

        CaveState.NUCLEO -> """
            Chegaste ao centro. O Pilar Mestre.
            Aqui, a realidade desfaz-se.
            Não há paredes.
            Há apenas uma massa pulsante de betão cinzento, ferro enferrujado e... bioluminescência.
            
            [IMAGEM: INTERFERÊNCIA - ROSTO DE SOFIA NO BETÃO]
            
            Ela está à tua frente.
            Metade mulher, metade arquitetura.
            Fios de cobre entram pelas suas têmporas.
            A boca dela não se mexe, mas a voz está em todo o lado.
            
            VOZ (SOFIA): "Estás a ver? É lindo... e terrível. Eu sou a fundação. Eu seguro os 40 andares. Sinto cada pessoa que caminha lá em cima, sinto as discussões, os amores, o medo."
            
            *O telemóvel vibra violentamente.*
            A bateria está crítica (2%).
            O Homem do Casaco Vermelho encontrou a tua localização física.
            Ele está a bater na porta de metal do poço do elevador onde estás.
            BANG. BANG. BANG.
            
            VOZ (SOFIA): "Tens pouco tempo. O meu acesso de administrador ainda funciona. Posso fazer uma coisa por ti. Só uma."
        """.trimIndent()

        CaveState.CLIMAX -> """
            A porta está a ceder.
            *O metal range*
            *O ecrã mostra duas linhas de código final*
        """.trimIndent()

        CaveState.FINAL_A -> """
            > CONFIRMAR? ISTO IRÁ DRENAR A ENERGIA RESTANTE DO NÚCLEO.
            > COMANDO ACEITE.
            
            Ouve-se um silvo hidráulico.
            As portas do lobby, 20 andares abaixo, destrancam-se.
            As luzes de emergência acendem-se, cegando o Homem do Casaco Vermelho momentaneamente.
            
            VOZ (SOFIA): "Ah... eu percebo. Tu queres viver. Corre. Não olhes para trás."
            
            [IMAGEM: INTERFERÊNCIA - ROSTO PETRIFICADO]
            
            O telemóvel morre.
            Estás no escuro, mas a porta de saída está aberta.
            Tu corres.
            
            [ FIM DO JOGO - FINAL A: O SOBREVIVENTE COBARDE ]
        """.trimIndent()

        CaveState.FINAL_B -> """
            > CONECTANDO AO SERVIDOR GLOBAL...
            > TRANSFERÊNCIA DE MASSA INICIADA.
            
            A porta do poço do elevador abre-se.
            O Homem do Casaco Vermelho entra. Ele é enorme. Cheira a ozono e sangue velho.
            
            VOZ (SOFIA): "Obrigada, agora eu posso descansar."
            
            > UPLOAD: 100%.
            > O Homem levanta a mão de betão para te esmagar.
            > Tu fechas os olhos.
            > Mas a dor não vem. Sentes... expansão. A tua mente sai do corpo e entra na rede.
            
            [IMAGEM: INTERFERÊNCIA - CÓDIGO BINÁRIO "BEM-VINDO A CASA"]
            
            [ FIM DO JOGO - FINAL B: O MÁRTIR DE BETÃO ]
        """.trimIndent()

        CaveState.FINAL_C -> """
            > CHAVE CRIPTOGRÁFICA ACEITE.
            > EXECUTANDO PROTOCOLO SAMSON...
            
            O sistema treme. Ouve-se um zumbido de 440hz.
            O betão começa a rachar. O Pilar IV entra em ressonância destrutiva.
            
            VOZ (SOFIA): "Sim... SIM! Vamos deitar tudo abaixo!"
            
            > O teto colapsa sobre ti e sobre o Homem do Casaco Vermelho.
            > Não há mais dor.
            > Apenas o som de milhões de toneladas a cair por terra.
            
            [ FIM DO JOGO - FINAL SECRETO: A DEMOLIÇÃO ]
        """.trimIndent()

        else -> ""
    }

    LaunchedEffect(fullText) {
        textToShow = ""
        isTyping = true

        val isTenseScene = gameState == CaveState.PERSEGUICAO || gameState == CaveState.NUCLEO || gameState == CaveState.CLIMAX
        val charDelay = if (gameState == CaveState.BOOT) 50L else 30L

        fullText.forEach { char ->
            textToShow += char

            if (textToShow.length % 3 == 0) {
                playTypingSound(isTenseScene)
            }

            if (char == '\n') delay(200L)
            else if (char == '.' || char == '?' || char == '!') delay(150L)
            else delay(charDelay)
        }

        isTyping = false
        stopTypingSound()

        // --- EFEITO ESPECIAL PARA O FINAL B ---
        if (gameState == CaveState.FINAL_B) {
            delay(2000) // Espera um pouco com a imagem normal
            // Simula glitch sonoro
            val resId = context.resources.getIdentifier("static_burst", "raw", context.packageName)
            if (resId != 0) {
                val mp = MediaPlayer.create(context, resId)
                mp.start()
            }
            // Troca para a imagem corrompida
            isFinalBCorrupted = true
        }

        // Transições automáticas
        if (gameState == CaveState.BOOT) {
            delay(1500)
            gameState = CaveState.INTRO
        } else if (gameState == CaveState.ESCONDER || gameState == CaveState.CORRER) {
            delay(3000)
            gameState = CaveState.NUCLEO
        } else if (gameState == CaveState.NUCLEO) {
            delay(4000)
            gameState = CaveState.CLIMAX
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        // ÁREA VISUAL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 16.dp)
                .background(Color(0xFF050505), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF003300), RoundedCornerShape(8.dp)),
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
                    CaveState.FINAL_B -> if (isFinalBCorrupted) "cave_binary_code2" else "cave_binary_code" // Troca dinâmica
                    CaveState.FINAL_C -> "cave_building_collapse"
                    else -> null
                }

                if (imageResName != null) {
                    val imageResId = context.resources.getIdentifier(imageResName, "drawable", context.packageName)
                    if (imageResId != 0) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = "Visual da Cave",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        )
                        ScanlinesOverlay()
                    } else {
                        Text("IMAGEM: $imageResName", color = Color.Red)
                    }
                }
            }
        }

        // Texto do Terminal
        Text(
            text = textToShow,
            color = if (gameState == CaveState.PERSEGUICAO) Color.Red else Color(0xFF00FF00),
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth()
        )

        if (isTyping) {
            val cursorAlpha by rememberInfiniteTransition(label = "cursor").animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "alpha"
            )
            Text("█", color = Color(0xFF00FF00).copy(alpha = cursorAlpha), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Opções de Escolha
        if (!isTyping) {
            when (gameState) {
                CaveState.INTRO -> {
                    CaveOption("[PING: Tentar localizar Sofia]") { gameState = CaveState.ESCOLHA_1 }
                    CaveOption("[DIAGNÓSTICO: Analisar o ambiente]") { gameState = CaveState.ESCOLHA_1 }
                }
                CaveState.ESCOLHA_1 -> {
                    CaveOption("[ROTA A: Seguir cheiro a antissético (Clínica)]") { gameState = CaveState.ROTA_A }
                    CaveOption("[ROTA B: Seguir som de maquinaria (Obra)]") { gameState = CaveState.ROTA_B }
                }
                CaveState.ROTA_A, CaveState.ROTA_B -> {
                    CaveOption("[AVANÇAR]") { gameState = CaveState.PERSEGUICAO }
                }
                CaveState.PERSEGUICAO -> {
                    CaveOption("[ESCONDER: Tentar camuflar sinal]") { gameState = CaveState.ESCONDER }
                    CaveOption("[CORRER: Forçar conexão até ao núcleo]") { gameState = CaveState.CORRER }
                }
                CaveState.CLIMAX -> {
                    CaveOption("[COMANDO: /OPEN_DOORS_EMERGENCY] (Sacrificar Sofia)") { gameState = CaveState.FINAL_A }
                    CaveOption("[COMANDO: /UPLOAD_DATA_SERVER_PUBLIC] (Salvar a verdade)") { gameState = CaveState.FINAL_B }
                    // CaveOption("/EXECUTE_PROTOCOL_SAMSON") { gameState = CaveState.FINAL_C } // Secreto
                }
                CaveState.FINAL_A, CaveState.FINAL_B, CaveState.FINAL_C -> {
                    Text(
                        text = "> SISTEMA DESLIGADO.",
                        color = Color.Red,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                else -> {}
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ... (Resto das funções auxiliares CaveOption, BootAnimation, ScanlinesOverlay mantêm-se iguais) ...
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
        // Círculos concêntricos
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