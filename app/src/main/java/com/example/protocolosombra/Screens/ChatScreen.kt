package com.example.protocolosombra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protocolosombra.data.GameData
import com.example.protocolosombra.data.ContactProfile

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    onNavigateToConversation: (String) -> Unit
) {
    val chats = GameData.contacts

    // PROTEÇÃO CONTRA CLIQUES MÚLTIPLOS
    var backPressed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        FakeStatusBar()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(enabled = !backPressed) {
                        backPressed = true
                        onBack()
                    }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "ChatLog",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(chats) { contact ->
                ChatListItem(
                    contact = contact,
                    onClick = { onNavigateToConversation(contact.id) }
                )
            }
        }
    }
}

@Composable
fun ChatListItem(
    contact: ContactProfile,
    onClick: () -> Unit
) {
    val lastMsgObj = contact.history.lastOrNull()
    val lastMsg = lastMsgObj?.content ?: ""
    val lastTime = lastMsgObj?.timestamp ?: ""
    val isUnread = lastMsgObj != null && !lastMsgObj.isRead

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
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

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = contact.name,
                    color = Color.White,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp
                )
                Text(
                    text = lastTime,
                    color = if (isUnread) Color(0xFF25D366) else Color(0xFFA0A0A0),
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lastMsg,
                color = if (isUnread) Color.White else Color(0xFFA0A0A0),
                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                fontSize = 14.sp
            )
        }

        if (isUnread) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color(0xFF25D366), CircleShape)
            )
        }
    }
}