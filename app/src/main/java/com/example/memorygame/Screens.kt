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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
// SES İKONLARI EKLENDİ
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

// ORTAK ARKA PLAN
val bgGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1A237E), Color(0xFF311B92), Color(0xFF4A148C))
)


// 1. GİRİŞ EKRANI (SES BUTONU EKLENDİ)

@Composable
fun StartScreen(navController: NavController, viewModel: GameViewModel, bestScore: Int) {
    LaunchedEffect(Unit) {
        viewModel.loadGameData()
        viewModel.startBackgroundMusic()
    }

    val currentLevel by viewModel.currentLevel.collectAsState()
    val score by viewModel.score.collectAsState()
    val hasSavedGame = currentLevel > 1 || score > 0
    // Ses Durumu
    val isMuted by viewModel.isMuted.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "scale"
    )

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {

        // --- SES BUTONU
        IconButton(
            onClick = { viewModel.toggleSound() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = "Sesi Aç/Kapat",
                tint = Color.White
            )
        }

        // ANA MENÜ İÇERİĞİ

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize().padding(32.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧠", fontSize = 60.sp, modifier = Modifier.scale(scale))
                        Text("MEMORY\nGAME", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BestScoreCard(bestScore)
                        Spacer(modifier = Modifier.height(32.dp))
                        if (hasSavedGame) {
                            GameMenuButton(text = "DEVAM ET", icon = Icons.Default.PlayArrow, gradient = Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFF00C853)))) {
                                viewModel.resumeOrRestartLevel()
                                navController.navigate("game")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            GameMenuButton(text = "BAŞTAN BAŞLA", icon = Icons.Default.DeleteForever, gradient = Brush.horizontalGradient(listOf(Color(0xFFFF5252), Color(0xFFD32F2F)))) {
                                viewModel.startNewGame()
                                navController.navigate("game")
                            }
                        } else {
                            GameMenuButton(text = "OYUNA BAŞLA", icon = Icons.Default.PlayArrow, gradient = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF00B8D4)))) {
                                viewModel.startNewGame()
                                navController.navigate("game")
                            }
                        }
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("🧠", fontSize = 80.sp, modifier = Modifier.scale(scale))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("MEMORY\nGAME", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 40.sp)
                    Spacer(modifier = Modifier.height(48.dp))
                    BestScoreCard(bestScore)
                    Spacer(modifier = Modifier.height(48.dp))
                    if (hasSavedGame) {
                        GameMenuButton(text = "DEVAM ET", icon = Icons.Default.PlayArrow, gradient = Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFF00C853)))) {
                            viewModel.resumeOrRestartLevel()
                            navController.navigate("game")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        GameMenuButton(text = "BAŞTAN BAŞLA", icon = Icons.Default.DeleteForever, gradient = Brush.horizontalGradient(listOf(Color(0xFFFF5252), Color(0xFFD32F2F)))) {
                            viewModel.startNewGame()
                            navController.navigate("game")
                        }
                    } else {
                        GameMenuButton(text = "OYUNA BAŞLA", icon = Icons.Default.PlayArrow, gradient = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF00B8D4)))) {
                            viewModel.startNewGame()
                            navController.navigate("game")
                        }
                    }
                }
            }
        }
    }
}


// 2. OYUN EKRANI

