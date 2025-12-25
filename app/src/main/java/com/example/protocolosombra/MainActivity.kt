package com.example.protocolosombra

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.protocolosombra.data.GameData
import com.example.protocolosombra.ui.*
import kotlinx.coroutines.delay

object Routes {
    const val HOME = "home"
    const val CHAT = "chat"
    const val CONVERSATION = "conversation/{contactId}"
    const val GALLERY = "gallery"
    const val BANK = "bank"
    const val NOTES = "notes"
    const val TRACKER = "tracker"
    const val SITECAM = "sitecam"
    const val EMAIL = "email"
    const val RADIO = "radio"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.sleep(500)
        setTheme(R.style.Theme_ProtocoloSombra)

        super.onCreate(savedInstanceState)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())

        setContent {
            GameNavigation()
        }
    }
}

@Composable
fun GameNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // --- GESTÃO DE ÁUDIO GLOBAL ---
    // Mantemos o estado da música aqui para que continue a tocar entre ecrãs
    var currentTrack by remember { mutableStateOf<MusicTrack?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

    // Função para parar e limpar o player
    fun releaseMediaPlayer() {
        try {
            mediaPlayer.value?.stop()
            mediaPlayer.value?.release()
            mediaPlayer.value = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Função global para controlar a música
    fun togglePlayPause(track: MusicTrack) {
        val selectedId = track.resourceId

        // Se for a mesma música, pausa/resume
        if (currentTrack?.id == track.id && mediaPlayer.value != null) {
            if (isPlaying) {
                mediaPlayer.value?.pause()
                isPlaying = false
            } else {
                mediaPlayer.value?.start()
                isPlaying = true
            }
            return
        }

        // Se for nova música
        releaseMediaPlayer()

        if (selectedId != null && selectedId != 0) {
            try {
                val mp = MediaPlayer.create(context, selectedId)
                mp.isLooping = true // Opcional: repetir a música
                mp.start()
                mediaPlayer.value = mp
                isPlaying = true
                currentTrack = track
            } catch (e: Exception) {
                e.printStackTrace()
                isPlaying = false
            }
        } else {
            // Faixa placeholder
            isPlaying = true
            currentTrack = track
        }
    }

    // Garante que a música para se a app for fechada completamente
    DisposableEffect(Unit) {
        onDispose {
            releaseMediaPlayer()
        }
    }

    // --- LÓGICA DO JOGO ---

    LaunchedEffect(GameData.triggerForcedNavigation.value) {
        if (GameData.triggerForcedNavigation.value) {
            navController.popBackStack(Routes.HOME, inclusive = false)
            delay(1500)
            navController.navigate(Routes.TRACKER)
            GameData.isHauntedPlaybackActive.value = true
            GameData.triggerForcedNavigation.value = false
        }
    }

    LaunchedEffect(GameData.showNotificationPopup.value) {
        if (GameData.showNotificationPopup.value) {
            val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
            delay(4000)
            GameData.showNotificationPopup.value = false
        }
    }

    LaunchedEffect(GameData.isSiteCamInstalled.value) {
        if (GameData.isSiteCamInstalled.value && GameData.emails.isEmpty()) {
            delay(20000)
            if (GameData.emails.isEmpty()) {
                GameData.triggerGhostEmail()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = Routes.HOME) {
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToChat = { navController.navigate(Routes.CHAT) },
                    onNavigateToGallery = { navController.navigate(Routes.GALLERY) },
                    onNavigateToBank = { navController.navigate(Routes.BANK) },
                    onNavigateToNotes = { navController.navigate(Routes.NOTES) },
                    onNavigateToTracker = { navController.navigate(Routes.TRACKER) },
                    onNavigateToSiteCam = { navController.navigate(Routes.SITECAM) },
                    onNavigateToEmail = { navController.navigate(Routes.EMAIL) },
                    onNavigateToRadio = { navController.navigate(Routes.RADIO) }
                )
            }
            composable(Routes.CHAT) { ChatScreen(onBack = { navController.popBackStack() }, onNavigateToConversation = { id -> navController.navigate("conversation/$id") }) }
            composable(Routes.CONVERSATION, arguments = listOf(navArgument("contactId") { type = NavType.StringType })) {
                ConversationScreen(contactId = it.arguments?.getString("contactId") ?: "", onBack = { navController.popBackStack() })
            }
            composable(Routes.NOTES) { NotesScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.BANK) { BankScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.GALLERY) { GalleryScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.TRACKER) { TrackerScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SITECAM) { SiteCamScreen(onBack = { navController.popBackStack() }, onForceNavigateToChat = { navController.navigate("conversation/chefe") { popUpTo(Routes.HOME) { inclusive = false } } }) }
            composable(Routes.EMAIL) { EmailScreen(onBack = { navController.popBackStack() }) }

            // Passamos o estado e a função de controlo para o ecrã da Rádio
            composable(Routes.RADIO) {
                RadioScreen(
                    onBack = { navController.popBackStack() },
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    onTogglePlayPause = { track -> togglePlayPause(track) }
                )
            }
        }

        AnimatedVisibility(
            visible = GameData.showNotificationPopup.value,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            NotificationBanner(text = GameData.notificationContent.value, onDismiss = { GameData.showNotificationPopup.value = false })
        }
    }
}