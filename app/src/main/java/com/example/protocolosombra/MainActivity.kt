package com.example.protocolosombra

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // --- MOSTRAR SPLASH SCREEN ---
        setTheme(R.style.Theme_ProtocoloSombra)

        super.onCreate(savedInstanceState)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            GameNavigation()
        }
    }
}

@Composable
fun GameNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // --- NAVEGAÇÃO SIMPLIFICADA (SEM BLOQUEIOS) ---
    // Removemos qualquer lógica de "debounce" ou atraso.
    // O sistema de navegação padrão do Compose é rápido e reativo.
    // O problema de "ficar preso" deve desaparecer com esta abordagem limpa.

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
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
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
                    onNavigateToEmail = { navController.navigate(Routes.EMAIL) }
                )
            }
            composable(Routes.CHAT) {
                ChatScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToConversation = { contactId -> navController.navigate("conversation/$contactId") }
                )
            }
            composable(route = Routes.CONVERSATION, arguments = listOf(navArgument("contactId") { type = NavType.StringType })) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
                ConversationScreen(contactId = contactId, onBack = { navController.popBackStack() })
            }
            composable(Routes.NOTES) { NotesScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.BANK) { BankScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.GALLERY) { GalleryScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.TRACKER) { TrackerScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SITECAM) {
                SiteCamScreen(onBack = { navController.popBackStack() }, onForceNavigateToChat = {
                    navController.navigate("conversation/chefe") { popUpTo(Routes.HOME) { inclusive = false } }
                })
            }
            composable(Routes.EMAIL) { EmailScreen(onBack = { navController.popBackStack() }) }
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