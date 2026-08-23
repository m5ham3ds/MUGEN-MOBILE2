package com.example.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.BounceButton
import com.example.storage.MugenMobileStorage
import java.io.File

@Composable
fun HomeScreen(
    hasPermission: Boolean,
    onLaunchBuiltInGame: () -> Unit
) {
    val context = LocalContext.current
    var showLogs by remember { mutableStateOf(false) }
    var logContent by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!hasPermission) {
                Text(
                    text = "Storage permission is required to load characters and stages.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Text(
                text = "Ready to Fight!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            BounceButton(
                onClick = onLaunchBuiltInGame,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(64.dp)
            ) {
                Text(text = "START", style = MaterialTheme.typography.titleLarge)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedButton(onClick = {
                val logFile = File(MugenMobileStorage.getBaseDir(context), "mugen_crash.log")
                logContent = if (logFile.exists()) {
                    val lines = logFile.readLines()
                    // Get last 150 lines to avoid massive lag
                    lines.takeLast(150).joinToString("\n")
                } else {
                    "No crash logs found."
                }
                showLogs = true
            }) {
                Text("View Crash Logs")
            }
        }
    }

    if (showLogs) {
        Dialog(
            onDismissRequest = { showLogs = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize().padding(16.dp), shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Crash Logs (Last 150 lines)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = logContent,
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showLogs = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
