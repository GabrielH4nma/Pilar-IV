package com.example.protocolosombra

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.protocolosombra.data.*
import com.example.protocolosombra.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

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
    const val CAVE_APP = "cave_app"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.sleep(500)
        setTheme(R.style.Theme_ProtocoloSombra)

        super.onCreate(savedInstanceState)

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val isRebooted = sharedPref.getBoolean("isSystemRebooted", false)
        if (isRebooted) {
            GameData.isSystemRebooted.value = true
        }

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())

        setContent {
            GameNavigation(
                saveRebootState = {
                    val editor = sharedPref.edit()
                    editor.putBoolean("isSystemRebooted", true)
                    editor.apply()
                },
                finishApp = {
                    finishAndRemoveTask()
                }
            )
        }
    }
}

@Composable
fun GameNavigation(saveRebootState: () -> Unit = {}, finishApp: () -> Unit = {}) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Função local para tocar sons na MainActivity
    fun playSound(fileName: String) {
        val resId = context.resources.getIdentifier(fileName, "raw", context.packageName)
        if (resId != 0) {
            try {
                val mp = MediaPlayer.create(context, resId)
                mp.setOnCompletionListener { it.release() }
                mp.start()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- GESTÃO DE ÁUDIO GLOBAL ---
    var currentTrack by remember { mutableStateOf<MusicTrack?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableFloatStateOf(0f) }
    var totalDuration by remember { mutableFloatStateOf(1f) }
    var volumeLevel by remember { mutableFloatStateOf(1f) }

    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    val staticPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    val audioScope = rememberCoroutineScope()

    val radioTracks = remember {
        listOf(
            MusicTrack(1, "Ecos do Betão", "Pilar IV OST", "03:45", context.resources.getIdentifier("faixa1", "raw", context.packageName)),
            MusicTrack(2, "Frequência Morta", "Pilar IV OST", "02:20", context.resources.getIdentifier("faixa2", "raw", context.packageName)),
            MusicTrack(3, "Sombra Estática", "Pilar IV OST", "04:10", null),
            MusicTrack(4, "Interferência", "Pilar IV OST", "01:55", context.resources.getIdentifier("faixa4", "raw", context.packageName))
        )
    }

    fun releaseMediaPlayer() {
        try {
            mediaPlayer.value?.stop()
            mediaPlayer.value?.release()
            mediaPlayer.value = null
            staticPlayer.value?.stop()
            staticPlayer.value?.release()
            staticPlayer.value = null
            isPlaying = false
            currentPosition = 0f
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun stopRadio() { if (isPlaying) releaseMediaPlayer() }

    fun setVolume(volume: Float) {
        volumeLevel = volume.coerceIn(0f, 1f)
        try {
            mediaPlayer.value?.setVolume(volumeLevel, volumeLevel)
            staticPlayer.value?.setVolume(volumeLevel, volumeLevel)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun restartMusic() {
        try {
            mediaPlayer.value?.seekTo(0)
            if (!isPlaying) { mediaPlayer.value?.start(); isPlaying = true }
        } catch (e: Exception) { e.printStackTrace() }
    }

    var playNextTrack: (() -> Unit)? = null

    fun playMusicContent(selectedId: Int) {
        try {
            val mp = MediaPlayer.create(context, selectedId)
            mp.isLooping = false
            mp.setVolume(volumeLevel, volumeLevel)
            mp.setOnCompletionListener { playNextTrack?.invoke() }
            mp.start()
            mediaPlayer.value = mp
            totalDuration = mp.duration.toFloat()
            audioScope.launch {
                while (mediaPlayer.value != null && mediaPlayer.value!!.isPlaying) {
                    currentPosition = mediaPlayer.value!!.currentPosition.toFloat()
                    delay(500)
                }
            }
        } catch (e: Exception) { e.printStackTrace(); isPlaying = false }
    }

    fun startTrackSequence(track: MusicTrack) {
        val selectedId = track.resourceId
        releaseMediaPlayer()
        if (selectedId != null && selectedId != 0) {
            isPlaying = true
            currentTrack = track
            audioScope.launch {
                val staticResId = context.resources.getIdentifier("radio_static", "raw", context.packageName)
                if (staticResId != 0) {
                    try {
                        val staticMp = MediaPlayer.create(context, staticResId)
                        staticMp.setVolume(volumeLevel, volumeLevel)
                        staticMp.isLooping = true
                        staticMp.start()
                        staticPlayer.value = staticMp
                        val staticDuration = Random.nextLong(1000, 10000)
                        delay(staticDuration)
                        staticMp.stop()
                        staticMp.release()
                        staticPlayer.value = null
                    } catch (e: Exception) { e.printStackTrace() }
                }
                if (isPlaying) playMusicContent(selectedId)
            }
        } else {
            isPlaying = true
            currentTrack = track
            totalDuration = 100f
            currentPosition = 50f
            audioScope.launch { delay(3000); if (isPlaying) playNextTrack?.invoke() }
        }
    }

    playNextTrack = {
        val currentIndex = radioTracks.indexOfFirst { it.id == currentTrack?.id }
        if (currentIndex != -1) {
            val nextIndex = (currentIndex + 1) % radioTracks.size
            val nextTrack = radioTracks[nextIndex]
            startTrackSequence(nextTrack)
        }
    }

    fun togglePlayPause(track: MusicTrack) {
        if (currentTrack?.id == track.id && mediaPlayer.value != null) {
            if (isPlaying) { mediaPlayer.value?.pause(); isPlaying = false }
            else { mediaPlayer.value?.start(); isPlaying = true }
            return
        }
        startTrackSequence(track)
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            if (mediaPlayer.value != null && mediaPlayer.value!!.isPlaying) {
                currentPosition = mediaPlayer.value!!.currentPosition.toFloat()
                if (mediaPlayer.value!!.duration > 0) totalDuration = mediaPlayer.value!!.duration.toFloat()
            } else if (staticPlayer.value != null && staticPlayer.value!!.isPlaying) {
                currentPosition = 0f
            }
            delay(100)
        }
    }

    DisposableEffect(Unit) { onDispose { releaseMediaPlayer() } }

    // --- LÓGICA DO JOGO ---

    // 1. Resposta do Chefe em Tempo Real
    LaunchedEffect(GameData.isFinalSequenceActive.value) {
        if (GameData.isFinalSequenceActive.value && !GameData.isSofiaChatUnlocked.value) {
            val chefeContact = GameData.getContact("chefe")

            if (chefeContact != null) {
                delay(5000)
                playSound("received")
                chefeContact.history.add(Message(content = "Sofia?", isFromPlayer = false, timestamp = "Agora"))
                GameData.notificationContent.value = "Chefe: Sofia?"
                GameData.showNotificationPopup.value = true

                delay(3000)
                playSound("received")
                chefeContact.history.add(Message(content = "De que estás a falar?", isFromPlayer = false, timestamp = "Agora"))

                delay(4000)
                playSound("received")
                chefeContact.history.add(Message(content = "Eu estou na obra agora", isFromPlayer = false, timestamp = "Agora"))

                delay(2000)
                playSound("received")
                chefeContact.history.add(Message(content = "Na cave", isFromPlayer = false, timestamp = "Agora"))

                delay(3000)
                playSound("received")
                chefeContact.history.add(Message(content = "Não há nenhum pilar 4", isFromPlayer = false, timestamp = "Agora"))

                delay(3000)
                playSound("received")
                chefeContact.history.add(Message(content = "O projeto foi alterado há meses", isFromPlayer = false, timestamp = "Agora"))

                delay(3000)
                playSound("received")
                chefeContact.history.add(Message(content = "", isFromPlayer = false, timestamp = "Agora", imageResId = R.drawable.cave_empty))
                GameData.notificationContent.value = "Chefe enviou uma foto."
                GameData.showNotificationPopup.value = true
            }

            delay(5000)
            GameData.isSofiaChatUnlocked.value = true

            val sofiaContact = ContactProfile(
                id = "sofia_ghost",
                name = "Eu (Sofia)",
                status = "Online",
                initialMessages = mutableStateListOf()
            )

            if (GameData.getContact("sofia_ghost") == null) {
                GameData.contacts.add(0, sofiaContact)
                playSound("received")
                // Sem notificação popup, o jogador tem de procurar
            }
        }
    }

    LaunchedEffect(GameData.showHauntedMarks.value) { if (GameData.showHauntedMarks.value) stopRadio() }

    LaunchedEffect(GameData.triggerForcedNavigation.value) {
        if (GameData.triggerForcedNavigation.value) {
            stopRadio()
            navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
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
            if (GameData.emails.isEmpty()) GameData.triggerGhostEmail()
        }
    }

    LaunchedEffect(GameData.isGameFinished.value) {
        if (GameData.isGameFinished.value && !GameData.isSystemRebooted.value) {
            stopRadio()
            GameData.isSystemRebooted.value = true
            saveRebootState()

            delay(1000)
            finishApp()
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
                    onNavigateToRadio = { navController.navigate(Routes.RADIO) },
                    onNavigateToCave = { navController.navigate(Routes.CAVE_APP) }
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
            composable(Routes.SITECAM) {
                if (GameData.hasReadGhostEmail.value) stopRadio()
                SiteCamScreen(onBack = { navController.popBackStack() }, onForceNavigateToChat = { navController.navigate("conversation/chefe") { popUpTo(Routes.HOME) { inclusive = false } } })
            }
            composable(Routes.EMAIL) { EmailScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.RADIO) {
                RadioScreen(
                    onBack = { navController.popBackStack() },
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    onTogglePlayPause = { track -> togglePlayPause(track) },
                    currentPosition = currentPosition,
                    totalDuration = totalDuration,
                    volumeLevel = volumeLevel,
                    onVolumeChange = { setVolume(it) },
                    onRestart = { restartMusic() }
                )
            }
            composable(Routes.CAVE_APP) {
                // AQUI ESTÁ A LIGAÇÃO PARA O NOVO ECRÃ
                CaveGameScreen()
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