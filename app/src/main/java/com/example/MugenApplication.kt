package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.ContentRepository
import com.example.data.repository.GameRepository

class MugenApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { GameRepository(database.gameDao()) }
    val contentRepository by lazy { ContentRepository(database.contentDao()) }
}
