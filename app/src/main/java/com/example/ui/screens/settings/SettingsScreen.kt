package com.example.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.material.icons.filled.Download
import com.example.engine.EngineDataDownloader
import com.example.engine.DownloadState
import kotlinx.coroutines.launch

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close

@Composable
fun SettingsScreen(
    onNavigateToControllerEditor: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var isFullscreen by remember { mutableStateOf(true) }
    var vsync by remember { mutableStateOf(true) }
    var useIntegerScaling by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val downloadState by EngineDataDownloader.downloadState.collectAsState()
    val progressMessage by EngineDataDownloader.progressMessage.collectAsState()
    val progressPercentage by EngineDataDownloader.progressPercentage.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        EngineDataDownloader.startDownload(context)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Graphics & Engine", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        item {
            SettingsSwitch(
                title = "Fullscreen",
                subtitle = "Run the engine in immersive mode",
                checked = isFullscreen,
                onCheckedChange = { isFullscreen = it }
            )
        }
        item {
            SettingsSwitch(
                title = "VSync",
                subtitle = "Synchronize frame rate to screen refresh rate",
                checked = vsync,
                onCheckedChange = { vsync = it }
            )
        }
        
        item {
            SettingsSwitch(
                title = "Integer Scaling",
                subtitle = "Preserve pixel art sharpness on high-res screens",
                checked = useIntegerScaling,
                onCheckedChange = { useIntegerScaling = it }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Engine Data", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Base Engine Data", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Download standard MUGEN system files to your local folder.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (downloadState == DownloadState.DOWNLOADING || downloadState == DownloadState.EXTRACTING || downloadState == DownloadState.PAUSED) {
                        if (downloadState == DownloadState.EXTRACTING) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        } else {
                            LinearProgressIndicator(
                                progress = { progressPercentage },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(progressMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        
                        if (downloadState == DownloadState.DOWNLOADING || downloadState == DownloadState.PAUSED) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                if (downloadState == DownloadState.PAUSED) {
                                    IconButton(onClick = { EngineDataDownloader.startDownload(context) }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                                    }
                                } else {
                                    IconButton(onClick = { EngineDataDownloader.pauseDownload() }) {
                                        Icon(Icons.Default.Pause, contentDescription = "Pause")
                                    }
                                }
                                IconButton(onClick = { EngineDataDownloader.cancelDownload() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { 
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    EngineDataDownloader.startDownload(context)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Download Base Data")
                        }
                        if (downloadState == DownloadState.SUCCESS || downloadState == DownloadState.ERROR) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(progressMessage, style = MaterialTheme.typography.bodySmall, color = if (downloadState == DownloadState.ERROR) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Controls", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Card(
                onClick = onNavigateToControllerEditor,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VideogameAsset,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Edit Controller Layout", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("Customize on-screen buttons for MUGEN", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
