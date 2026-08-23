package com.example.storage

import android.os.Environment
import java.io.File

object MugenMobileStorage {
    val baseDir: File by lazy {
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MUGEN_MOBILE")
    }
    
    val charsDir: File by lazy { File(baseDir, "chars") }
    val stagesDir: File by lazy { File(baseDir, "stages") }
    val gamesDir: File by lazy { File(baseDir, "games") }
    val dataDir: File by lazy { File(baseDir, "data") }
    val fontDir: File by lazy { File(baseDir, "font") }
    val soundDir: File by lazy { File(baseDir, "sound") }
    val engineDir: File by lazy { File(baseDir, "engine") }

    fun initializeDirectories() {
        if (!baseDir.exists()) baseDir.mkdirs()
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
