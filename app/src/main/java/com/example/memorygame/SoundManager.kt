package com.example.memorygame

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

class SoundManager(private val context: Context) {
    private var musicPlayer: MediaPlayer? = null
    var isMuted = false

    // SoundPool Değişkenleri
    private var soundPool: SoundPool? = null
    private val soundMap = HashMap<Int, Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // 2. Efektleri RAM'e (Hafızaya) Yükle
        // Bu işlem Splash ekranı geçilene kadar tamamlanır, gecikme sıfıra iner.
        loadSound(R.raw.sfx_flip)
        loadSound(R.raw.sfx_match)
        loadSound(R.raw.sfx_win)
        loadSound(R.raw.sfx_gameover)
    }

    private fun loadSound(resourceId: Int) {
        soundPool?.let { pool ->
            val soundId = pool.load(context, resourceId, 1)
            soundMap[resourceId] = soundId
        }
    }

    // --- MÜZİK KISMI (MediaPlayer ile devam, çünkü müzik uzun dosyadır) ---
    fun playMusic() {
        if (isMuted) return
        if (musicPlayer == null) {
            try {
                musicPlayer = MediaPlayer.create(context, R.raw.music_bg).apply {
                    isLooping = true
                    setVolume(0.4f, 0.4f) // Müziği biraz daha kıstım ki efektler öne çıksın
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (!musicPlayer!!.isPlaying) {
            musicPlayer?.start()
        }
    }

    fun pauseMusic() {
        if (musicPlayer?.isPlaying == true) {
            musicPlayer?.pause()
        }
    }

    // --- EFEKT KISMI (SoundPool ile Sıfır Gecikme) ---
    fun playSound(resourceId: Int) {
        if (isMuted) return

        val soundId = soundMap[resourceId]
        if (soundId != null) {
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun toggleSound(): Boolean {
        isMuted = !isMuted
        if (isMuted) {
            pauseMusic()
        } else {
            playMusic()
        }
        return isMuted
    }

    // Uygulama kapanırsa hafızayı temizlemek için
    fun release() {
        musicPlayer?.release()
        soundPool?.release()
        musicPlayer = null
        soundPool = null
    }
}