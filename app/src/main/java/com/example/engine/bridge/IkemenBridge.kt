package com.example.engine.bridge

import android.content.Context
import android.util.Log
import com.example.engine.NativeEngineLoader

object IkemenBridge {
    private const val TAG = "IkemenBridge"
    private var isEngineLoaded = false
    private var appContext: Context? = null

    fun loadLibrary(context: Context? = null) {
        if (isEngineLoaded) return
        val safeContext = (context ?: appContext)?.applicationContext ?: return
        appContext = safeContext
        try {
            val result = NativeEngineLoader.loadEngine(safeContext)
            if (!result.first) {
                throw UnsatisfiedLinkError(result.second)
            }
            isEngineLoaded = true
        } catch (error: UnsatisfiedLinkError) {
            Log.e(TAG, "Native library failed to load", error)
            throw error
        }
    }

    fun initEngine(assetsPath: String, storagePath: String) {
        if (!isEngineLoaded) {
            throw IllegalStateException("Engine is not loaded")
        }
        Log.d(TAG, "initEngine assets=$assetsPath storage=$storagePath")
    }

    fun startEngine() {
        if (!isEngineLoaded) {
            throw IllegalStateException("Engine is not loaded")
        }
        Log.d(TAG, "startEngine")
    }

    fun stopEngine() {
        Log.d(TAG, "stopEngine")
    }

    fun pauseEngine() {
        Log.d(TAG, "pauseEngine")
    }

    fun resumeEngine() {
        Log.d(TAG, "resumeEngine")
    }

    fun sendTouchInput(action: Int, x: Float, y: Float, pointerId: Int) {
        if (!isEngineLoaded) {
            Log.w(TAG, "Touch input ignored because engine is not loaded")
            return
        }
        Log.d(TAG, "sendTouchInput action=$action x=$x y=$y pointerId=$pointerId")
    }

    fun sendKeyEvent(keyCode: Int, isDown: Boolean) {
        if (!isEngineLoaded) {
            Log.w(TAG, "Key event ignored because engine is not loaded")
            return
        }
        Log.d(TAG, "sendKeyEvent code=$keyCode down=$isDown")
    }
}
