package com.example.protocolosombra.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.protocolosombra.data.ContactProfile
import com.example.protocolosombra.data.GameData
import com.example.protocolosombra.data.Message
import com.example.protocolosombra.data.ReplyOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatState(private val contact: ContactProfile) {
    val messages = contact.history

    val currentOptions = mutableStateOf<List<ReplyOption>>(emptyList())
    val isTyping = mutableStateOf(false)

    init {
        val currentNode = contact.currentNodeId

        if (currentNode != null) {
            val node = GameData.getDialogueNode(currentNode)
            if (node != null) {
                if (messages.isEmpty() || !messages.last().isFromPlayer) {
                    currentOptions.value = node.options
                }
            }
        }
    }

    // UPDATE: Agora aceita callbacks para tocar sons
    fun selectOption(
        option: ReplyOption,
        scope: kotlinx.coroutines.CoroutineScope,
        onTypingSound: () -> Unit = {},
        onMessageSound: () -> Unit = {}
    ) {
        val playerMsg = Message(
            content = option.text,
            isFromPlayer = true,
            timestamp = getCurrentTime(),
            isRead = true
        )
        messages.add(playerMsg)

        currentOptions.value = emptyList()
        contact.currentNodeId = option.nextNodeId

        option.nextNodeId?.let { nextId ->
            val nextNode = GameData.getDialogueNode(nextId) ?: return

            scope.launch {
                // Início da resposta do NPC
                isTyping.value = true
                onTypingSound() // Toca som de teclado (se disponível)

                nextNode.npcMessages.forEach { npcText ->
                    delay(1500)

                    val npcMsg = Message(
                        content = npcText,
                        isFromPlayer = false,
                        timestamp = getCurrentTime(),
                        isRead = true
                    )
                    messages.add(npcMsg)
                    onMessageSound() // Toca som de "plim" (se disponível)

                    delay(500)

                    // Se ainda houver mensagens para vir, toca o som de escrever novamente
                    if (nextNode.npcMessages.last() != npcText) {
                        onTypingSound()
                    }
                }

                isTyping.value = false
                currentOptions.value = nextNode.options
            }
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "Hoje " + sdf.format(Date())
    }
}

@Composable
fun rememberChatState(contact: ContactProfile): ChatState {
    return remember(contact.id) { ChatState(contact) }
}