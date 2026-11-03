package com.example.projetofinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.projetofinal.data.datastore.UserPreferencesRepository
import com.example.projetofinal.ui.navigation.AppNavGraph
import com.example.projetofinal.ui.theme.ProjetoFinalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val prefsRepository = remember { UserPreferencesRepository(applicationContext) }
            val prefs by prefsRepository.userPreferencesFlow.collectAsState(
                initial = com.example.projetofinal.data.datastore.UserPreferences()
            )

            val useDarkMode = if (prefs.isDarkMode) true else isSystemInDarkTheme()

            ProjetoFinalTheme(darkTheme = useDarkMode) {
                AppNavGraph()
            }
        }
    }
}
