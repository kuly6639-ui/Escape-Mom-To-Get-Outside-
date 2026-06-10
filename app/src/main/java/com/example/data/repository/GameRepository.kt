package com.example.data.repository

import com.example.data.database.GameDao
import com.example.data.database.GameProgress
import com.example.data.database.GameSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val gameDao: GameDao) {

    val allProgress: Flow<List<GameProgress>> = gameDao.getAllProgress()

    val settings: Flow<GameSettings> = gameDao.getSettingsFlow().map { it ?: GameSettings() }

    suspend fun getProgressForLevel(levelId: Int): GameProgress? {
        return gameDao.getProgressForLevel(levelId)
    }

    suspend fun getSettingsDirect(): GameSettings {
        return gameDao.getSettingsDirect() ?: GameSettings()
    }

    suspend fun saveProgress(progress: GameProgress) {
        gameDao.saveProgress(progress)
    }

    suspend fun saveSettings(settings: GameSettings) {
        gameDao.saveSettings(settings)
    }

    suspend fun initializeDefaultLevels(totalLevels: Int) {
        for (i in 1..totalLevels) {
            val existing = gameDao.getProgressForLevel(i)
            if (existing == null) {
                gameDao.saveProgress(
                    GameProgress(
                        levelId = i,
                        isCompleted = false,
                        isUnlocked = i == 1, // Level 1 is unlocked by default
                        bestTimeSeconds = 9999f,
                        keysCollected = 0,
                        score = 0
                    )
                )
            }
        }
    }
}
