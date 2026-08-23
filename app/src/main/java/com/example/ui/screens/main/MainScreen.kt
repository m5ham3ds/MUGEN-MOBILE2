package com.example.ui.screens.main

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.storage.MugenMobileStorage
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.characters.CharactersScreen
import com.example.ui.screens.stages.StagesScreen
import com.example.ui.screens.games.GamesScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.utils.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLaunchBuiltInGame: () -> Unit,
    onLaunchCustomGame: (String) -> Unit,
    onNavigateToControllerEditor: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(PermissionUtils.hasStoragePermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = PermissionUtils.hasStoragePermission(context)
        if (hasPermission) {
            MugenMobileStorage.initializeDirectories()
        }
    }

    val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasPermission = PermissionUtils.hasStoragePermission(context)
        if (hasPermission) {
            MugenMobileStorage.initializeDirectories()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                PermissionUtils.requestManageStorageIntent(context)?.let {
                    manageStorageLauncher.launch(it)
                }
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        } else {
            MugenMobileStorage.initializeDirectories()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MUGEN MOBILE") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Play") },
                    label = { Text("Play") },
                    selected = currentRoute == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Characters") },
                    label = { Text("Characters") },
                    selected = currentRoute == "characters",
                    onClick = {
                        navController.navigate("characters") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Map, contentDescription = "Stages") },
                    label = { Text("Stages") },
                    selected = currentRoute == "stages",
                    onClick = {
                        navController.navigate("stages") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Games, contentDescription = "Games") },
                    label = { Text("Games") },
                    selected = currentRoute == "games",
                    onClick = {
                        navController.navigate("games") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    hasPermission = hasPermission,
                    onLaunchBuiltInGame = onLaunchBuiltInGame
                )
            }
            composable("characters") {
                CharactersScreen(hasPermission = hasPermission)
            }
            composable("stages") {
                StagesScreen(hasPermission = hasPermission)
            }
            composable("games") {
                GamesScreen(
                    hasPermission = hasPermission,
                    onLaunchCustomGame = onLaunchCustomGame
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateToControllerEditor = onNavigateToControllerEditor,
                    onNavigateBack = {}
                )
            }
        }
    }
}
