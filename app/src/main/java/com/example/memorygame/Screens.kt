package com.example.memorygame

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// --- ORTAK ARKA PLAN ---
val bgGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1A237E), Color(0xFF311B92), Color(0xFF4A148C))
)

// ==========================================
// 1. GİRİŞ EKRANI
// ==========================================
@Composable
fun StartScreen(navController: NavController, viewModel: GameViewModel, bestScore: Int) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "scale"
    )

    Box(modifier = Modifier.fillMaxSize().background(bgGradient), contentAlignment = Alignment.Center) {
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🧠", fontSize = 60.sp, modifier = Modifier.scale(scale))
                    Text("HAFIZA\nOYUNU", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BestScoreCard(bestScore)
                    Spacer(modifier = Modifier.height(24.dp))
                    StartButton {
                        viewModel.resetGame()
                        navController.navigate("game")
                    }
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("🧠", fontSize = 80.sp, modifier = Modifier.scale(scale))
                Spacer(modifier = Modifier.height(16.dp))
                Text("HAFIZA KARTI\nOYUNU", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 40.sp)
                Spacer(modifier = Modifier.height(32.dp))
                BestScoreCard(bestScore)
                Spacer(modifier = Modifier.height(48.dp))
                StartButton {
                    viewModel.resetGame()
                    navController.navigate("game")
                }
            }
        }
    }
}

// ==========================================
// 2. OYUN EKRANI
// ==========================================
@Composable
fun GameScreen(viewModel: GameViewModel, navController: NavController) {
    val cards by viewModel.cards.collectAsState()
    val score by viewModel.score.collectAsState()
    val attempts by viewModel.attempts.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()

    // Standart Android Kontrolü (Artık hatasız çalışacak)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val columns = if (isLandscape) 8 else 4
    val gridState = rememberLazyGridState()

    LaunchedEffect(isGameOver) {
        if (isGameOver) {
            navController.navigate("result") { popUpTo("game") { inclusive = true } }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(16.dp)
    ) {
        // --- ÜST BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight() // İçeriği kadar yer kaplar
                .padding(bottom = if (isLandscape) 4.dp else 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Geri Butonu
            IconButton(
                onClick = { viewModel.resetGame(); navController.popBackStack() },
                modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape).size(40.dp)
            ) { Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White) }

            // Orta Kısım
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimerDisplay(timeLeft)
                // Yan ekranda skorlar burada
                if (isLandscape) {
                    CompactInfoChip(icon = "🎯", text = "$attempts")
                    CompactInfoChip(icon = "⭐", text = "$score")
                }
            }

            // Yeniden Başlat
            IconButton(
                onClick = { viewModel.resetGame() },
                modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape).size(40.dp)
            ) { Icon(Icons.Default.Refresh, contentDescription = "Yeniden Başlat", tint = Color.White) }
        }

        // --- İKİNCİ SATIR (Sadece Dik Modda) ---
        if (!isLandscape) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                InfoChip(icon = "🎯", text = "$attempts")
                Spacer(modifier = Modifier.width(16.dp))
                InfoChip(icon = "⭐", text = "$score")
            }
        }

        // --- GRID (OYUN ALANI) ---
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .weight(1f) // Kalan alanı doldur
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            items(cards) { card ->
                MemoryCardView(card = card) { viewModel.onCardClick(card) }
            }
        }
    }
}

// ==========================================
// 3. SONUÇ EKRANI
// ==========================================
@Composable
fun ResultScreen(viewModel: GameViewModel, navController: NavController) {
    val score by viewModel.score.collectAsState()
    val attempts by viewModel.attempts.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val scrollState = rememberScrollState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val isTimeUp = timeLeft == 0
    val titleText = if (isTimeUp) "SÜRE BİTTİ!" else "TEBRİKLER!"
    val iconText = if (isTimeUp) "⌛" else "🎉"
    val messageText = if (isTimeUp) "Biraz daha hızlı olmalısın." else "Hafızan çok kuvvetli!"

    Box(modifier = Modifier.fillMaxSize().background(bgGradient), contentAlignment = Alignment.Center) {
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(iconText, fontSize = 80.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(titleText, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(messageText, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, textAlign = TextAlign.Center)
                }
                Spacer(modifier = Modifier.width(32.dp))
                Column(modifier = Modifier.weight(1.2f).verticalScroll(scrollState), horizontalAlignment = Alignment.CenterHorizontally) {
                    ResultScoreCard(score, attempts)
                    Spacer(modifier = Modifier.height(24.dp))
                    ResultButtons(viewModel, navController)
                }
            }
        } else {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(scrollState), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(iconText, fontSize = 100.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(titleText, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text(messageText, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                ResultScoreCard(score, attempts)
                Spacer(modifier = Modifier.height(32.dp))
                ResultButtons(viewModel, navController)
            }
        }
    }
}

// ==========================================
// YARDIMCI BİLEŞENLER
// ==========================================

@Composable
fun TimerDisplay(timeLeft: Int) {
    val timerColor = if (timeLeft <= 10) Color(0xFFFF5252) else Color.White
    Surface(
        color = Color.Black.copy(alpha = 0.3f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, timerColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AccessTime, contentDescription = null, tint = timerColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "$timeLeft", color = timerColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoChip(icon: String, text: String) {
    Surface(color = Color.Black.copy(alpha = 0.3f), shape = CircleShape) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CompactInfoChip(icon: String, text: String) {
    Surface(color = Color.Black.copy(alpha = 0.3f), shape = CircleShape) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun BestScoreCard(score: Int) {
    Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏆 EN İYİ SKOR", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
            Text("$score", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun StartButton(onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)), modifier = Modifier.width(200.dp).height(50.dp)) {
        Text("OYUNA BAŞLA", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ResultScoreCard(score: Int, attempts: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ResultRow("Toplam Skor", "$score")
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            ResultRow("Deneme Sayısı", "$attempts")
        }
    }
}

@Composable
fun ResultButtons(viewModel: GameViewModel, navController: NavController) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { viewModel.resetGame(); navController.navigate("game") { popUpTo("start") } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64FFDA)), modifier = Modifier.width(220.dp).height(50.dp)) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("YENİDEN OYNA", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = { viewModel.resetGame(); navController.navigate("start") }) {
            Text("Ana Menüye Dön", color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = Color(0xFF311B92), fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MemoryCardView(card: MemoryCard, onClick: () -> Unit) {
    val rotation by animateFloatAsState(targetValue = if (card.isFlipped) 180f else 0f, animationSpec = tween(durationMillis = 400), label = "rotation")
    Box(modifier = Modifier.aspectRatio(1f).graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }.clickable { onClick() }, contentAlignment = Alignment.Center) {
        if (rotation <= 90f) {
            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(colors = listOf(Color(0xFF00E5FF), Color(0xFF2979FF)))), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }.clip(RoundedCornerShape(12.dp)).background(if (card.isMatched) Color(0xFF69F0AE) else Color.White).border(2.dp, Color(0xFF2979FF), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(text = card.emoji, fontSize = 32.sp)
            }
        }
    }
}