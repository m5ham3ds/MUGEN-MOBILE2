package com.example.ui.screens.controllerEditor

import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.settings.ControllerDataStore
import com.example.utils.HideSystemBars
import com.example.utils.LockScreenOrientation
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

data class ControllerButtonState(
    val id: String,
    val initialX: Float,
    val initialY: Float,
    val label: String,
    val scale: Float = 1.0f
)

enum class ControllerPreset(val displayName: String) {
    CLASSIC("Classic"),
    SIX_BUTTON("6-Button"),
    ARCADE("Arcade"),
    CUSTOM("Custom")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControllerEditorScreen(onNavigateBack: () -> Unit) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
    HideSystemBars()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var selectedPreset by remember { mutableStateOf(ControllerPreset.SIX_BUTTON) }
    var buttons by remember { mutableStateOf<List<ControllerButtonState>>(emptyList()) }
    var showPresetMenu by remember { mutableStateOf(false) }
    
    var selectedButtonId by remember { mutableStateOf<String?>(null) }

    // Load saved layout
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
                if (loadedButtons.isNotEmpty()) {
                    buttons = loadedButtons
                    selectedPreset = ControllerPreset.CUSTOM
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    // clicking on empty space deselects
                }
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        LaunchedEffect(selectedPreset, widthPx, heightPx) {
            if (widthPx > 0 && heightPx > 0 && selectedPreset != ControllerPreset.CUSTOM && buttons.isEmpty()) {
                buttons = getPresetLayout(selectedPreset, widthPx, heightPx)
            } else if (widthPx > 0 && heightPx > 0 && selectedPreset != ControllerPreset.CUSTOM) {
                buttons = getPresetLayout(selectedPreset, widthPx, heightPx)
            }
        }

        // Draw the buttons
        buttons.forEachIndexed { index, buttonState ->
            DraggableButton(
                buttonState = buttonState,
                isSelected = buttonState.id == selectedButtonId,
                onClick = { selectedButtonId = buttonState.id },
                onPositionChanged = { newX, newY ->
                    val updatedButtons = buttons.toMutableList()
                    updatedButtons[index] = buttonState.copy(initialX = newX, initialY = newY)
                    buttons = updatedButtons
                    selectedPreset = ControllerPreset.CUSTOM
                    selectedButtonId = buttonState.id
                }
            )
        }
        
        val selectedButton = buttons.find { it.id == selectedButtonId }

