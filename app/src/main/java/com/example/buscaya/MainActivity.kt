package com.example.buscaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.buscaya.ui.screen.FarmaciaScreen
import com.example.buscaya.ui.theme.BuscayaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuscayaTheme {
                FarmaciaScreen()
            }
        }
    }
}
