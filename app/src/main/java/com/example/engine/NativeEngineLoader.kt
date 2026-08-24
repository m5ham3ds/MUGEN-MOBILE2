package com.example.engine

import android.content.Context
import android.util.Log

object NativeEngineLoader {
    private const val TAG = "NativeEngineLoader"
    
    fun hasEngineFiles(): Boolean {
        return true
    }

    fun loadEngine(context: Context): Pair<Boolean, String> {
        try {
            Log.d(TAG, "Loading bundled dependencies...")
            // Load bundled libraries (from jniLibs in APK)
            val bundledLibs = listOf(
                "SDL2", "avutil", "swresample", "swscale", 
                "avcodec", "avformat", "avfilter", "avdevice", "xmp", "main"
            )
            
            for (lib in bundledLibs) {
                try {
                    System.loadLibrary(lib)
                    Log.d(TAG, "Loaded bundled library: lib\${lib}.so")
                } catch (e: UnsatisfiedLinkError) {
                    Log.w(TAG, "Could not load bundled library $lib: ${e.message}")
                    return Pair(false, "Failed to load $lib: ${e.message}")
                }
            }
            
            return Pair(true, "Native engine loaded successfully from APK!")
        } catch (e: SecurityException) {
            e.printStackTrace()
            return Pair(false, "Security Error: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(false, "Error loading engine: ${e.message}")
        }
    }
}
