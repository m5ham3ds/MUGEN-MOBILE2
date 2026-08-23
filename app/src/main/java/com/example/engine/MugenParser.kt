package com.example.engine

import android.net.Uri
import com.example.storage.MugenMobileStorage
import java.io.File

data class CharacterData(
    val folderName: String,
    val displayName: String,
    val imageUri: Uri?,
    val cnsFile: File?
)

data class StageData(
    val defFileName: String,
    val displayName: String,
    val imageUri: Uri?
)

class CnsEditor(val cnsFile: File) {
    val lines = cnsFile.readLines().toMutableList()
    val properties = mutableMapOf<String, MutableMap<String, CnsProperty>>()
    
    data class CnsProperty(val section: String, val key: String, var value: String, val lineIndex: Int)
    
    init {
        val allowedSections = listOf("[data]", "[size]", "[velocity]", "[movement]")
        var currentSection = ""
        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.lowercase()
            } else if (currentSection in allowedSections && line.contains("=")) {
                val key = line.substringBefore("=").trim().lowercase()
                val valueStr = line.substringAfter("=").substringBefore(";").trim()
                if (key.isNotEmpty()) {
                    if (properties[currentSection] == null) properties[currentSection] = mutableMapOf()
                    if (!properties[currentSection]!!.containsKey(key)) {
                        properties[currentSection]!![key] = CnsProperty(currentSection, key, valueStr, i)
                    }
                }
            }
        }
    }
    
    fun save() {
        for (sec in properties.values) {
            for (prop in sec.values) {
                val originalLine = lines[prop.lineIndex]
                val commentPart = if (originalLine.contains(";")) " ;" + originalLine.substringAfter(";") else ""
                lines[prop.lineIndex] = "${prop.key} = ${prop.value}$commentPart"
            }
        }
        cnsFile.writeText(lines.joinToString("\n"))
    }
}

object MugenParser {
    private val cacheDir by lazy { File(MugenMobileStorage.baseDir, ".cache").apply { if (!exists()) mkdirs() } }

    fun getCharacters(): List<CharacterData> {
        val charsDir = MugenMobileStorage.charsDir
        if (!charsDir.exists()) return emptyList()
        val list = mutableListOf<CharacterData>()
        val folders = charsDir.listFiles { file -> file.isDirectory } ?: return emptyList()
        for (folder in folders) {
            val allFiles = folder.walkTopDown().toList()
            val defFile = allFiles.find { it.name.equals("${folder.name}.def", ignoreCase = true) }
                ?: allFiles.find { it.extension.lowercase() == "def" && !it.name.startsWith("intro", ignoreCase=true) && !it.name.startsWith("ending", ignoreCase=true) }

            if (defFile != null) {
                list.add(parseCharacter(folder, defFile, allFiles))
            } else {
                list.add(CharacterData(folder.name, folder.name, findImageInFolder(folder, allFiles), null))
            }
        }
        return list
    }

    private fun parseCharacter(folder: File, defFile: File, allFiles: List<File>): CharacterData {
        var displayName = folder.name
        var cnsFileName = "${folder.name}.cns"
        
        try {
            defFile.readLines().forEach { line ->
                val cleanLine = line.substringBefore(";").trim()
                if (cleanLine.lowercase().startsWith("displayname")) {
                    displayName = cleanLine.substringAfter("=").replace("\"", "").trim()
                }
                if (cleanLine.lowercase().startsWith("cns")) {
                    cnsFileName = cleanLine.substringAfter("=").replace("\"", "").trim()
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        
        val cnsFile = allFiles.find { f -> f.name.equals(cnsFileName, ignoreCase = true) }
        return CharacterData(folder.name, displayName, findImageInFolder(folder, allFiles), cnsFile)
    }

    fun getStages(): List<StageData> {
        val stagesDir = MugenMobileStorage.stagesDir
        if (!stagesDir.exists()) return emptyList()
        val list = mutableListOf<StageData>()
        val defFiles = stagesDir.listFiles { file -> file.extension.lowercase() == "def" } ?: return emptyList()
        for (defFile in defFiles) {
            var displayName = defFile.nameWithoutExtension
            var sffFileName = ""
            try {
                defFile.readLines().forEach { line ->
                    val cleanLine = line.substringBefore(";").trim()
                    if (cleanLine.lowercase().startsWith("displayname")) {
                        displayName = cleanLine.substringAfter("=").replace("\"", "").trim()
                    }
                    if (cleanLine.lowercase().startsWith("spr")) {
                        sffFileName = cleanLine.substringAfter("=").replace("stages/", "").trim()
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }

            val baseName = defFile.nameWithoutExtension
            var imgUri = File(stagesDir, "$baseName.png").takeIf { it.exists() }?.let { Uri.fromFile(it) }
                ?: File(stagesDir, "$baseName.jpg").takeIf { it.exists() }?.let { Uri.fromFile(it) }

            // If no manual image, extract from SFF!
            if (imgUri == null) {
                val sffFile = if (sffFileName.isNotEmpty()) {
                    File(stagesDir, sffFileName).takeIf { it.exists() }
                } else null ?: File(stagesDir, "$baseName.sff").takeIf { it.exists() }

                if (sffFile != null) {
                    imgUri = SffParser.extractPortrait(sffFile, cacheDir, "stage_$baseName")
                }
            }
            list.add(StageData(defFile.name, displayName, imgUri))
        }
        return list
    }

    private fun findImageInFolder(folder: File, allFiles: List<File>? = null): Uri? {
        val filesToSearch = allFiles ?: folder.walkTopDown().toList()
        
        // Try to find manual portrait first
        val manualImgs = filesToSearch.filter { file -> 
            val ext = file.extension.lowercase()
            ext == "png" || ext == "jpg" || ext == "jpeg" 
        }
        val preferred = manualImgs.find { it.nameWithoutExtension.lowercase() == "portrait" || it.nameWithoutExtension.lowercase() == "face" }
        if (preferred != null) return Uri.fromFile(preferred)
        
        // No manual PNG, let's extract from .sff!
        val sffFile = filesToSearch.find { it.name.equals("${folder.name}.sff", ignoreCase = true) }
            ?: filesToSearch.find { f -> f.extension.lowercase() == "sff" }
            
        if (sffFile != null) {
            val extracted = SffParser.extractPortrait(sffFile, cacheDir, "char_${folder.name}")
            if (extracted != null) return extracted
        }
        return manualImgs.firstOrNull()?.let { Uri.fromFile(it) }
    }
}
