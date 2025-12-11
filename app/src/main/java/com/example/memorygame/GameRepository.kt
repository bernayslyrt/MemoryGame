package com.example.memorygame

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Veritabanı (DataStore) oluşturma
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_stats")

class GameRepository(private val context: Context) {

    companion object {
        val BEST_SCORE_KEY = intPreferencesKey("best_score")
    }

    // Skoru okuma (Flow olarak döner, canlı güncellenir)
    val bestScore: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[BEST_SCORE_KEY] ?: 0
        }

    // Skoru kaydetme
    suspend fun saveBestScore(score: Int) {
        context.dataStore.edit { preferences ->
            val currentBest = preferences[BEST_SCORE_KEY] ?: 0
            // Sadece yeni skor daha yüksekse kaydet
            if (score > currentBest) {
                preferences[BEST_SCORE_KEY] = score
            }
        }
    }
}