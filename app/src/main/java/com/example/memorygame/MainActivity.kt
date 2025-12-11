package com.example.memorygame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()

            // --- ÇÖZÜM BURADA ---
            // 'remember' yerine 'viewModel' factory kullanıyoruz.
            // Bu sayede ekran döndüğünde oyun verileri SİLİNMİYOR.
            val viewModel: GameViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val repository = GameRepository(context)
                        return GameViewModel(repository) as T
                    }
                }
            )

            val bestScore by viewModel.bestScore.collectAsState(initial = 0)

            NavHost(navController = navController, startDestination = "start") {
                composable("start") {
                    StartScreen(navController = navController, viewModel = viewModel, bestScore = bestScore)
                }
                composable("game") {
                    GameScreen(viewModel = viewModel, navController = navController)
                }
                composable("result") {
                    ResultScreen(viewModel = viewModel, navController = navController)
                }
            }
        }
    }
}