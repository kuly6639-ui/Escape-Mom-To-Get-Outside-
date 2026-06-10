package com.example.ui.game

object LevelsFactory {

    fun getLevel(levelId: Int): LevelData {
        return when (levelId) {
            1 -> createLevel1()
            2 -> createLevel2()
            3 -> createLevel3()
            else -> createLevel1()
        }
    }

    private fun createLevel1(): LevelData {
        val grid = arrayOf(
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            intArrayOf(1, 0, 0, 0, 0, 0, 1, 0, 0, 1),
            intArrayOf(1, 0, 1, 1, 0, 0, 1, 0, 1, 1),
            intArrayOf(1, 0, 1, 0, 0, 0, 0, 0, 0, 1),
            intArrayOf(1, 0, 1, 0, 1, 1, 1, 1, 0, 1),
            intArrayOf(1, 0, 0, 0, 1, 0, 0, 1, 0, 1),
            intArrayOf(1, 1, 1, 0, 1, 0, 0, 0, 0, 1),
            intArrayOf(1, 0, 0, 0, 0, 0, 1, 1, 0, 1),
            intArrayOf(1, 0, 1, 1, 1, 0, 0, 0, 0, 1),
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 3, 1, 1) // 3 is Locked door at x=7, y=9
        )

        return LevelData(
            levelId = 1,
            title = "Level 1: The Kitchen",
            VietnameseTitle = "Màn 1: Căn bếp bừa bộn",
            widthCount = 10,
            heightCount = 10,
            grid = grid,
            startPlayerPos = Point2D(1.5f, 1.5f),
            moms = listOf(
                MomState(
                    id = "mom_lvl_1",
                    gridPos = Point2D(4.5f, 3.5f),
                    waypoints = listOf(
                        Point2D(4.5f, 3.5f),
                        Point2D(8.5f, 3.5f),
                        Point2D(8.5f, 1.5f),
                        Point2D(1.5f, 7.5f),
                        Point2D(4.5f, 7.5f)
                    ),
                    speed = 1.2f,
                    visionRange = 4.8f,
                    visionAngleDegree = 70f
                )
            ),
            keySpots = listOf(
                KeySpotState("cabinet_1", 8, 7, "Tủ bếp", true),
                KeySpotState("cabinet_2", 1, 5, "Ngăn kéo", false)
            ),
            decors = listOf(
                DecorObject("decor_1", 2.0f, 2.0f, 1.0f, 1.0f, DecorType.STOVE, "Bếp nấu", "#E57373"),
                DecorObject("decor_2", 5.0f, 6.0f, 1.5f, 0.8f, DecorType.KITCHEN_ISLAND, "Bàn đảo", "#A1887F"),
                DecorObject("decor_3", 7.0f, 8.0f, 0.8f, 0.8f, DecorType.REFRIGERATOR, "Tủ lạnh", "#90A4AE")
            ),
            exitDoorPos = Point2D(7.5f, 9.5f),
            totalKeysRequired = 1
        )
    }

    private fun createLevel2(): LevelData {
        val grid = arrayOf(
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            intArrayOf(1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1),
            intArrayOf(1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1),
            intArrayOf(1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1),
            intArrayOf(1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1),
            intArrayOf(1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1),
            intArrayOf(1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 0, 1),
            intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1),
            intArrayOf(1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1),
            intArrayOf(1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1),
            intArrayOf(1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 1),
            intArrayOf(1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1) // Exit at bottom (col 6, row 11)
        )

        return LevelData(
            levelId = 2,
            title = "Level 2: The Living Room",
            VietnameseTitle = "Màn 2: Phòng khách nhộn nhịp",
            widthCount = 12,
            heightCount = 12,
            grid = grid,
            startPlayerPos = Point2D(1.5f, 1.5f),
            moms = listOf(
                MomState(
                    id = "mom_lvl_2",
                    gridPos = Point2D(7.5f, 1.5f),
                    waypoints = listOf(
                        Point2D(7.5f, 1.5f),
                        Point2D(10.5f, 1.5f),
                        Point2D(10.5f, 5.5f),
                        Point2D(4.5f, 9.5f),
                        Point2D(1.5f, 9.5f),
                        Point2D(1.5f, 5.5f)
                    ),
                    speed = 1.5f,
                    visionRange = 5.5f,
                    visionAngleDegree = 75f
                )
            ),
            keySpots = listOf(
                KeySpotState("cabinet_l2_1", 10, 9, "Kệ sách", true),
                KeySpotState("cabinet_l2_2", 1, 7, "Tủ TV", true),
                KeySpotState("cabinet_l2_3", 5, 3, "Ngăn kéo nhỏ", false)
            ),
            decors = listOf(
                DecorObject("decor_l2_1", 2.0f, 5.0f, 1.5f, 0.8f, DecorType.SOFA, "Ghế Sofa lớn", "#4DD0E1"),
                DecorObject("decor_l2_2", 8.0f, 7.0f, 1.0f, 1.0f, DecorType.TV_CABINET, "Kệ tủ tivi", "#795548"),
                DecorObject("decor_l2_3", 10.0f, 3.0f, 0.8f, 0.8f, DecorType.PLANT_POT, "Cây cảnh", "#81C784")
            ),
            exitDoorPos = Point2D(6.5f, 11.5f),
            totalKeysRequired = 2
        )
    }

    private fun createLevel3(): LevelData {
        val grid = arrayOf(
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1),
            intArrayOf(1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1),
            intArrayOf(1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1),
            intArrayOf(1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1),
            intArrayOf(1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1),
            intArrayOf(1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1),
            intArrayOf(1, 0, 0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1),
            intArrayOf(1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1),
            intArrayOf(1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1),
            intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
            intArrayOf(1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1),
            intArrayOf(1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1),
            intArrayOf(1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1) // Exit at bottom (col 6, row 13)
        )

        return LevelData(
            levelId = 3,
            title = "Level 3: The Mansion",
            VietnameseTitle = "Màn 3: Biệt thự trốn tìm",
            widthCount = 14,
            heightCount = 14,
            grid = grid,
            startPlayerPos = Point2D(1.5f, 1.5f),
            moms = listOf(
                // Patrolling the left rooms
                MomState(
                    id = "mom_lvl_3_left",
                    gridPos = Point2D(2.5f, 7.5f),
                    waypoints = listOf(
                        Point2D(2.5f, 7.5f),
                        Point2D(1.5f, 12.5f),
                        Point2D(4.5f, 12.5f),
                        Point2D(5.5f, 10.5f),
                        Point2D(2.5f, 1.5f)
                    ),
                    speed = 1.4f,
                    visionRange = 5.2f,
                    visionAngleDegree = 80f
                ),
                // Patrolling the right corridors
                MomState(
                    id = "mom_lvl_3_right",
                    gridPos = Point2D(12.5f, 1.5f),
                    waypoints = listOf(
                        Point2D(12.5f, 1.5f),
                        Point2D(10.5f, 5.5f),
                        Point2D(12.5f, 9.5f),
                        Point2D(12.5f, 12.5f),
                        Point2D(7.5f, 12.5f),
                        Point2D(7.5f, 10.5f)
                    ),
                    speed = 1.6f,
                    visionRange = 5.8f,
                    visionAngleDegree = 75f
                )
            ),
            keySpots = listOf(
                KeySpotState("cabinet_l3_1", 1, 12, "Tủ đầu giường", true),
                KeySpotState("cabinet_l3_2", 12, 1, "Tủ quần áo lớn", true),
                KeySpotState("cabinet_l3_3", 7, 12, "Tủ trang điểm", true),
                KeySpotState("cabinet_l3_4", 10, 5, "Hộp gỗ cổ", false)
            ),
            decors = listOf(
                DecorObject("decor_l3_1", 2.0f, 10.0f, 1.2f, 1.8f, DecorType.BED, "Giường ngủ", "#BA68C8"),
                DecorObject("decor_l3_2", 8.0f, 2.0f, 1.0f, 1.0f, DecorType.WARDROBE, "Tủ đồ lớn", "#8D6E63"),
                DecorObject("decor_l3_3", 11.0f, 10.0f, 0.8f, 0.8f, DecorType.PLANT_POT, "Chậu tùng cảnh", "#4CAF50")
            ),
            exitDoorPos = Point2D(6.5f, 13.5f),
            totalKeysRequired = 3
        )
    }
}
