package com.example.protocolosombra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Note(val title: String, val content: String, val color: Color)

@Composable
fun NotesScreen(onBack: () -> Unit) {
    // PROTEÇÃO CONTRA CLIQUES MÚLTIPLOS
    var backPressed by remember { mutableStateOf(false) }

    val notes = listOf(
        Note("Lista Compras", "Leite\nPão\nComida para o gato\nPilhas", Color(0xFFFFF59D)),
        Note("Ideias Projeto", "Falar com o Eng. Santos sobre as fundações da zona norte.", Color(0xFF81D4FA)),
        Note("🎂", "15 de Maio!!!\nFinalmente 28 anos! 🎉\nNão esquecer de reservar o restaurante para o jantar.", Color(0xFFFFAB91)),
        Note("Séries", "Ver o último ep de Dark.\nComeçar aquela nova da HBO.", Color(0xFFA5D6A7))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        FakeStatusBar()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier
                    .size(30.dp)
                    .clickable(enabled = !backPressed) {
                        backPressed = true
                        onBack()
                    }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Notas",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(notes) { note ->
                NoteCard(note)
            }
        }
    }
}

@Composable
fun NoteCard(note: Note) {
    Column(
        modifier = Modifier
            .height(180.dp)
            .background(note.color, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = note.title,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = note.content,
            color = Color.DarkGray,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}