        // Floating Controls (Top Bar replacement)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                Text("Back")
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedButton != null) {
                    Text("Size:", color = Color.White, modifier = Modifier.padding(end = 8.dp))
                    Slider(
                        value = selectedButton.scale,
                        onValueChange = { newScale -> 
                            val updatedButtons = buttons.toMutableList()
                            val idx = updatedButtons.indexOfFirst { it.id == selectedButtonId }
                            if (idx != -1) {
                                updatedButtons[idx] = updatedButtons[idx].copy(scale = newScale)
                                buttons = updatedButtons
                                selectedPreset = ControllerPreset.CUSTOM
                            }
                        },
                        valueRange = 0.5f..3.0f,
                        modifier = Modifier.width(150.dp)
                    )
                } else {
                    Text("Select a button to resize", color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                    Spacer(modifier = Modifier.width(150.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Box {
                    Button(
                        onClick = { showPresetMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("Preset: ${selectedPreset.displayName}")
                    }
                    DropdownMenu(
                        expanded = showPresetMenu,
                        onDismissRequest = { showPresetMenu = false }
                    ) {
                        ControllerPreset.values().forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(preset.displayName) },
                                onClick = {
                                    selectedPreset = preset
                                    showPresetMenu = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val jsonArray = JSONArray()
                            buttons.forEach { b ->
                                val obj = JSONObject()
                                obj.put("id", b.id)
                                obj.put("x", b.initialX)
                                obj.put("y", b.initialY)
                                obj.put("scale", b.scale.toDouble())
                                jsonArray.put(obj)
                            }
                            ControllerDataStore.saveLayout(context, jsonArray.toString())
                            Toast.makeText(context, "Layout saved!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

fun getLabelForId(id: String): String {
    return when(id) {
        "dpad_up" -> "↑"
        "dpad_down" -> "↓"
        "dpad_left" -> "←"
        "dpad_right" -> "→"
        "btn_start" -> "START"
        "btn_menu" -> "MENU"
        else -> id.removePrefix("btn_").uppercase()
    }
}

fun getPresetLayout(preset: ControllerPreset, widthPx: Float, heightPx: Float): List<ControllerButtonState> {
    val unit = (widthPx * 0.08f).coerceIn(50f, 120f)
    
    val dpadCx = widthPx * 0.15f + unit
    val dpadCy = heightPx * 0.65f
    
    val dpad = listOf(
        ControllerButtonState("dpad_up", dpadCx, dpadCy - unit, "↑"),
        ControllerButtonState("dpad_down", dpadCx, dpadCy + unit, "↓"),
        ControllerButtonState("dpad_left", dpadCx - unit, dpadCy, "←"),
        ControllerButtonState("dpad_right", dpadCx + unit, dpadCy, "→")
    )
    
    val system = listOf(
        ControllerButtonState("btn_start", widthPx * 0.5f - unit * 0.8f, heightPx * 0.85f, "START", scale = 0.8f),
        ControllerButtonState("btn_menu", widthPx * 0.5f + unit * 0.8f, heightPx * 0.85f, "MENU", scale = 0.8f)
    )

    val actionCx = widthPx * 0.85f - unit
    val actionCy = heightPx * 0.65f

    val actionButtons = when (preset) {
        ControllerPreset.CLASSIC -> listOf(
            ControllerButtonState("btn_a", actionCx + unit, actionCy, "A"),
            ControllerButtonState("btn_b", actionCx, actionCy + unit, "B"),
            ControllerButtonState("btn_x", actionCx, actionCy - unit, "X"),
            ControllerButtonState("btn_y", actionCx - unit, actionCy, "Y")
        )
        ControllerPreset.ARCADE -> listOf(
            ControllerButtonState("btn_x", actionCx - unit, actionCy - unit*0.6f, "X"),
            ControllerButtonState("btn_y", actionCx, actionCy - unit*0.6f, "Y"),
            ControllerButtonState("btn_z", actionCx + unit, actionCy - unit*0.6f, "Z"),
            ControllerButtonState("btn_a", actionCx - unit, actionCy + unit*0.6f, "A"),
            ControllerButtonState("btn_b", actionCx, actionCy + unit*0.6f, "B"),
            ControllerButtonState("btn_c", actionCx + unit, actionCy + unit*0.6f, "C")
        )
        else -> listOf(
            ControllerButtonState("btn_x", actionCx - unit, actionCy - unit*0.5f, "X"),
            ControllerButtonState("btn_y", actionCx, actionCy - unit, "Y"),
            ControllerButtonState("btn_z", actionCx + unit, actionCy - unit*0.5f, "Z"),
            ControllerButtonState("btn_a", actionCx - unit, actionCy + unit*0.5f, "A"),
            ControllerButtonState("btn_b", actionCx, actionCy, "B"),
            ControllerButtonState("btn_c", actionCx + unit, actionCy + unit*0.5f, "C")
        )
    }
    
    return dpad + system + actionButtons
}

@Composable
fun DraggableButton(
    buttonState: ControllerButtonState,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPositionChanged: (Float, Float) -> Unit
) {
    var offsetX by remember(buttonState.initialX) { mutableStateOf(buttonState.initialX) }
    var offsetY by remember(buttonState.initialY) { mutableStateOf(buttonState.initialY) }

    val baseSize = if (buttonState.label.length > 1) 80.dp else 64.dp
    val scaledSize = baseSize * buttonState.scale

    val borderModifier = if (isSelected) Modifier.border(2.dp, Color.Yellow, CircleShape) else Modifier

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(scaledSize)
            .then(borderModifier)
            .background(Color.White.copy(alpha = if (isSelected) 0.4f else 0.2f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onClick() },
                    onDragEnd = { onPositionChanged(offsetX, offsetY) }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = buttonState.label, 
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
