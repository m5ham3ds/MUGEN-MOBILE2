package com.example.engine

import android.content.Context
import android.util.Log
import com.example.storage.MugenMobileStorage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object NativeEngineLoader {
    private const val TAG = "NativeEngineLoader"
    
    // Check if the engine files exist in the user's MUGEN_MOBILE/engine directory
    fun hasEngineFiles(): Boolean {
        val engineDir = MugenMobileStorage.engineDir
        if (!engineDir.exists()) return false
        val soFiles = engineDir.listFiles { file -> file.extension == "so" } ?: emptyArray()
        return soFiles.isNotEmpty()
    }

    fun loadEngine(context: Context): Pair<Boolean, String> {
        val engineDir = MugenMobileStorage.engineDir
        if (!engineDir.exists()) return Pair(false, "Engine directory not found at ${engineDir.absolutePath}")

        val soFiles = engineDir.listFiles { file -> file.extension == "so" } ?: emptyArray()
        if (soFiles.isEmpty()) return Pair(false, "No .so files found in ${engineDir.absolutePath}. Please download and extract them there.")

        // Android requires native libraries to be loaded from app-internal executable directories
        val internalLibDir = context.getDir("ikemen_libs", Context.MODE_PRIVATE)
        
        try {
            // Usually SDL2 must be loaded before the main engine library
            val sdlLib = soFiles.find { it.name.contains("SDL2", ignoreCase = true) }
            val ikemenLib = soFiles.find { it.name.contains("ikemen", ignoreCase = true) }

            val orderedLibs = listOfNotNull(sdlLib, ikemenLib) + soFiles.filter { it != sdlLib && it != ikemenLib }

            for (sourceSo in orderedLibs) {
                val destSo = File(internalLibDir, sourceSo.name)
                // Copy if it doesn't exist or if the size differs (indicating an update)
                if (!destSo.exists() || destSo.length() != sourceSo.length()) {
                    Log.d(TAG, "Copying ${sourceSo.name} to internal storage...")
                    FileInputStream(sourceSo).use { fis ->
                        FileOutputStream(destSo).use { fos ->
                            fis.copyTo(fos)
                        }
                    }
                }
                
                Log.d(TAG, "Loading ${destSo.name}...")
                // Load it using the absolute path of the internal copy
                System.load(destSo.absolutePath)
            }
            return Pair(true, "Native engine libraries loaded successfully!")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
            return Pair(false, "Link Error: ${e.message}")
        } catch (e: SecurityException) {
            e.printStackTrace()
            return Pair(false, "Security Error: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(false, "Error loading engine: ${e.message}")
        }
    }
}
