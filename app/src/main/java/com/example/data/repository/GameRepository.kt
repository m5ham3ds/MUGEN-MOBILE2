package com.example.data.repository

import com.example.data.db.GameDao
import com.example.data.model.GameEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {

    val allGames: Flow<List<GameEntity>> = gameDao.getAllGames()
    val favoriteGames: Flow<List<GameEntity>> = gameDao.getFavoriteGames()

    fun searchGames(query: String): Flow<List<GameEntity>> {
        return gameDao.searchGames(query)
    }

    fun getGameByPath(folderPath: String): Flow<GameEntity?> {
        return gameDao.getGameByPath(folderPath)
    }

    suspend fun getGameByPathOnce(folderPath: String): GameEntity? {
        return gameDao.getGameByPathOnce(folderPath)
    }

    suspend fun insertGame(game: GameEntity): Long {
        return gameDao.insertGame(game)
    }

    suspend fun updateGame(game: GameEntity) {
        gameDao.updateGame(game)
    }

    suspend fun deleteGame(game: GameEntity) {
        gameDao.deleteGame(game)
    }
}
