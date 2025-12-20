package com.example.memorygame

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Veritabanı oluşturma
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_stats")

class GameRepository(private val context: Context) {

    companion object {
        val BEST_SCORE_KEY = intPreferencesKey("best_score")
        val SAVED_LEVEL_KEY = intPreferencesKey("saved_level")
        val SAVED_SCORE_KEY = intPreferencesKey("saved_score")
    }

    // En iyi skoru okuma
    val bestScore: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[BEST_SCORE_KEY] ?: 0 }

    //Kayıtlı Level'ı okuma (Varsayılan 1)
    val savedLevel: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[SAVED_LEVEL_KEY] ?: 1 }

    //Kayıtlı Skoru okuma (Varsayılan 0)
    val savedScore: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[SAVED_SCORE_KEY] ?: 0 }

    // En iyi skoru kaydetme
    suspend fun saveBestScore(score: Int) {
        context.dataStore.edit { preferences ->
            val currentBest = preferences[BEST_SCORE_KEY] ?: 0
            if (score > currentBest) {
                preferences[BEST_SCORE_KEY] = score
            }
        }
    }

    //İlerlemeyi Kaydet (Level ve Skor)
    suspend fun saveGameProgress(level: Int, score: Int) {
        context.dataStore.edit { preferences ->
            preferences[SAVED_LEVEL_KEY] = level
            preferences[SAVED_SCORE_KEY] = score
        }
    }

    //İlerlemeyi Sıfırla (Yeni Oyun Başlatınca)
    suspend fun clearProgress() {
        context.dataStore.edit { preferences ->
            preferences[SAVED_LEVEL_KEY] = 1
            preferences[SAVED_SCORE_KEY] = 0
        }
    }
}