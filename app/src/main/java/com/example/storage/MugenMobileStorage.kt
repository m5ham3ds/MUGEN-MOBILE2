package com.example.storage

import android.os.Environment
import java.io.File

object MugenMobileStorage {
    private var customBaseDir: File? = null

    fun getBaseDir(context: android.content.Context): File {
        if (customBaseDir != null) return customBaseDir!!
        val sharedPrefs = context.getSharedPreferences("IkemenGo", android.content.Context.MODE_PRIVATE)
        val path = sharedPrefs.getString("folder", "")
        if (!path.isNullOrEmpty()) {
            customBaseDir = File(path)
            return customBaseDir!!
        }
        return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MUGEN_MOBILE")
    }
    
    // For legacy compatibility where context is not passed, use the default or custom if initialized
    val baseDir: File 
        get() = customBaseDir ?: File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MUGEN_MOBILE")
    
    val charsDir: File get() = File(baseDir, "chars")
    val stagesDir: File get() = File(baseDir, "stages")
    val gamesDir: File get() = File(baseDir, "games")
    val dataDir: File get() = File(baseDir, "data")
    val fontDir: File get() = File(baseDir, "font")
    val soundDir: File get() = File(baseDir, "sound")
    val engineDir: File get() = File(baseDir, "engine")

    fun initializeDirectories(context: android.content.Context? = null) {
        val currentBaseDir = if (context != null) getBaseDir(context) else baseDir
        
        if (!currentBaseDir.exists()) currentBaseDir.mkdirs()
        if (!charsDir.exists()) charsDir.mkdirs()
        if (!stagesDir.exists()) stagesDir.mkdirs()
        if (!gamesDir.exists()) gamesDir.mkdirs()
        if (!dataDir.exists()) dataDir.mkdirs()
        if (!fontDir.exists()) fontDir.mkdirs()
        if (!soundDir.exists()) soundDir.mkdirs()
        if (!engineDir.exists()) engineDir.mkdirs()
    }
    
    fun listCharacters(): List<String> {
        if (!charsDir.exists()) return emptyList()
        // Characters are usually folders inside chars/ or .def files
        return charsDir.listFiles { file -> file.isDirectory }?.map { it.name } ?: emptyList()
    }

    fun listStages(): List<String> {
        if (!stagesDir.exists()) return emptyList()
        // Stages are usually .def files
        return stagesDir.listFiles { file -> file.extension == "def" }?.map { it.nameWithoutExtension } ?: emptyList()
    }
}
