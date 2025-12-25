package com.example.protocolosombra.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Modelo de dados para as músicas
data class MusicTrack(val id: Int, val title: String, val artist: String, val duration: String, val resourceId: Int? = null)

@Composable
fun RadioScreen(
    onBack: () -> Unit,
    currentTrack: MusicTrack?,
    isPlaying: Boolean,
    onTogglePlayPause: (MusicTrack) -> Unit,
    currentPosition: Float, // Posição atual em ms
    totalDuration: Float,    // Duração total em ms
    volumeLevel: Float,      // Volume (0.0 a 1.0)
    onVolumeChange: (Float) -> Unit, // Callback para mudança de volume
    onRestart: () -> Unit // Callback para reiniciar música
) {
    val context = LocalContext.current

    // Lista de músicas
    val tracks = remember {
        listOf(
            MusicTrack(
                id = 1,
                title = "Ecos do Betão",
                artist = "Pilar IV OST",
                duration = "03:45",
                resourceId = context.resources.getIdentifier("faixa1", "raw", context.packageName)
            ),
            MusicTrack(2, "Frequência Morta", "Pilar IV OST", "02:20"),
            MusicTrack(3, "Sombra Estática", "Pilar IV OST", "04:10"),
            MusicTrack(4, "Interferência", "Pilar IV OST", "01:55")
        )
    }

    // Calcula o progresso de 0 a 1 para a barra
    val progress = if (totalDuration > 0) (currentPosition / totalDuration).coerceIn(0f, 1f) else 0f

    // Função auxiliar para formatar milissegundos em MM:SS
    fun formatTime(ms: Float): String {
        val seconds = (ms / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "%02d:%02d".format(minutes, remainingSeconds)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        FakeStatusBar()

        // Cabeçalho
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack, "Voltar",
                tint = Color.White,
                modifier = Modifier.size(28.dp).clickable { onBack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("Rádio FM", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        // Leitor Visual (Área superior)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f) // Aumentei um pouco o peso para caber o volume
                .padding(24.dp)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // Animação de pulsação (só visual)
                val pulseAlpha by animateFloatAsState(
                    targetValue = if (isPlaying) 1f else 0.7f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ), label = "pulse"
                )

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFE91E63).copy(alpha = if (isPlaying) pulseAlpha else 1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.GraphicEq else Icons.Default.Audiotrack,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentTrack?.title ?: "Nenhuma Faixa",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = currentTrack?.artist ?: "Selecione uma música",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- BARRA DE PROGRESSO E TEMPO ---
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFFE91E63),
                        trackColor = Color.DarkGray,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (currentTrack != null) formatTime(totalDuration) else "--:--",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- CONTROLOS PRINCIPAIS ---
                // Row centralizada para os botões de controlo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center // Garante que tudo fica no meio
                ) {
                    // Botão Recomeçar (à esquerda)
                    IconButton(
                        onClick = { onRestart() },
                        enabled = currentTrack != null
                    ) {
                        Icon(
                            Icons.Default.Replay,
                            "Recomeçar",
                            tint = if (currentTrack != null) Color.LightGray else Color.DarkGray,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp)) // Espaço entre Recomeçar e Play

                    // Botão Play/Pause (Centro)
                    // Usamos Box para garantir o tamanho e forma corretos
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (currentTrack != null) Color(0xFFE91E63) else Color.Gray)
                            .clickable(enabled = currentTrack != null) {
                                currentTrack?.let { onTogglePlayPause(it) }
                            }
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Elemento "fantasma" à direita para equilibrar o layout se quisermos simetria perfeita
                    // Como só temos 2 botões funcionais no centro, podemos adicionar um Spacer do mesmo tamanho do primeiro botão + margem
                    // Ou simplesmente deixar assim se quisermos o Play no centro absoluto da Row

                    // Se o botão Play não parecer estar no centro geométrico da Row, podemos usar um Spacer dummy:
                    Spacer(modifier = Modifier.width(32.dp))
                    // Spacer do tamanho do botão Replay para forçar o Play a ficar no centro exato
                    Spacer(modifier = Modifier.size(48.dp)) // 32dp icon + padding default do IconButton
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- SLIDER DE VOLUME ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VolumeDown, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = volumeLevel,
                        onValueChange = { onVolumeChange(it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFFE91E63),
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.VolumeUp, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Lista de Músicas
        Text(
            "LISTA DE REPRODUÇÃO",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
        )

        LazyColumn(modifier = Modifier.weight(0.5f)) {
            items(tracks) { track ->
                TrackItem(
                    track = track,
                    isSelected = currentTrack?.id == track.id,
                    isPlaying = isPlaying && currentTrack?.id == track.id,
                    onClick = { onTogglePlayPause(track) }
                )
            }
        }
    }
}

@Composable
fun TrackItem(track: MusicTrack, isSelected: Boolean, isPlaying: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isSelected) Color(0xFF1E1E1E) else Color.Transparent)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isPlaying) {
                Icon(Icons.Default.GraphicEq, null, tint = Color(0xFFE91E63), modifier = Modifier.size(16.dp).width(30.dp))
            } else {
                Text(
                    text = "${track.id}",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.width(30.dp)
                )
            }

            Column {
                Text(
                    text = track.title,
                    color = if (isSelected) Color(0xFFE91E63) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = track.artist,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Text(track.duration, color = Color.Gray, fontSize = 12.sp)
    }
}