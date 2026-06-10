package com.example.ui.game

import androidx.compose.ui.geometry.Offset

data class Point2D(val x: Float, val y: Float) {
    operator fun plus(other: Point2D) = Point2D(x + other.x, y + other.y)
    operator fun minus(other: Point2D) = Point2D(x - other.x, y - other.y)
    operator fun times(scale: Float) = Point2D(x * scale, y * scale)
    fun distanceTo(other: Point2D): Float = kotlin.math.sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y))
    fun toOffset() = Offset(x, y)
}

enum class DecorType {
    STOVE,
    DINING_TABLE,
    KITCHEN_ISLAND,
    SOFA,
    TV_CABINET,
    BOOKSHELF,
    REFRIGERATOR,
    PLANT_POT,
    BED,
    WARDROBE
}

data class DecorObject(
    val id: String,
    val x: Float, // Grid coordinates
    val y: Float,
    val width: Float,
    val height: Float,
    val type: DecorType,
    val name: String,
    val colorHex: String
)

data class KeySpotState(
    val id: String,
    val gridX: Int,
    val gridY: Int,
    val label: String,
    val containsKey: Boolean,
    var isSearched: Boolean = false,
    var searchProgress: Float = 0f // 0 to 1
)

data class PlayerState(
    var gridPos: Point2D = Point2D(1.5f, 1.5f),
    var headingAngle: Float = 0f, // in radians
    var isCrouched: Boolean = false,
    var isJumping: Boolean = false,
    var jumpProgress: Float = 0f, // 0 to 1.0 parabolic
    val radius: Float = 0.35f,
    var keysCollected: Int = 0,
    var isHiding: Boolean = false,
    var hidingInId: String? = null
) {
    val zOffset: Float
        get() = if (isJumping) {
            // jump duration curve
            val heightMultiplier = 1.6f
            heightMultiplier * (4f * jumpProgress * (1f - jumpProgress))
        } else {
            0f
        }
}

data class MomState(
    val id: String,
    var gridPos: Point2D,
    var headingAngle: Float = 0f,
    val speed: Float = 1.5f, // units per second
    val visionAngleDegree: Float = 75f,
    val visionRange: Float = 5.2f,
    val waypoints: List<Point2D>,
    var currentWaypointIndex: Int = 0,
    var waitTimerSeconds: Float = 0f,
    var isInvestigating: Boolean = false,
    var searchLookAngle: Float = 0f,
    var pathNodes: List<Point2D> = emptyList(),
    var isChasing: Boolean = false,
    var lastKnownPlayerPos: Point2D? = null,
    var chaseSearchTimer: Float = 0f
)

data class LevelData(
    val levelId: Int,
    val title: String,
    val VietnameseTitle: String,
    val widthCount: Int,
    val heightCount: Int,
    val grid: Array<IntArray>, // 1 = Wall, 0 = Empty, 3 = Locked Door Exit
    val startPlayerPos: Point2D,
    val moms: List<MomState>,
    val keySpots: List<KeySpotState>,
    val decors: List<DecorObject>,
    val exitDoorPos: Point2D,
    val totalKeysRequired: Int
) {
    // Generate valid list of obstacles for raycasting or collision
    fun getSolidCells(): List<Pair<Int, Int>> {
        val list = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until heightCount) {
            for (x in 0 until widthCount) {
                if (grid[y][x] == 1) {
                    list.add(Pair(x, y))
                }
            }
        }
        return list
    }
}
