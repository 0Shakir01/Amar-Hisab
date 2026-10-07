package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.FinanceViewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.AmarHisabTheme

class MainActivity : ComponentActivity() {

    private val financeViewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmarHisabTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(viewModel = financeViewModel)
                }
            }
        }
    }
}
