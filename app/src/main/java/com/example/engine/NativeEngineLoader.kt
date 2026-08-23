package com.example.engine

import android.content.Context
import android.util.Log
import com.example.storage.MugenMobileStorage
import java.io.File

object NativeEngineLoader {
    private const val TAG = "NativeEngineLoader"
    
    // Check if the engine files exist in the user's MUGEN_MOBILE/engine directory
    // Now it only checks if libikemen.so is available (either bundled or external)
    fun hasEngineFiles(): Boolean {
        val engineDir = MugenMobileStorage.engineDir
        val externalIkemen = File(engineDir, "libikemen.so")
        return externalIkemen.exists()
    }

    fun loadEngine(context: Context): Pair<Boolean, String> {
        try {
            Log.d(TAG, "Loading bundled dependencies...")
            // Load bundled libraries (from jniLibs in APK)
            val bundledLibs = listOf(
                "SDL2", "avutil", "swresample", "swscale", 
                "avcodec", "avformat", "avfilter", "avdevice", "xmp"
            )
            
            for (lib in bundledLibs) {
                try {
                    System.loadLibrary(lib)
                    Log.d(TAG, "Loaded bundled library: lib${lib}.so")
                } catch (e: UnsatisfiedLinkError) {
                    Log.w(TAG, "Could not load bundled library $lib, it may not be packaged.")
                }
            }

            // Android requires native libraries to be loaded from app-internal executable directories
            val internalLibDir = context.getDir("ikemen_libs", Context.MODE_PRIVATE)
            val engineDir = MugenMobileStorage.engineDir
            
            // Look for libikemen.so in external engine directory
            val externalIkemen = File(engineDir, "libikemen.so")
            
            if (externalIkemen.exists()) {
                val internalIkemen = File(internalLibDir, "libikemen.so")
                if (!internalIkemen.exists() || internalIkemen.length() != externalIkemen.length()) {
                    Log.d(TAG, "Copying libikemen.so to internal storage...")
                    externalIkemen.inputStream().use { fis ->
                        internalIkemen.outputStream().use { fos ->
                            fis.copyTo(fos)
                        }
                    }
                }
                Log.d(TAG, "Loading libikemen.so from internal storage...")
                System.load(internalIkemen.absolutePath)
                return Pair(true, "Native engine loaded successfully from external directory!")
            } else {
                // Try to load bundled libikemen.so if it exists
                try {
                    System.loadLibrary("ikemen")
                    return Pair(true, "Native engine loaded successfully from APK!")
                } catch (e: UnsatisfiedLinkError) {
                    return Pair(false, "libikemen.so not found in APK or ${engineDir.absolutePath}")
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            return Pair(false, "Security Error: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(false, "Error loading engine: ${e.message}")
        }
    }
}
