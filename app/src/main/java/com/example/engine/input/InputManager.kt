package com.example.engine.input

import com.example.engine.bridge.IkemenBridge

object InputManager {
    fun sendTouchInput(action: Int, x: Float, y: Float, pointerId: Int) {
        IkemenBridge.sendTouchInput(action, x, y, pointerId)
    }
    
    fun sendButtonInput(keyCode: Int, isPressed: Boolean) {
        IkemenBridge.sendKeyEvent(keyCode, isPressed)
    }
}
