package com.example.engine.lifecycle

import android.content.Context
import android.util.Log
import com.example.engine.bridge.IkemenBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class GameState {
    IDLE, LOADING, RUNNING, PAUSED, ERROR
}

object GameLifecycleManager {
    private const val TAG = "GameLifecycleManager"
    private val _gameState = MutableStateFlow(GameState.IDLE)
    val gameState: StateFlow<GameState> = _gameState

    fun launchGame(gamePath: String, context: Context? = null) {
        if (gamePath.isBlank()) {
            _gameState.value = GameState.ERROR
            return
        }
        _gameState.value = GameState.LOADING
        try {
            IkemenBridge.loadLibrary(context)
            IkemenBridge.initEngine(gamePath, gamePath)
            IkemenBridge.startEngine()
            _gameState.value = GameState.RUNNING
        } catch (error: Exception) {
            Log.e(TAG, "Failed to launch game at $gamePath", error)
            _gameState.value = GameState.ERROR
        }
    }

    fun pauseGame() {
        if (_gameState.value == GameState.RUNNING) {
            IkemenBridge.pauseEngine()
            _gameState.value = GameState.PAUSED
        }
    }

    fun resumeGame() {
        if (_gameState.value == GameState.PAUSED) {
            IkemenBridge.resumeEngine()
            _gameState.value = GameState.RUNNING
        }
    }

    fun stopGame() {
        if (_gameState.value != GameState.IDLE) {
            IkemenBridge.stopEngine()
            _gameState.value = GameState.IDLE
        }
    }
}