@Composable
fun GameScreen(viewModel: GameViewModel, navController: NavController) {
    val cards by viewModel.cards.collectAsState()
    val score by viewModel.score.collectAsState()
    val attempts by viewModel.attempts.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val isLevelWon by viewModel.isLevelWon.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) 8 else 4
    val gridState = rememberLazyGridState()

    LaunchedEffect(isGameOver) {
        if (isGameOver) {
            navController.navigate("result") { popUpTo("game") { inclusive = true } }
        }
    }

    //LOTTIE HAZIRLIĞI
    val confettiComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))
    val confettiProgress by animateLottieCompositionAsState(
        composition = confettiComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isLevelWon
    )

    // PAUSE DIALOG
    if (isPaused) {
        AlertDialog(
            onDismissRequest = {  },
            title = { Text("OYUN DURAKLATILDI", color = Color(0xFF311B92), fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.resumeGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Devam Et")
                    }
                    Button(
                        onClick = { viewModel.resetCurrentLevel() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yeniden Başlat")
                    }
                }
            },
            confirmButton = {},
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- ANA OYUN DÜZENİ ---
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. OYUN İÇERİĞİ
        Column(modifier = Modifier.fillMaxSize().background(bgGradient).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(bottom = if (isLandscape) 4.dp else 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape).size(40.dp)) { Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White) }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = Color(0xFFFFD700), shape = RoundedCornerShape(12.dp)) {
                        Text(text = "LEVEL $currentLevel", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    TimerDisplay(timeLeft)
                    if (isLandscape) {
                        CompactInfoChip(icon = "🎯", text = "$attempts")
                        CompactInfoChip(icon = "💎", text = "$score")
                    }
                }
                IconButton(onClick = { viewModel.pauseGame() }, modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape).size(40.dp)) { Icon(Icons.Default.Pause, contentDescription = "Durdur", tint = Color.White) }
            }
            if (!isLandscape) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.Center) {
                    InfoChip(icon = "🎯", text = "$attempts")
                    Spacer(modifier = Modifier.width(16.dp))
                    InfoChip(icon = "💎", text = "$score")
                }
            }
            LazyVerticalGrid(state = gridState, columns = GridCells.Fixed(columns), modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 4.dp)) {
                items(cards) { card -> MemoryCardView(card = card) { viewModel.onCardClick(card) } }
            }
        }

        // 2. KONFETİ KATMANI
        if (isLevelWon) {
            LottieAnimation(
                composition = confettiComposition,
                progress = { confettiProgress },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}


// 3. SONUÇ EKRANI

@Composable
fun ResultScreen(viewModel: GameViewModel, navController: NavController) {
    val currentScore by viewModel.score.collectAsState()
    val currentIsLevelWon by viewModel.isLevelWon.collectAsState()

    val score = remember { currentScore }
    val isLevelWon = remember { currentIsLevelWon }

    val stars by viewModel.earnedStars.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    val titleText = if (isLevelWon) "SEVİYE TAMAM!" else "OYUN BİTTİ!"
    val iconText = if (isLevelWon) "🎉" else "⌛"
    val messageText = if (isLevelWon) "Tebrikler! Sonraki seviyeye geç." else "Süre doldu! Denemeye devam et."
    val buttonText = if (isLevelWon) "SONRAKİ LEVEL" else "TEKRAR DENE"
    val buttonGradient = if (isLevelWon) Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFF00C853))) else Brush.horizontalGradient(listOf(Color(0xFFFF5252), Color(0xFFD32F2F)))

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(bgGradient), contentAlignment = Alignment.Center) {
        val isActuallyLandscape = maxWidth > maxHeight

        if (isActuallyLandscape) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(iconText, fontSize = 80.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(titleText, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isLevelWon) {
                        LevelIndicator(level = currentLevel)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.Center) {
                            repeat(3) { index ->
                                val starColor = if (index < stars) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.5f)
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = starColor, modifier = Modifier.size(32.dp).padding(2.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(messageText, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, textAlign = TextAlign.Center)
                }
                Spacer(modifier = Modifier.width(32.dp))
                Column(modifier = Modifier.weight(1f).verticalScroll(scrollState), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    ResultScoreCard(score)
                    Spacer(modifier = Modifier.height(24.dp))
                    GameMenuButton(text = buttonText, icon = if (isLevelWon) Icons.Default.PlayArrow else Icons.Default.Refresh, gradient = buttonGradient) {
                        if (isLevelWon) viewModel.advanceToNextLevel() else viewModel.resetCurrentLevel()
                        navController.navigate("game") { popUpTo("start") }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.prepareForMenuReturn(); navController.navigate("start") }) {
                        Text("Ana Menüye Dön", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(scrollState), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(iconText, fontSize = 100.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(titleText, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                if (isLevelWon) {
                    LevelIndicator(level = currentLevel)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.Center) {
                        repeat(3) { index ->
                            val starColor = if (index < stars) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.5f)
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = starColor, modifier = Modifier.size(48.dp).padding(4.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(messageText, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                ResultScoreCard(score)
                Spacer(modifier = Modifier.height(32.dp))
                GameMenuButton(text = buttonText, icon = if (isLevelWon) Icons.Default.PlayArrow else Icons.Default.Refresh, gradient = buttonGradient) {
                    if (isLevelWon) viewModel.advanceToNextLevel() else viewModel.resetCurrentLevel()
                    navController.navigate("game") { popUpTo("start") }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { viewModel.prepareForMenuReturn(); navController.navigate("start") }) {
                    Text("Ana Menüye Dön", color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}


// YARDIMCI BİLEŞENLER


@Composable
fun GameMenuButton(text: String, icon: ImageVector, gradient: Brush, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = Modifier.width(280.dp).height(56.dp).shadow(8.dp, RoundedCornerShape(24.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize().background(gradient).border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = text, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun LevelIndicator(level: Int) {
    Text(text = "BİTEN LEVEL: $level", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFFFD700), textAlign = TextAlign.Center)
}

@Composable
fun ResultScoreCard(score: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth(0.6f)) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "TOPLAM SKOR", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$score", color = Color(0xFF311B92), fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun TimerDisplay(timeLeft: Int) {
    val timerColor = if (timeLeft <= 10) Color(0xFFFF5252) else Color.White
    Surface(color = Color.Black.copy(alpha = 0.3f), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, timerColor.copy(alpha = 0.5f))) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
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