package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.GameDatabase
import com.example.data.repository.GameRepository
import com.example.ui.game.EscapeMomGameApp
import com.example.ui.game.GameViewModel
import com.example.ui.game.GameViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Instantiate database components safely on startup
        val database = GameDatabase.getDatabase(applicationContext)
        val repository = GameRepository(database.gameDao())
        val factory = GameViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[GameViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Fullscreen Canvas is edge-to-edge; padding is managed inside HUD overlays
                    EscapeMomGameApp(viewModel = viewModel)
                }
            }
        }
    }
}
