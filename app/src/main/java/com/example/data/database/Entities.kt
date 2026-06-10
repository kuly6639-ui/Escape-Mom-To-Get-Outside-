package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_progress")
data class GameProgress(
    @PrimaryKey val levelId: Int,
    val isCompleted: Boolean = false,
    val isUnlocked: Boolean = false,
    val bestTimeSeconds: Float = 9999f,
    val keysCollected: Int = 0,
    val score: Int = 0,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_settings")
data class GameSettings(
    @PrimaryKey val id: Int = 1, // Singleton row
    val musicVolume: Float = 0.5f,
    val sfxVolume: Float = 0.7f,
    val doubleTapToCrouch: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true
)
