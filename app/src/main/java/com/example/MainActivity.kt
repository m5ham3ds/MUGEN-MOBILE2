package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.data.settings.SettingsDataStore
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Start recording logs in the background
    com.example.utils.LogcatRecorder.start(this)
    
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val isOnboardingCompleted by remember {
          SettingsDataStore.isOnboardingCompleted(this)
        }.collectAsState(initial = null)

        if (isOnboardingCompleted != null) {
            AppNavigation(isOnboardingCompleted = isOnboardingCompleted!!)
        }
      }
    }
  }
}
