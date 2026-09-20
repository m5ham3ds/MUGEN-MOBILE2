package com.example.ui.screens.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GameOverlay(onResume: () -> Unit, onExit: () -> Unit) {
    var showSettings by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        if (showSettings) {
            Card(
                modifier = Modifier.padding(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("In-game settings", style = MaterialTheme.typography.titleLarge)
                    Text("Runtime settings are not yet bound to the native engine. Use Resume to continue.")
                    Button(onClick = { showSettings = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Back")
                    }
                }
            }
            return@Box
        }

        Card(
            modifier = Modifier.padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Pause Menu", style = MaterialTheme.typography.titleLarge)

                Button(onClick = onResume, modifier = Modifier.fillMaxWidth()) {
                    Text("Resume")
                }

                Button(onClick = { showSettings = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Settings")
                }

                OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
                    Text("Exit Game")
                }
            }
        }
    }
}
