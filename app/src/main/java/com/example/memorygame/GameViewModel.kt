package com.example.memorygame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.max

class GameViewModel(private val repository: GameRepository, private val soundManager: SoundManager) : ViewModel() {

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

    private val _currentLevel = MutableStateFlow(1)
    val currentLevel = _currentLevel.asStateFlow()

    private val _isLevelWon = MutableStateFlow(false)
    val isLevelWon = _isLevelWon.asStateFlow()

    private val _earnedStars = MutableStateFlow(0)
    val earnedStars = _earnedStars.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()

    // Ses Durumu
    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    private var timerJob: Job? = null
    val bestScore = repository.bestScore
    private var openCards = mutableListOf<MemoryCard>()
    private var isProcessing = false

    init {
        loadGameData()
    }

    fun startBackgroundMusic() {
        if (!_isMuted.value) {
            soundManager.playMusic()
        }
    }

    fun toggleSound() {
        val muted = soundManager.toggleSound()
        _isMuted.value = muted
    }

    fun loadGameData() {
        viewModelScope.launch {
            val savedLvl = repository.savedLevel.first()
            val savedScr = repository.savedScore.first()
            _currentLevel.value = savedLvl
            _score.value = savedScr
        }
    }

    fun pauseGame() {
        _isPaused.value = true
        soundManager.pauseMusic()
    }

    fun resumeGame() {
        _isPaused.value = false
        if (!_isMuted.value) soundManager.playMusic()
    }

    fun startNewGame() {
        viewModelScope.launch {
            repository.clearProgress()
            _isGameOver.value = false
            _isLevelWon.value = false
            _isPaused.value = false
            _currentLevel.value = 1
            _score.value = 0
            _attempts.value = 0
            startLevel()
        }
    }


    // levele ilk gelindiğindeki kayıtlı puanla başlamasını sağlar.
    fun resetCurrentLevel() {
        viewModelScope.launch {
            // DataStore'dan en son kaydedilen skoru geri yükle
            val savedScr = repository.savedScore.first()
            _score.value = savedScr

            _isPaused.value = false
            if (!_isMuted.value) soundManager.playMusic()
            startLevel()
        }
    }

    fun resumeOrRestartLevel() {
        if (!_isMuted.value) soundManager.playMusic()
        startLevel()
    }

    fun advanceToNextLevel() {
        _currentLevel.value += 1
        saveProgress()
        startLevel()
    }

    fun prepareForMenuReturn() {
        if (_isLevelWon.value) {
            _currentLevel.value += 1
            saveProgress()
        }
        // Menüye dönerken bu "bayrakları" indiriyoruz ki karışıklık olmasın
        _isGameOver.value = false
        _isLevelWon.value = false
        _isPaused.value = false
        timerJob?.cancel()
    }

    private fun saveProgress() {
        viewModelScope.launch {
            repository.saveGameProgress(_currentLevel.value, _score.value)
        }
    }

    private fun startLevel() {
        timerJob?.cancel()

        val allEmojis = listOf(
            "🚀", "🛸", "🪐", "🌍", "🌕", "⭐", "☄️", "👽", "👾", "🤖",
            "🦄", "🐲", "🦕", "🐢", "🐬", "🦊", "🐼", "🦁", "🐧", "🦉",
            "🍕", "🍔", "🍦", "🍩", "🍿", "🌮", "🍒", "🥑", "🧁", "🍪",
            "⚽", "🏀", "🎮", "🎸", "🎨", "🚗", "✈️", "⚓", "💎", "🎈",
            "👻", "🎃", "🎁", "👑", "🧩", "📸", "🎧", "💡", "⏰", "🔑",
            "🔥", "🌈", "❤️", "🍀", "⚡", "❄️", "🌊", "🌵", "🍄", "🍁"
        )

        val selectedEmojis = allEmojis.shuffled().take(12)
        val gameImages = (selectedEmojis + selectedEmojis).shuffled()

        _cards.value = gameImages.mapIndexed { index, emoji ->
            MemoryCard(id = index, emoji = emoji)
        }

        _attempts.value = 0
        _earnedStars.value = 0
        _isGameOver.value = false
        _isLevelWon.value = false
        _isPaused.value = false
        openCards.clear()
        isProcessing = false

        val baseTime = 120
        val timeReduction = (_currentLevel.value - 1) * 10
        _timeLeft.value = max(40, baseTime - timeReduction)

        startTimer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0 && !_isGameOver.value && !_isLevelWon.value) {
                delay(1000)
                if (!_isPaused.value) {
                    _timeLeft.value -= 1
                }
            }

            if (_timeLeft.value == 0 && !_isLevelWon.value) {
                soundManager.playSound(R.raw.sfx_gameover)

                _isGameOver.value = true
                _isLevelWon.value = false
            }
        }
    }

    private fun calculateStars(attempts: Int): Int {
        return when {
            attempts <= 20 -> 3
            attempts <= 25 -> 2
            else -> 1
        }
    }

    fun onCardClick(card: MemoryCard) {
        if (_isPaused.value || card.isFlipped || card.isMatched || isProcessing || _isGameOver.value) return

        //Kart Çevirme
        soundManager.playSound(R.raw.sfx_flip)

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

                soundManager.playSound(R.raw.sfx_match)

                _cards.value = _cards.value.map {
                    if (it.id == card1.id || it.id == card2.id) it.copy(isMatched = true) else it
                }

                if (_cards.value.all { it.isMatched }) {
                    timerJob?.cancel()
                    _earnedStars.value = calculateStars(_attempts.value)
                    _isLevelWon.value = true

                    soundManager.playSound(R.raw.sfx_win)

                    val timeBonus = _timeLeft.value
                    _score.value += timeBonus

                    repository.saveGameProgress(_currentLevel.value + 1, _score.value)
                    repository.saveBestScore(_score.value)

                    delay(3000) // Konfeti beklemesi

                    _isGameOver.value = true
                }
            } else {
                // Puanı düşürürken 0'ın altına inmesin
                val newScore = _score.value - 20
                _score.value = max(0, newScore)

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