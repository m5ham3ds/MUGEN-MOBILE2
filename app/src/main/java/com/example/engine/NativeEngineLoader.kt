package com.example.engine

import android.content.Context
import android.util.Log
import java.io.File

/** Validates and loads the native engine packaged in the APK. */
object NativeEngineLoader {
    private const val TAG = "NativeEngineLoader"

    private val bundledLibraries = listOf(
        "SDL2", "avutil", "swresample", "swscale",
        "avcodec", "avformat", "avfilter", "avdevice", "xmp", "main"
    )

    fun hasEngineFiles(context: Context): Boolean {
        val nativeDir = context.applicationInfo.nativeLibraryDir ?: return false
        return bundledLibraries.all { library ->
            File(nativeDir, System.mapLibraryName(library)).isFile
        }
    }

    fun loadEngine(context: Context): Pair<Boolean, String> {
        if (!hasEngineFiles(context)) {
            return false to "One or more native engine libraries are missing from the APK."
        }

        return try {
            bundledLibraries.forEach { library ->
                System.loadLibrary(library)
                Log.d(TAG, "Loaded bundled library: lib$library.so")
            }
            true to "Native engine loaded successfully from APK."
        } catch (error: UnsatisfiedLinkError) {
            Log.e(TAG, "Unable to load native engine", error)
            false to "Failed to load native library: ${error.message ?: "unknown linker error"}"
        } catch (error: SecurityException) {
            Log.e(TAG, "Security policy blocked native engine loading", error)
            false to "Security error while loading the native engine."
        }
    }
}
