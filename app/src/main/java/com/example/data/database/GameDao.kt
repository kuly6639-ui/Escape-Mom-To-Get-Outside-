package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // Game Progress queries
    @Query("SELECT * FROM game_progress ORDER BY levelId ASC")
    fun getAllProgress(): Flow<List<GameProgress>>

    @Query("SELECT * FROM game_progress WHERE levelId = :levelId LIMIT 1")
    suspend fun getProgressForLevel(levelId: Int): GameProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: GameProgress)

    @Query("DELETE FROM game_progress")
    suspend fun clearAllProgress()

    // Settings queries
    @Query("SELECT * FROM game_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<GameSettings?>

    @Query("SELECT * FROM game_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): GameSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: GameSettings)
}
