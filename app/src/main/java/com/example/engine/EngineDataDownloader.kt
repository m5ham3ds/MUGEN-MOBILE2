package com.example.engine

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.storage.MugenMobileStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream

enum class DownloadState { IDLE, DOWNLOADING, PAUSED, EXTRACTING, SUCCESS, ERROR }

object EngineDataDownloader {
    private const val TAG = "EngineDataDownloader"
    private const val REPO_ZIP_URL = "https://github.com/m5ham3ds/MUGEN/archive/refs/heads/main.zip"

    private val client = OkHttpClient()
    private val _downloadState = MutableStateFlow(DownloadState.IDLE)
    val downloadState: StateFlow<DownloadState> = _downloadState
    private val _progressMessage = MutableStateFlow("")
    val progressMessage: StateFlow<String> = _progressMessage
    private val _progressPercentage = MutableStateFlow(0f)
    val progressPercentage: StateFlow<Float> = _progressPercentage

    private var downloadJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    @Volatile private var shouldPause = false
    @Volatile private var shouldCancel = false

    private fun isInternetAvailable(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun startDownload(context: Context) {
        if (_downloadState.value == DownloadState.DOWNLOADING || _downloadState.value == DownloadState.EXTRACTING) return
        MugenMobileStorage.initializeDirectories(context)
        if (File(MugenMobileStorage.dataDir, "system.def").isFile) {
            _progressMessage.value = "Engine data is already installed."
            return
        }
        if (!isInternetAvailable(context)) {
            _downloadState.value = DownloadState.ERROR
            _progressMessage.value = "No internet connection."
            return
        }
        shouldPause = false
        shouldCancel = false
        val intent = Intent(context, DownloadService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent)
        downloadJob?.cancel()
        downloadJob = scope.launch { executeDownload(context.applicationContext) }
    }

    fun pauseDownload() { shouldPause = true }
    fun cancelDownload() { shouldCancel = true }

    private suspend fun executeDownload(context: Context) {
        _downloadState.value = DownloadState.DOWNLOADING
        val zipFile = File(context.cacheDir, "mugen_data.zip")
        try {
            var downloaded = if (zipFile.isFile) zipFile.length() else 0L
            val request = Request.Builder().url(REPO_ZIP_URL).apply {
                if (downloaded > 0L) header("Range", "bytes=$downloaded-")
            }.build()
            client.newCall(request).execute().use { response ->
                val resumed = downloaded > 0L
                if (!response.isSuccessful || (resumed && response.code != 206)) {
                    if (resumed && response.code == 200) {
                        zipFile.delete()
                        downloaded = 0L
                    } else throw IOException("Server returned HTTP ${response.code}")
                }
                if (downloaded == 0L && response.code != 200) throw IOException("Unexpected HTTP ${response.code}")
                val body = response.body ?: throw IOException("Empty response body")
                val contentLength = body.contentLength()
                val totalLength = if (response.code == 206) downloaded + contentLength else contentLength
                FileOutputStream(zipFile, response.code == 206).use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            if (shouldCancel) { zipFile.delete(); setCancelled(context); return }
                            if (shouldPause) { _downloadState.value = DownloadState.PAUSED; _progressMessage.value = "Download paused."; return }
                            val count = input.read(buffer)
                            if (count == -1) break
                            output.write(buffer, 0, count)
                            downloaded += count
                            if (totalLength > 0) _progressPercentage.value = downloaded.toFloat() / totalLength
                            _progressMessage.value = "Downloading: ${downloaded / 1024 / 1024} MB"
                        }
                    }
                }
            }
            extractZip(zipFile, context)
        } catch (error: Exception) {
            Log.e(TAG, "Download failed", error)
            _downloadState.value = DownloadState.ERROR
            _progressMessage.value = "Error: ${error.localizedMessage ?: "download failed"}"
            context.stopService(Intent(context, DownloadService::class.java))
        }
    }

    private fun setCancelled(context: Context) {
        _downloadState.value = DownloadState.IDLE
        _progressMessage.value = "Download cancelled."
        _progressPercentage.value = 0f
        context.stopService(Intent(context, DownloadService::class.java))
    }

    private suspend fun extractZip(zipFile: File, context: Context) {
        _downloadState.value = DownloadState.EXTRACTING
        _progressMessage.value = "Extracting files to data..."
        withContext(Dispatchers.IO) {
            try {
                val dataDir = MugenMobileStorage.dataDir.apply { mkdirs() }
                val root = dataDir.canonicalFile
                ZipInputStream(BufferedInputStream(zipFile.inputStream())).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory) {
                            val relativeName = entry.name.replace('\\', '/')
                            val destination = File(root, relativeName).canonicalFile
                            val rootPath = root.path + File.separator
                            if (destination.path != root.path && !destination.path.startsWith(rootPath)) {
                                throw SecurityException("Unsafe ZIP entry: ${entry.name}")
                            }
                            destination.parentFile?.mkdirs()
                            FileOutputStream(destination).use { output -> zip.copyTo(output) }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
                zipFile.delete()
                _progressPercentage.value = 1f
                _downloadState.value = DownloadState.SUCCESS
                _progressMessage.value = "Engine data successfully installed!"
                context.stopService(Intent(context, DownloadService::class.java))
            } catch (error: Exception) {
                Log.e(TAG, "Extraction failed", error)
                _downloadState.value = DownloadState.ERROR
                _progressMessage.value = "Extract error: ${error.localizedMessage ?: "invalid archive"}"
                context.stopService(Intent(context, DownloadService::class.java))
            }
        }
    }

    fun reset() {
        _downloadState.value = DownloadState.IDLE
        _progressMessage.value = ""
        _progressPercentage.value = 0f
    }
}
