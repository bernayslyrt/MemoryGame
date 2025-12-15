package com.example.memorygame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    private val _cards = MutableStateFlow<List<MemoryCard>>(emptyList())
    val cards = _cards.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _attempts = MutableStateFlow(0)
    val attempts = _attempts.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver = _isGameOver.asStateFlow()

    private val _timeLeft = MutableStateFlow(120)
    val timeLeft = _timeLeft.asStateFlow()

    // --- YENİ: LEVEL TAKİBİ ---
    private val _currentLevel = MutableStateFlow(1)
    val currentLevel = _currentLevel.asStateFlow()

    // Level kazanıldı mı yoksa süre mi bitti?
    private val _isLevelWon = MutableStateFlow(false)
    val isLevelWon = _isLevelWon.asStateFlow()

    private var timerJob: Job? = null
    val bestScore = repository.bestScore
    private var openCards = mutableListOf<MemoryCard>()
    private var isProcessing = false

    init {
        // Başlangıç beklemede
    }

    // --- 1. OYUNU SIFIRDAN BAŞLAT (En Baştan) ---
    fun restartGame() {
        _currentLevel.value = 1
        _score.value = 0
        startLevel()
    }
    // --- YENİ EKLENECEK KISIM: MEVCUT LEVEL'I TEKRARLA ---
    // Bu fonksiyon skoru ve level sayısını sıfırlamadan sadece kartları ve süreyi yeniler.
    fun resetCurrentLevel() {
        startLevel()
    }

    // --- 2. SONRAKİ LEVEL'A GEÇ (Skor korunur) ---
    fun advanceToNextLevel() {
        _currentLevel.value += 1
        startLevel()
    }

    // --- ORTAK LEVEL BAŞLATMA FONKSİYONU ---
    private fun startLevel() {
        timerJob?.cancel()

        // --- GENİŞLETİLMİŞ EMOJİ HAVUZU (Birbirinden Farklı) ---
        val allEmojis = listOf(
            "🚀", "🛸", "🪐", "🌍", "🌕", "⭐", "☄️", "👽", "👾", "🤖",
            "🦄", "🐲", "🦕", "🐢", "🐬", "🦊", "🐼", "🦁", "🐧", "🦉",
            "🍕", "🍔", "🍦", "🍩", "🍿", "🌮", "🍒", "🥑", "🧁", "🍪",
            "⚽", "🏀", "🎮", "🎸", "🎨", "🚗", "✈️", "⚓", "💎", "🎈",
            "👻", "🎃", "🎁", "👑", "🧩", "📸", "🎧", "💡", "⏰", "🔑",
            "🔥", "🌈", "❤️", "🍀", "⚡", "❄️", "🌊", "🌵", "🍄", "🍁"
        )

        // 24 Kart için 12 Çift seçiyoruz
        val selectedEmojis = allEmojis.shuffled().take(12)
        val gameImages = (selectedEmojis + selectedEmojis).shuffled()

        _cards.value = gameImages.mapIndexed { index, emoji ->
            MemoryCard(id = index, emoji = emoji)
        }

        // Skor ve Level SIFIRLANMAZ, sadece tur verileri sıfırlanır
        _attempts.value = 0
        _isGameOver.value = false
        _isLevelWon.value = false
        openCards.clear()
        isProcessing = false

        // --- ZORLUK MANTIĞI ---
        // Level 1: 120sn. Her levelde 10sn azalır. Minimum 40sn.
        val baseTime = 120
        val timeReduction = (_currentLevel.value - 1) * 10
        _timeLeft.value = max(40, baseTime - timeReduction)

        startTimer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0 && !_isGameOver.value) {
                delay(1000)
                _timeLeft.value -= 1
            }

            if (_timeLeft.value == 0) {
                _isGameOver.value = true
                _isLevelWon.value = false // Süre bitti, kaybettin
            }
        }
    }

    fun onCardClick(card: MemoryCard) {
        if (card.isFlipped || card.isMatched || isProcessing || _isGameOver.value) return

        _cards.value = _cards.value.map {
            if (it.id == card.id) it.copy(isFlipped = true) else it
        }

        val updatedCard = _cards.value.find { it.id == card.id } ?: return
        openCards.add(updatedCard)

        if (openCards.size == 2) {
            processMatch()
        }
    }

    private fun processMatch() {
        isProcessing = true
        _attempts.value += 1

        viewModelScope.launch {
            val card1 = openCards[0]
            val card2 = openCards[1]

            if (card1.emoji == card2.emoji) {
                _score.value += 50

                _cards.value = _cards.value.map {
                    if (it.id == card1.id || it.id == card2.id) it.copy(isMatched = true) else it
                }

                if (_cards.value.all { it.isMatched }) {
                    // Bonus Puan
                    val timeBonus = _timeLeft.value
                    _score.value += timeBonus

                    repository.saveBestScore(_score.value)
                    timerJob?.cancel()
                    delay(500)

                    // KAZANDI
                    _isLevelWon.value = true
                    _isGameOver.value = true
                }
            } else {
                val newScore = _score.value - 20
                _score.value = if (newScore < 0) 0 else newScore

                delay(800)
                _cards.value = _cards.value.map {
                    if (it.id == card1.id || it.id == card2.id) it.copy(isFlipped = false) else it
                }
            }
            openCards.clear()
            isProcessing = false
        }
    }
}