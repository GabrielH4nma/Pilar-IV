package com.example.protocolosombra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protocolosombra.data.Email
import com.example.protocolosombra.data.GameData

@Composable
fun EmailScreen(onBack: () -> Unit) {
    var selectedEmail by remember { mutableStateOf<Email?>(null) }
    var backPressed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        FakeStatusBar()

        if (selectedEmail == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.clickable(enabled = !backPressed) {
                            backPressed = true
                            onBack()
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Pesquisar no correio", color = Color.Gray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color(0xFF5F6368), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text("PRINCIPAL", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))

            if (GameData.emails.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn {
                    items(GameData.emails) { email ->
                        EmailItem(email) {
                            selectedEmail = email
                            email.isRead = true

                            // NOVO: Se o e-mail for o da Sofia, ativa o modo Endgame
                            if (email.sender.contains("Eu (Sofia)")) {
                                GameData.hasReadGhostEmail.value = true
                            }
                        }
                    }
                }
            }
        } else {
            EmailDetailView(
                email = selectedEmail!!,
                onBack = { selectedEmail = null }
            )
        }
    }
}

// ... (EmptyState, EmailItem, EmailDetailView mantêm-se iguais) ...
@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = null,
            tint = Color.DarkGray,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Tudo limpo", color = Color.Gray, fontSize = 18.sp)
        Text("Não tens e-mails novos", color = Color.DarkGray, fontSize = 14.sp)
    }
}

@Composable
fun EmailItem(email: Email, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF5350)),
            contentAlignment = Alignment.Center
        ) {
            Text(email.sender.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = email.sender,
                    color = if (email.isRead) Color.LightGray else Color.White,
                    fontWeight = if (email.isRead) FontWeight.Normal else FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = email.date,
                    color = if (email.isRead) Color.Gray else Color.White,
                    fontSize = 12.sp,
                    fontWeight = if (email.isRead) FontWeight.Normal else FontWeight.Bold
                )
            }
            Text(
                text = email.subject,
                color = if (email.isRead) Color.Gray else Color.White,
                fontWeight = if (email.isRead) FontWeight.Normal else FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = email.body.replace("\n", " "),
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmailDetailView(email: Email, onBack: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White, modifier = Modifier.clickable { onBack() })
            Spacer(modifier = Modifier.width(16.dp))
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.Email, "Arquivar", tint = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(email.subject, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF5350)),
                contentAlignment = Alignment.Center
            ) {
                Text(email.sender.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row {
                    Text(email.sender, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(" <sofia.arch.mendes@gmail.com>", color = Color.Gray, fontSize = 12.sp)
                }
                Text("para mim", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = email.body,
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}