package com.example.memorygame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    private val _cards = MutableStateFlow<List<MemoryCard>>(emptyList())
    val cards = _cards.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _attempts = MutableStateFlow(0)
    val attempts = _attempts.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver = _isGameOver.asStateFlow()


    private val _timeLeft = MutableStateFlow(100)
    val timeLeft = _timeLeft.asStateFlow()

    private var timerJob: Job? = null

    val bestScore = repository.bestScore

    private var openCards = mutableListOf<MemoryCard>()
    private var isProcessing = false

    init {
        // ViewModel ilk oluştuğunda oyunu başlatma,
        // Kullanıcı butona basınca başlatacağız.
    }

    fun resetGame() {
        timerJob?.cancel() // Eski sayacı durdur

        val emojis = listOf("🚀", "🛸", "🪐", "🌍", "🌕", "⭐", "☄️", "👽", "👾", "🤖")
        val selectedEmojis = emojis.shuffled().take(8)
        val gameImages = (selectedEmojis + selectedEmojis).shuffled()

        _cards.value = gameImages.mapIndexed { index, emoji ->
            MemoryCard(id = index, emoji = emoji)
        }
        _score.value = 0
        _attempts.value = 0

        _timeLeft.value = 100

        _isGameOver.value = false
        openCards.clear()
        isProcessing = false

        startTimer() // Sayacı başlat
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0 && !_isGameOver.value) {
                delay(1000)
                _timeLeft.value -= 1
            }

            if (_timeLeft.value == 0) {
                _isGameOver.value = true
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
                    // Bonus Puan: Kalan süre
                    val timeBonus = _timeLeft.value
                    _score.value += timeBonus

                    repository.saveBestScore(_score.value)
                    timerJob?.cancel()
                    delay(500)
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