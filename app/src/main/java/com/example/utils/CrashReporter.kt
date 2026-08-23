package com.example.utils

import android.content.Context
import android.util.Log
import com.example.storage.MugenMobileStorage
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashReporter {
    private const val TAG = "CrashReporter"
    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null

    fun initialize(context: Context) {
        if (defaultExceptionHandler != null) return
        
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleException(thread, throwable)
            defaultExceptionHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun handleException(thread: Thread, throwable: Throwable) {
        try {
            val crashDir = File(MugenMobileStorage.baseDir, "crash")
            if (!crashDir.exists()) {
                crashDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val crashFile = File(crashDir, "crash_$timestamp.txt")

            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            val stackTrace = sw.toString()

            val crashLog = """
                --- MUGEN MOBILE CRASH REPORT ---
                Time: $timestamp
                Thread: ${thread.name}
                
                Exception:
                $stackTrace
            """.trimIndent()

            crashFile.writeText(crashLog)
            Log.e(TAG, "Crash log saved to: ${crashFile.absolutePath}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save crash log", e)
        }
    }
}
