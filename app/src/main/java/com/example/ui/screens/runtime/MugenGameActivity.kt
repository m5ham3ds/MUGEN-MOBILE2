package com.example.ui.screens.runtime

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.ViewGroup
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.settings.ControllerDataStore
import com.example.engine.NativeEngineLoader
import com.example.ui.screens.controllerEditor.ControllerButtonState
import com.example.ui.screens.controllerEditor.ControllerPreset
import com.example.ui.screens.controllerEditor.getLabelForId
import com.example.ui.screens.controllerEditor.getPresetLayout
import com.example.ui.screens.overlay.GameOverlay
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.libsdl.app.SDLActivity
import org.libsdl.app.SDL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MugenGameActivity : SDLActivity() {
    override fun getLibraries(): Array<String> {
        return arrayOf("main")
    }

    override fun loadLibraries() {
        val (success, message) = NativeEngineLoader.loadEngine(this)
        if (!success) {
            throw UnsatisfiedLinkError(message)
        }
    }

    override fun getArguments(): Array<String> {
        val intentPath = intent.getStringExtra("gamePath") ?: ""
        val decodedPath = try {
            URLDecoder.decode(intentPath, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            intentPath
        }
        
        // Setup any Ikemen-specific arguments if needed here
        return super.getArguments()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // We handle fullscreen and orientation before super
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        
        super.onCreate(savedInstanceState)
        
        // Remove standard SDLActivity UI
        try {
            mLayout?.removeAllViews()
        } catch(e: Exception) {}

        // Add our Compose overlay
        val composeView = ComposeView(this).apply {
            setContent {
                GameScreenOverlay(onExit = { finish() })
            }
        }
        
        mLayout?.addView(composeView, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
    }
}

@Composable
fun GameScreenOverlay(onExit: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat() * configuration.densityDpi / 160f
    val screenHeight = configuration.screenHeightDp.toFloat() * configuration.densityDpi / 160f
    
    var showOverlay by remember { mutableStateOf(false) }
    var controllerButtons by remember { mutableStateOf<List<ControllerButtonState>>(emptyList()) }

    LaunchedEffect(Unit) {
        val savedLayout = ControllerDataStore.getLayout(context).firstOrNull()
        if (!savedLayout.isNullOrEmpty()) {
            try {
                val jsonArray = JSONArray(savedLayout)
                val loadedButtons = mutableListOf<ControllerButtonState>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.getString("id")
                    loadedButtons.add(
                        ControllerButtonState(
                            id = id,
                            initialX = obj.getDouble("x").toFloat(),
                            initialY = obj.getDouble("y").toFloat(),
                            label = getLabelForId(id),
                            scale = if (obj.has("scale")) obj.getDouble("scale").toFloat() else 1.0f
                        )
                    )
                }
                controllerButtons = loadedButtons
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            controllerButtons = getPresetLayout(ControllerPreset.SIX_BUTTON, screenWidth, screenHeight)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        
        // Draw On-Screen Controller
        if (controllerButtons.isNotEmpty()) {
            VirtualController(
                buttons = controllerButtons,
                onButtonPressed = { id ->
                    if (id == "btn_menu") {
                        showOverlay = true
                    } else {
                        SDLActivity.onNativeKeyDown(buttonIdToKeyCode(id))
                    }
                },
                onButtonReleased = { id ->
                    if (id != "btn_menu") {
                        SDLActivity.onNativeKeyUp(buttonIdToKeyCode(id))
                    }
                }
            )
        } else {
            Button(
                onClick = {
                    showOverlay = true
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Text("Menu")
            }
        }

        if (showOverlay) {
            GameOverlay(
                onResume = { showOverlay = false },
                onExit = { 
                    showOverlay = false
                    onExit()
                }
            )
        }
    }
}

fun buttonIdToKeyCode(id: String): Int {
    // Android KeyEvent mappings
    return when(id) {
        "dpad_up" -> android.view.KeyEvent.KEYCODE_DPAD_UP // 19
        "dpad_down" -> android.view.KeyEvent.KEYCODE_DPAD_DOWN // 20
        "dpad_left" -> android.view.KeyEvent.KEYCODE_DPAD_LEFT // 21
        "dpad_right" -> android.view.KeyEvent.KEYCODE_DPAD_RIGHT // 22
        "btn_a" -> android.view.KeyEvent.KEYCODE_BUTTON_A // 96
        "btn_b" -> android.view.KeyEvent.KEYCODE_BUTTON_B // 97
        "btn_x" -> android.view.KeyEvent.KEYCODE_BUTTON_X // 99
        "btn_y" -> android.view.KeyEvent.KEYCODE_BUTTON_Y // 100
        "btn_z" -> android.view.KeyEvent.KEYCODE_BUTTON_Z // 101
        "btn_c" -> android.view.KeyEvent.KEYCODE_BUTTON_C // 102
        "btn_start" -> android.view.KeyEvent.KEYCODE_BUTTON_START // 108
        else -> 0
    }
}
