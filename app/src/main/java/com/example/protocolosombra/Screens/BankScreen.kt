package com.example.protocolosombra.ui

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
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
import kotlinx.coroutines.delay

@Composable
fun BankScreen(onBack: () -> Unit) {
    var pinInput by remember { mutableStateOf("") }
    var isAuthenticated by remember { mutableStateOf(GameData.isBankHacked) }
    var showError by remember { mutableStateOf(false) }
    val correctPin = "1505"

    var backPressed by remember { mutableStateOf(false) }

    val safeOnBack = {
        if (!backPressed) {
            backPressed = true
            onBack()
        }
    }

    fun onDigitClick(digit: String) {
        if (pinInput.length < 4) {
            pinInput += digit
            if (pinInput.length == 4) {
                if (pinInput == correctPin) {
                    isAuthenticated = true

                    // LÓGICA DE EVENTOS (Separados por tempo)
                    if (!GameData.isBankHacked) {
                        // 10 Segundos: Ricardo reage
                        Handler(Looper.getMainLooper()).postDelayed({
                            GameData.triggerRicardoBankReaction()
                        }, 10000)

                        // 20 Segundos: Desconhecido manda a pista
                        Handler(Looper.getMainLooper()).postDelayed({
                            GameData.triggerUnknownBankHint()
                        }, 20000)
                    }
                } else {
                    showError = true
                    pinInput = ""
                }
            }
        }
    }

    LaunchedEffect(showError) {
        if (showError) {
            delay(1000)
            showError = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        FakeStatusBar()

        if (isAuthenticated) {
            BankDashboard(onBack = safeOnBack)
        } else {
            BankLogin(
                pinInput = pinInput,
                showError = showError,
                onDigitClick = { onDigitClick(it) },
                onDeleteClick = { if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1) },
                onBack = safeOnBack
            )
        }
    }
}

// ... Resto das funções (BankLogin, BankDashboard, TransactionItemView) mantêm-se iguais
@Composable
fun BankLogin(
    pinInput: String,
    showError: Boolean,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier.clickable { onBack() }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "SHADOW BANK",
            color = Color(0xFFFFC107),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = if (showError) "PIN INCORRETO" else "Insira o PIN",
            color = if (showError) Color.Red else Color.Gray,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < pinInput.length) Color(0xFFFFC107) else Color.DarkGray
                        )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.padding(bottom = 50.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "del")
            )

            for (row in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (item in row) {
                        if (item == "del") {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clickable { onDeleteClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Apagar",
                                    tint = Color.White
                                )
                            }
                        } else if (item.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C2C2C))
                                    .clickable { onDigitClick(item) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item, color = Color.White, fontSize = 24.sp)
                            }
                        } else {
                            Spacer(modifier = Modifier.size(80.dp))
                        }
                    }
                }
            }
        }
    }
}

data class Transaction(val title: String, val amount: String, val date: String, val isSuspicious: Boolean = false)

@Composable
fun BankDashboard(onBack: () -> Unit) {
    val transactions = listOf(
        Transaction("Supermercado Pingo", "- 45,20 €", "22 Out"),
        Transaction("Netflix", "- 11,99 €", "20 Out"),
        Transaction("CLÍNICA PRIVADA 'LUZ'", "- 850,00 €", "18 Out", true),
        Transaction("SpyShop Online", "- 420,50 €", "15 Out", true),
        Transaction("SafeBox Central (Aluguer)", "- 150,00 €", "12 Out"),
        Transaction("Caridade 'Mãos Abertas'", "- 200,00 €", "10 Out"),
        Transaction("KRONOS HOLDINGS", "+ 5.000,00 €", "05 Out", true)
    )

    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier.clickable { onBack() }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        Text("Olá, Sofia", color = Color.Gray, fontSize = 18.sp)
        Text("Saldo Disponível", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("15.772,31 €", color = Color(0xFFFFC107), fontSize = 40.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(40.dp))

        Text("Últimos Movimentos", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(transactions) { trans ->
                TransactionItemView(trans)
            }
        }
    }
}

@Composable
fun TransactionItemView(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                transaction.title,
                color = if (transaction.isSuspicious) Color(0xFFFFAB91) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(transaction.date, color = Color.Gray, fontSize = 14.sp)
        }
        Text(
            transaction.amount,
            color = if (transaction.amount.startsWith("+")) Color.Green else Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}