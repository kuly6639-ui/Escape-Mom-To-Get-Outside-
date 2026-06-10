package com.example.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.GameAudioEngine
import com.example.data.database.GameProgress
import com.example.data.database.GameSettings
import java.util.*
import kotlin.math.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EscapeMomGameApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val progressList by viewModel.progressState.collectAsStateWithLifecycle()
    val activeLevelId by viewModel.activeLevelId.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF12141C) // Deep dark space background
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                GameViewModel.Screen.MENU -> {
                    MainMenuScreen(
                        viewModel = viewModel,
                        progressList = progressList,
                        onPlayClick = { viewModel.navigateTo(GameViewModel.Screen.LEVEL_SELECTOR) },
                        onSettingsClick = { viewModel.navigateTo(GameViewModel.Screen.SETTINGS) }
                    )
                }
                GameViewModel.Screen.LEVEL_SELECTOR -> {
                    LevelSelectorScreen(
                        viewModel = viewModel,
                        progressList = progressList,
                        onBack = { viewModel.navigateTo(GameViewModel.Screen.MENU) }
                    )
                }
                GameViewModel.Screen.SETTINGS -> {
                    SettingsScreen(
                        settings = settings,
                        onVolumeMusicChange = { viewModel.updateMusicVolume(it) },
                        onVolumeSfxChange = { viewModel.updateSfxVolume(it) },
                        onBack = { viewModel.navigateTo(GameViewModel.Screen.MENU) }
                    )
                }
                GameViewModel.Screen.PLAYING -> {
                    GamePlayScreen(viewModel = viewModel)
                }
                GameViewModel.Screen.GAME_OVER -> {
                    GameOverScreen(
                        viewModel = viewModel,
                        levelId = activeLevelId,
                        onRestart = { viewModel.selectLevelAndPlay(activeLevelId) },
                        onMenu = { viewModel.navigateTo(GameViewModel.Screen.LEVEL_SELECTOR) }
                    )
                }
                GameViewModel.Screen.LEVEL_WIN -> {
                    LevelWinScreen(
                        viewModel = viewModel,
                        levelId = activeLevelId,
                        onNextLevel = {
                            if (activeLevelId < 3) {
                                viewModel.selectLevelAndPlay(activeLevelId + 1)
                            } else {
                                viewModel.navigateTo(GameViewModel.Screen.VICTORY_SCREEN)
                            }
                        },
                        onMenu = { viewModel.navigateTo(GameViewModel.Screen.LEVEL_SELECTOR) }
                    )
                }
                GameViewModel.Screen.VICTORY_SCREEN -> {
                    VictoryScreen(
                        onBack = { viewModel.navigateTo(GameViewModel.Screen.MENU) }
                    )
                }
            }
        }
    }
}

// 1. MAIN MENU SCREEN
@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    progressList: List<GameProgress>,
    onPlayClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background elements
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw subtle glowing background circles
            drawCircle(
                color = Color(0xFFDE3226).copy(alpha = 0.08f),
                radius = size.width * 0.4f,
                center = Offset(size.width * 0.2f, size.height * 0.3f)
            )
            drawCircle(
                color = Color(0xFFFFC107).copy(alpha = 0.05f),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.8f, size.height * 0.7f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 500.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Run",
                tint = Color(0xFFDE3226),
                modifier = Modifier
                    .size(80.dp)
                    .shadow(16.dp, CircleShape)
                    .background(Color(0x33DE3226), CircleShape)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Escape Mom",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp,
                modifier = Modifier.testTag("app_title")
            )
            Text(
                text = "TO GET OUTSIDE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF9A9A),
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Trốn ra ngoài mà không bị mẹ phát hiện! Một tựa game lén lút giả lập 3D thú vị.",
                color = Color(0xFF9EA3B0),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onPlayClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDE3226),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("start_game_button"),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CHƠI LÉN", fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onSettingsClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFF37474F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("settings_button")
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CÀI ĐẶT & ÂM THANH", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Global stats badge
            val completedLevelsCount = progressList.count { it.isCompleted }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2130)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TIẾN ĐỘ CHƠI TỰ ĐỘNG", fontSize = 12.sp, color = Color(0xFF78909C), fontWeight = FontWeight.Bold)
                        Text("Đã vượt qua: $completedLevelsCount / 3 màn", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Icon(
                        imageVector = if (completedLevelsCount == 3) Icons.Default.Star else Icons.Default.Lock,
                        contentDescription = "Status",
                        tint = if (completedLevelsCount == 3) Color(0xFFFFC107) else Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// 2. LEVEL SELECTOR SCREEN
@Composable
fun LevelSelectorScreen(
    viewModel: GameViewModel,
    progressList: List<GameProgress>,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .background(Color(0xFF1E2130), CircleShape)
                .size(48.dp)
                .testTag("back_button")
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 64.dp)
        ) {
            Text(
                text = "Chọn Màn Chơi",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Hệ thống tự động lưu trữ điểm số và tiến độ chơi",
                fontSize = 13.sp,
                color = Color(0xFF78909C),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            val sortedProgress = progressList.sortedBy { it.levelId }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Ensure we have levels even if DB is still syncing
                val currentLevels = if (sortedProgress.isEmpty()) {
                    listOf(
                        GameProgress(1, isCompleted = false, isUnlocked = true),
                        GameProgress(2, isCompleted = false, isUnlocked = false),
                        GameProgress(3, isCompleted = false, isUnlocked = false)
                    )
                } else {
                    sortedProgress
                }

                items(currentLevels) { item ->
                    val levelData = LevelsFactory.getLevel(item.levelId)
                    LevelItemRow(
                        progress = item,
                        levelData = levelData,
                        onPlay = { viewModel.selectLevelAndPlay(item.levelId) }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelItemRow(
    progress: GameProgress,
    levelData: LevelData,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("level_card_${levelData.levelId}"),
        colors = CardDefaults.cardColors(
            containerColor = if (progress.isUnlocked) Color(0xFF1E2130) else Color(0xFF151722)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.5.dp,
            if (progress.isUnlocked) Color(0xFF37474F) else Color(0xFF1F2430)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level ID / Status Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (progress.isUnlocked) Color(0xFFDE3226).copy(alpha = 0.15f) else Color(0xFF263238),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (progress.isUnlocked) {
                    Text(
                        "${levelData.levelId}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFDE3226)
                    )
                } else {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFF78909C), modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Stats and Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = levelData.VietnameseTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (progress.isUnlocked) Color.White else Color(0xFF78909C)
                )

                if (progress.isUnlocked) {
                    Text(
                        text = "Số lượng chìa khóa: ${levelData.totalKeysRequired} 🔑",
                        fontSize = 12.sp,
                        color = Color(0xFF9EA3B0),
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    if (progress.isCompleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Best Time", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Kỷ lục: %.1fs".format(progress.bestTimeSeconds),
                                fontSize = 12.sp,
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "Chưa hoàn thành",
                            fontSize = 12.sp,
                            color = Color(0xFFFFB74D),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Chinh phục màn trước để mở khóa!",
                        fontSize = 12.sp,
                        color = Color(0xFF78909C),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Play Trigger Button
            if (progress.isUnlocked) {
                IconButton(
                    onClick = onPlay,
                    modifier = Modifier
                        .background(Color(0xFFDE3226), CircleShape)
                        .size(44.dp)
                        .testTag("play_button_${levelData.levelId}")
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Start Level", tint = Color.White)
                }
            }
        }
    }
}

// 3. SETTINGS & CUSTOM SYNTH SOUNDS SCREEN
@Composable
fun SettingsScreen(
    settings: GameSettings,
    onVolumeMusicChange: (Float) -> Unit,
    onVolumeSfxChange: (Float) -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .background(Color(0xFF1E2130), CircleShape)
                .size(48.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 64.dp)
                .widthIn(max = 500.dp)
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = "Tùy Chọn Âm Thanh",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Chế độ âm thanh được tổng hợp tín hiệu trực tiếp",
                fontSize = 12.sp,
                color = Color(0xFF78909C),
                modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
            )

            // Dynamic volume sliders
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2130)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Nhạc Nền (Stealth Ambient)",
                        fontSize = 15.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Music", tint = Color(0xFFDE3226))
                        Spacer(modifier = Modifier.width(12.dp))
                        Slider(
                            value = settings.musicVolume,
                            onValueChange = onVolumeMusicChange,
                            modifier = Modifier.weight(1f).testTag("music_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFDE3226),
                                activeTrackColor = Color(0xFFDE3226),
                                inactiveTrackColor = Color(0xFF263238)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${(settings.musicVolume * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Hiệu Ứng SFX (Phản Hồi Thao Tác)",
                        fontSize = 15.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "SFX", tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(12.dp))
                        Slider(
                            value = settings.sfxVolume,
                            onValueChange = onVolumeSfxChange,
                            modifier = Modifier.weight(1f).testTag("sfx_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF4CAF50),
                                activeTrackColor = Color(0xFF4CAF50),
                                inactiveTrackColor = Color(0xFF263238)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${(settings.sfxVolume * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(42.dp)
                        )
                    }
                }
            }

            // Simple Instructions Page
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F111D)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color(0xFF1E2130))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "📖 HƯỚNG DẪN CHƠI LÉN",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB74D)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val guides = listOf(
                        "1. **Điều khiển:** Sử dụng cần Joystick ảo góc trái màn hình để di chuyển mượt mà.",
                        "2. **Cúi người (Crouch):** Hạ thấp người giúp di chuyển êm, lách khỏi tầm mắt mẹ dẫu đang đi siêu gần gầm bàn/Sofa.",
                        "3. **Tầm nhìn mẹ (Vision Cone):** Hãy chú ý nón đỏ mờ quét rọi xung quanh mẹ. Nếu sa chân vào nón đỏ, bạn bị tóm ngay lập tức!",
                        "4. **Tìm Chìa Khóa:** Tiếp cận các hộc tủ, dùng phím **Tương tác** giữ sục sạo hòng nhặt chìa khóa ẩn giấu.",
                        "5. **Mở Phá Cửa:** Luôn nhớ mở cửa ra ngoài ở mép dưới cùng để thắng cuộc."
                    )

                    guides.forEach { text ->
                        Text(
                            text = text.replace("**", ""), // clean bold markup
                            fontSize = 13.sp,
                            color = Color(0xFF9EA3B0),
                            modifier = Modifier.padding(vertical = 4.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// 4. MAIN GAMEPLAY SHEET (HUD & Pseudo-3D Canvas Render)
@Composable
fun GamePlayScreen(viewModel: GameViewModel) {
    val level by viewModel.activeLevel.collectAsStateWithLifecycle()
    val player by viewModel.playerState.collectAsStateWithLifecycle()
    val moms by viewModel.momsState.collectAsStateWithLifecycle()
    val spots by viewModel.keySpotsState.collectAsStateWithLifecycle()
    val elapsed by viewModel.timeSeconds.collectAsStateWithLifecycle()
    val searching by viewModel.isSearching.collectAsStateWithLifecycle()
    val activeSpot by viewModel.activeSearchSpot.collectAsStateWithLifecycle()

    val cameraRotation by viewModel.cameraRotation.collectAsStateWithLifecycle()
    val cameraTilt by viewModel.cameraTilt.collectAsStateWithLifecycle()

    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    if (level == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFDE3226))
        }
        return
    }

    val activeLevelData = level!!

    var isDraggingRightHalf by remember { mutableStateOf(false) }

    val isChasing by viewModel.isChasing.collectAsStateWithLifecycle()
    val keyFoundMessage by viewModel.keyFoundMessage.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (offset.x > size.width / 2f) {
                            isDraggingRightHalf = true
                        }
                    },
                    onDragEnd = {
                        isDraggingRightHalf = false
                    },
                    onDragCancel = {
                        isDraggingRightHalf = false
                    },
                    onDrag = { change, dragAmount ->
                        if (isDraggingRightHalf) {
                            change.consume()
                            viewModel.onCameraRotateDrag(dragAmount.x, dragAmount.y)
                        }
                    }
                )
            }
    ) {
        // True First Person 3D viewport canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_arena_canvas")
        ) {
            canvasSize = size

            val width = size.width
            val height = size.height
            val halfW = width / 2f
            val halfH = height / 2.1f // center of view matches standard horizon line

            val px = player.gridPos.x
            val py = player.gridPos.y
            
            // Eye Height (pz): standing is 0.45, crouched is 0.23, with realistic walking bobbing
            val baseEyeH = if (player.isCrouched) 0.22f else 0.45f
            // Bobbing only when moving
            val isMoving = viewModel.joystickX != 0f || viewModel.joystickY != 0f
            val bobVal = if (isMoving && !player.isHiding) {
                abs(sin(elapsed * 9f)) * 0.04f
            } else 0f
            val pz = baseEyeH + bobVal

            val facingAngle = player.headingAngle
            val pitchRad = (cameraTilt * PI / 180f).toFloat()
            val fovRad = (82f * PI / 180f).toFloat() // Comfortable immersive FOV
            val fovScale = 1.0f / tan(fovRad / 2.0f)

            // Local Point3D data helper
            class LocalPoint3D(val x: Float, val y: Float, val z: Float)

            fun projectToCameraSpace(gx: Float, gy: Float, gz: Float): LocalPoint3D {
                val dx = gx - px
                val dy = gy - py
                val dz = gz - pz

                val depth = dx * cos(facingAngle) + dy * sin(facingAngle)
                val lateral = dx * (-sin(facingAngle)) + dy * cos(facingAngle)
                val heightOffset = dz

                val cosPitch = cos(pitchRad)
                val sinPitch = sin(pitchRad)

                val camDepth = depth * cosPitch + heightOffset * sinPitch
                val camHeight = -depth * sinPitch + heightOffset * cosPitch
                val camLateral = lateral

                return LocalPoint3D(camLateral, camDepth, camHeight)
            }

            fun clipFaceAndProject(v: List<LocalPoint3D>): List<Offset> {
                val nearPlane = 0.06f
                val output = mutableListOf<LocalPoint3D>()
                for (i in v.indices) {
                    val p1 = v[i]
                    val p2 = v[(i + 1) % v.size]

                    if (p1.y >= nearPlane) {
                        output.add(p1)
                    }

                    if ((p1.y >= nearPlane && p2.y < nearPlane) || (p1.y < nearPlane && p2.y >= nearPlane)) {
                        val t = (nearPlane - p1.y) / (p2.y - p1.y)
                        val ix = p1.x + (p2.x - p1.x) * t
                        val iy = nearPlane
                        val iz = p1.z + (p2.z - p1.z) * t
                        output.add(LocalPoint3D(ix, iy, iz))
                    }
                }

                return output.map { pt ->
                    val sx = halfW + (pt.x / pt.y) * halfW * fovScale
                    val sy = halfH - (pt.z / pt.y) * halfH * fovScale
                    Offset(sx, sy)
                }
            }

            // 1. CLEAR & CEILING DRAW (Deep twilight / room night ceiling)
            val ceilingBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFF04060E), Color(0xFF111425))
            )
            drawRect(brush = ceilingBrush)

            // Dynamic Horizon Line
            val horizonY = halfH + tan(pitchRad) * halfH * fovScale

            // 2. FLOOR DRAW (Cozy wood / plush tile floors depending on Level ID)
            val floorColor = when (activeLevelData.levelId) {
                1 -> Color(0xFF3E2723) // Rich Dark Kitchen Mahogany
                2 -> Color(0xFF0D1B2A) // Cozy Living Room Indigo Carpet
                else -> Color(0xFF1B1B1B) // Slate Black Marble Mansion
            }
            drawRect(
                color = floorColor,
                topLeft = Offset(0f, horizonY.coerceIn(0f, height)),
                size = Size(width, (height - horizonY).coerceAtLeast(0f))
            )

            // Draw grid perspective guidelines on floor and ceiling for deep spatial reference
            val gridLineColor = Color.White.copy(alpha = 0.06f)
            val gridLineStroke = 1.5f

            // floor horizontal & vertical guidelines
            for (gx in 0..activeLevelData.widthCount) {
                val linePoints = mutableListOf<LocalPoint3D>()
                for (gy in 0..activeLevelData.heightCount) {
                    linePoints.add(projectToCameraSpace(gx.toFloat(), gy.toFloat(), 0f))
                }
                val scrPoints = clipFaceAndProject(linePoints)
                if (scrPoints.size >= 2) {
                    for (k in 0 until scrPoints.size - 1) {
                        drawLine(gridLineColor, scrPoints[k], scrPoints[k + 1], gridLineStroke)
                    }
                }
            }
            for (gy in 0..activeLevelData.heightCount) {
                val linePoints = mutableListOf<LocalPoint3D>()
                for (gx in 0..activeLevelData.widthCount) {
                    linePoints.add(projectToCameraSpace(gx.toFloat(), gy.toFloat(), 0f))
                }
                val scrPoints = clipFaceAndProject(linePoints)
                if (scrPoints.size >= 2) {
                    for (k in 0 until scrPoints.size - 1) {
                        drawLine(gridLineColor, scrPoints[k], scrPoints[k + 1], gridLineStroke)
                    }
                }
            }

            // ceiling gridlines (slightly higher, z=1.8f)
            val ceilGridColor = Color(0xFFDE3226).copy(alpha = 0.04f)
            for (gx in 0..activeLevelData.widthCount) {
                val linePoints = mutableListOf<LocalPoint3D>()
                for (gy in 0..activeLevelData.heightCount) {
                    linePoints.add(projectToCameraSpace(gx.toFloat(), gy.toFloat(), 1.8f))
                }
                val scrPoints = clipFaceAndProject(linePoints)
                if (scrPoints.size >= 2) {
                    for (k in 0 until scrPoints.size - 1) {
                        drawLine(ceilGridColor, scrPoints[k], scrPoints[k + 1], gridLineStroke)
                    }
                }
            }

            // 3D DEPTH RENDERING LIST (Painter's Algorithm across all standing solids)
            class RenderObject3D(val type: Int, val gx: Float, val gy: Float, val data: Any?, val depth: Float)

            val renderables = mutableListOf<RenderObject3D>()

            // 1. Gather all Walls
            for (gy in 0 until activeLevelData.heightCount) {
                for (gx in 0 until activeLevelData.widthCount) {
                    if (activeLevelData.grid[gy][gx] == 1) {
                        val cam = projectToCameraSpace(gx + 0.5f, gy + 0.5f, 0.7f)
                        if (cam.y > -0.5f) {
                            renderables.add(RenderObject3D(1, gx.toFloat(), gy.toFloat(), null, cam.y))
                        }
                    }
                }
            }

            // 2. Gather all Decors
            activeLevelData.decors.forEach { decor ->
                val cam = projectToCameraSpace(decor.x + decor.width / 2f, decor.y + decor.height / 2f, 0.4f)
                if (cam.y > -0.5f) {
                    renderables.add(RenderObject3D(2, 0f, 0f, decor, cam.y))
                }
            }

            // 3. Gather all Cabinet key spots
            spots.forEach { spot ->
                val cam = projectToCameraSpace(spot.gridX + 0.5f, spot.gridY + 0.5f, 0.3f)
                if (cam.y > -0.5f) {
                    renderables.add(RenderObject3D(3, 0f, 0f, spot, cam.y))
                }
            }

            // 4. Gather Moms
            moms.forEach { mom ->
                val cam = projectToCameraSpace(mom.gridPos.x, mom.gridPos.y, 0.5f)
                if (cam.y > -0.5f) {
                    renderables.add(RenderObject3D(4, 0f, 0f, mom, cam.y))
                }
            }

            // 5. Gather Exit Door gateway location
            val exitCam = projectToCameraSpace(activeLevelData.exitDoorPos.x, activeLevelData.exitDoorPos.y, 0.6f)
            if (exitCam.y > -0.5f) {
                renderables.add(RenderObject3D(5, 0f, 0f, activeLevelData.exitDoorPos, exitCam.y))
            }

            // Sort ALL solids back-to-front (furthest depth distance first, closest last)
            renderables.sortByDescending { it.depth }

            // Draw all solid solids in depth sorted order
            renderables.forEach { renderable ->
                when (renderable.type) {
                    1 -> {
                        val gx = renderable.gx
                        val gy = renderable.gy

                        // Color schemes based on level ID
                        val wallColor = when (activeLevelData.levelId) {
                            1 -> Color(0xFF6D4C41) // Kitchen brick brown
                            2 -> Color(0xFF37474F) // Lounge steel blue
                            else -> Color(0xFF263238) // Gothic marble
                        }
                        val wallSideColor = wallColor.copy(red = (wallColor.red * 0.75f).coerceIn(0f, 1f), green = (wallColor.green * 0.75f).coerceIn(0f, 1f), blue = (wallColor.blue * 0.75f).coerceIn(0f, 1f))
                        val wallTopColor = wallColor.copy(red = (wallColor.red * 1.15f).coerceIn(0f, 1f), green = (wallColor.green * 1.15f).coerceIn(0f, 1f), blue = (wallColor.blue * 1.15f).coerceIn(0f, 1f))

                        val w = 0.98f
                        val h = 0.98f
                        val wallH = 1.7f

                        // Construct side faces of the wall cube
                        val verticesTop = listOf(
                            projectToCameraSpace(gx, gy, wallH),
                            projectToCameraSpace(gx + w, gy, wallH),
                            projectToCameraSpace(gx + w, gy + h, wallH),
                            projectToCameraSpace(gx, gy + h, wallH)
                        )
                        val verticesNorth = listOf(
                            projectToCameraSpace(gx, gy, 0f),
                            projectToCameraSpace(gx + w, gy, 0f),
                            projectToCameraSpace(gx + w, gy, wallH),
                            projectToCameraSpace(gx, gy, wallH)
                        )
                        val verticesSouth = listOf(
                            projectToCameraSpace(gx, gy + h, 0f),
                            projectToCameraSpace(gx + w, gy + h, 0f),
                            projectToCameraSpace(gx + w, gy + h, wallH),
                            projectToCameraSpace(gx, gy + h, wallH)
                        )
                        val verticesWest = listOf(
                            projectToCameraSpace(gx, gy, 0f),
                            projectToCameraSpace(gx, gy + h, 0f),
                            projectToCameraSpace(gx, gy + h, wallH),
                            projectToCameraSpace(gx, gy, wallH)
                        )
                        val verticesEast = listOf(
                            projectToCameraSpace(gx + w, gy, 0f),
                            projectToCameraSpace(gx + w, gy + h, 0f),
                            projectToCameraSpace(gx + w, gy + h, wallH),
                            projectToCameraSpace(gx + w, gy, wallH)
                        )

                        // Relative back-to-front face drawing order within the wall segment
                        val faces = listOf(
                            Pair(verticesTop, wallTopColor),
                            Pair(verticesNorth, wallSideColor),
                            Pair(verticesSouth, wallSideColor),
                            Pair(verticesWest, wallColor),
                            Pair(verticesEast, wallColor)
                        ).map { p ->
                            val avgD = p.first.map { it.y }.average().toFloat()
                            Triple(p.first, p.second, avgD)
                        }.sortedByDescending { it.third }

                        faces.forEach { face ->
                            val pList = clipFaceAndProject(face.first)
                            if (pList.size >= 3) {
                                val path = Path().apply {
                                    moveTo(pList[0].x, pList[0].y)
                                    for (m in 1 until pList.size) {
                                        lineTo(pList[m].x, pList[m].y)
                                    }
                                    close()
                                }
                                drawPath(path, brush = SolidColor(face.second))
                                // Highlight wall seam corner lines
                                drawPath(path, brush = SolidColor(Color.White.copy(alpha = 0.05f)), style = Stroke(1f))
                            }
                        }
                    }
                    2 -> {
                        val decor = renderable.data as com.example.ui.game.DecorObject
                        val col = Color(android.graphics.Color.parseColor(decor.colorHex))
                        val colDark = col.copy(red = col.red * 0.72f, green = col.green * 0.72f, blue = col.blue * 0.72f)
                        val colLight = col.copy(red = (col.red * 1.15f).coerceIn(0f, 1f), green = (col.green * 1.15f).coerceIn(0f, 1f), blue = (col.blue * 1.15f).coerceIn(0f, 1f))

                        val decH = 0.8f
                        val top = listOf(
                            projectToCameraSpace(decor.x, decor.y, decH),
                            projectToCameraSpace(decor.x + decor.width, decor.y, decH),
                            projectToCameraSpace(decor.x + decor.width, decor.y + decor.height, decH),
                            projectToCameraSpace(decor.x, decor.y + decor.height, decH)
                        )
                        val front = listOf(
                            projectToCameraSpace(decor.x, decor.y + decor.height, 0f),
                            projectToCameraSpace(decor.x + decor.width, decor.y + decor.height, 0f),
                            projectToCameraSpace(decor.x + decor.width, decor.y + decor.height, decH),
                            projectToCameraSpace(decor.x, decor.y + decor.height, decH)
                        )
                        val west = listOf(
                            projectToCameraSpace(decor.x, decor.y, 0f),
                            projectToCameraSpace(decor.x, decor.y + decor.height, 0f),
                            projectToCameraSpace(decor.x, decor.y + decor.height, decH),
                            projectToCameraSpace(decor.x, decor.y, decH)
                        )
                        val east = listOf(
                            projectToCameraSpace(decor.x + decor.width, decor.y, 0f),
                            projectToCameraSpace(decor.x + decor.width, decor.y + decor.height, 0f),
                            projectToCameraSpace(decor.x + decor.width, decor.y + decor.height, decH),
                            projectToCameraSpace(decor.x + decor.width, decor.y, decH)
                        )

                        val decFaces = listOf(
                            Pair(top, colLight),
                            Pair(front, col),
                            Pair(west, colDark),
                            Pair(east, colDark)
                        ).map { p ->
                            val avgD = p.first.map { it.y }.average().toFloat()
                            Triple(p.first, p.second, avgD)
                        }.sortedByDescending { it.third }

                        decFaces.forEach { face ->
                            val pList = clipFaceAndProject(face.first)
                            if (pList.size >= 3) {
                                val path = Path().apply {
                                    moveTo(pList[0].x, pList[0].y)
                                    for (m in 1 until pList.size) {
                                        lineTo(pList[m].x, pList[m].y)
                                    }
                                    close()
                                }
                                drawPath(path, brush = SolidColor(face.second))
                                drawPath(path, brush = SolidColor(Color.Black.copy(alpha = 0.15f)), style = Stroke(1.5f))
                            }
                        }
                    }
                    3 -> {
                        val spot = renderable.data as com.example.ui.game.KeySpotState
                        val isSearched = spot.isSearched
                        val baseCol = if (isSearched) Color(0xFF5D4037) else Color(0xFFE65100)
                        val sideCol = Color(0xFF3E2723)
                        val topCol = if (isSearched) Color(0xFF8D6E63) else Color(0xFFFFB74D)

                        val gx = spot.gridX + 0.12f
                        val gy = spot.gridY + 0.12f
                        val w = 0.76f
                        val h = 0.76f
                        val cabH = 0.72f

                        val top = listOf(
                            projectToCameraSpace(gx, gy, cabH),
                            projectToCameraSpace(gx + w, gy, cabH),
                            projectToCameraSpace(gx + w, gy + h, cabH),
                            projectToCameraSpace(gx, gy + h, cabH)
                        )
                        val front = listOf(
                            projectToCameraSpace(gx, gy + h, 0f),
                            projectToCameraSpace(gx + w, gy + h, 0f),
                            projectToCameraSpace(gx + w, gy + h, cabH),
                            projectToCameraSpace(gx, gy + h, cabH)
                        )
                        val west = listOf(
                            projectToCameraSpace(gx, gy, 0f),
                            projectToCameraSpace(gx, gy + h, 0f),
                            projectToCameraSpace(gx, gy + h, cabH),
                            projectToCameraSpace(gx, gy, cabH)
                        )
                        val east = listOf(
                            projectToCameraSpace(gx + w, gy, 0f),
                            projectToCameraSpace(gx + w, gy + h, 0f),
                            projectToCameraSpace(gx + w, gy + h, cabH),
                            projectToCameraSpace(gx + w, gy, cabH)
                        )

                        val spotFaces = listOf(
                            Pair(top, topCol),
                            Pair(front, baseCol),
                            Pair(west, sideCol),
                            Pair(east, sideCol)
                        ).map { p ->
                            val avgD = p.first.map { it.y }.average().toFloat()
                            Triple(p.first, p.second, avgD)
                        }.sortedByDescending { it.third }

                        spotFaces.forEach { face ->
                            val pList = clipFaceAndProject(face.first)
                            if (pList.size >= 3) {
                                val path = Path().apply {
                                    moveTo(pList[0].x, pList[0].y)
                                    for (m in 1 until pList.size) {
                                        lineTo(pList[m].x, pList[m].y)
                                    }
                                    close()
                                }
                                drawPath(path, brush = SolidColor(face.second))
                                drawPath(path, brush = SolidColor(Color.White.copy(alpha = 0.2f)), style = Stroke(1.5f))

                                // If drawing the FRONT face, draw cupboard dresser handles!
                                if (face.first === front) {
                                    // Golden handle lines drawn centered
                                    val leftMid = Offset(
                                        pList[0].x + (pList[3].x - pList[0].x) * 0.5f,
                                        pList[0].y + (pList[3].y - pList[0].y) * 0.5f
                                    )
                                    val rightMid = Offset(
                                        pList[1].x + (pList[2].x - pList[1].x) * 0.5f,
                                        pList[1].y + (pList[2].y - pList[1].y) * 0.5f
                                    )
                                    val handleL = Offset(
                                        leftMid.x + (rightMid.x - leftMid.x) * 0.35f,
                                        leftMid.y + (rightMid.y - leftMid.y) * 0.35f
                                    )
                                    val handleR = Offset(
                                        leftMid.x + (rightMid.x - leftMid.x) * 0.65f,
                                        leftMid.y + (rightMid.y - leftMid.y) * 0.65f
                                    )
                                    drawLine(Color(0xFFFFA000), handleL, handleR, 4f)
                                }
                            }
                        }

                        // Proximity highlighted circle overlay and billboard labeled overlay if near
                        val spotPos = Point2D(spot.gridX + 0.5f, spot.gridY + 0.5f)
                        val spotDist = player.gridPos.distanceTo(spotPos)
                        if (spotDist < 1.15f && !isSearched) {
                            val spotCam = projectToCameraSpace(spot.gridX + 0.5f, spot.gridY + 0.5f, 0.4f)
                            if (spotCam.y > 0.05f) {
                                val sx = halfW + (spotCam.x / spotCam.y) * halfW * fovScale
                                val sy = halfH - (spotCam.z / spotCam.y) * halfH * fovScale
                                drawCircle(
                                    color = Color(0xFFFFB74D).copy(alpha = 0.4f + sin(elapsed * 7f) * 0.15f),
                                    radius = (width / spotCam.y) * 0.045f,
                                    center = Offset(sx, sy),
                                    style = Stroke(2.dp.toPx())
                                )
                            }
                        }
                    }
                    4 -> {
                        val mom = renderable.data as com.example.ui.game.MomState
                        val momCam = projectToCameraSpace(mom.gridPos.x, mom.gridPos.y, 0.5f)

                        if (momCam.y > 0.07f) {
                            // 1. Draw Ground search indicator vision flashlight cone
                            val fovRadM = mom.visionAngleDegree * PI.toFloat() / 180f
                            val startAngle = mom.headingAngle - fovRadM / 2
                            val endAngle = mom.headingAngle + fovRadM / 2
                            val stepCount = 14
                            val conePoints = mutableListOf<LocalPoint3D>()
                            // Start apex at Mom's feet
                            conePoints.add(projectToCameraSpace(mom.gridPos.x, mom.gridPos.y, 0f))
                            for (m in 0..stepCount) {
                                val frac = m.toFloat() / stepCount
                                val rayAngle = startAngle + (endAngle - startAngle) * frac
                                val gx = mom.gridPos.x + cos(rayAngle) * mom.visionRange
                                val gy = mom.gridPos.y + sin(rayAngle) * mom.visionRange
                                conePoints.add(projectToCameraSpace(gx, gy, 0f))
                            }
                            val coneScr = clipFaceAndProject(conePoints)
                            if (coneScr.size >= 3) {
                                val path = Path().apply {
                                    moveTo(coneScr[0].x, coneScr[0].y)
                                    for (k in 1 until coneScr.size) {
                                        lineTo(coneScr[k].x, coneScr[k].y)
                                    }
                                    close()
                                }
                                val beamColor = if (mom.isChasing) Color(0x66E53935) else Color(0x3DFFD54F)
                                drawPath(path, brush = SolidColor(beamColor))
                            }

                            // 2. Draw base shadow circle on floor
                            val mBase = projectToCameraSpace(mom.gridPos.x, mom.gridPos.y, 0f)
                            val mBaseScr = clipFaceAndProject(listOf(mBase))
                            if (mBaseScr.isNotEmpty()) {
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    radius = (width / momCam.y) * 0.043f,
                                    center = mBaseScr[0]
                                )
                                // Red hazard outline under chassis if chasing
                                if (mom.isChasing) {
                                    drawCircle(
                                        color = Color(0xFFE53935).copy(alpha = 0.5f + abs(sin(elapsed * 10f)) * 0.4f),
                                        radius = (width / momCam.y) * (0.045f + sin(elapsed * 8f) * 0.01f),
                                        center = mBaseScr[0],
                                        style = Stroke(2.5.dp.toPx())
                                    )
                                }
                            }

                            // 3. Draw Mom billboard sprite card
                            val mTop = projectToCameraSpace(mom.gridPos.x, mom.gridPos.y, 1.25f)
                            val mTopScr = clipFaceAndProject(listOf(mTop))
                            val mBaseScrPt = mBaseScr.firstOrNull()
                            val mTopScrPt = mTopScr.firstOrNull()

                            if (mBaseScrPt != null && mTopScrPt != null) {
                                val sY = abs(mBaseScrPt.y - mTopScrPt.y)
                                val sX = sY * 0.72f
                                val headY = mTopScrPt.y + sY * 0.15f
                                val headR = sY * 0.18f

                                // Draw Apron mother dress (trapezoid body)
                                val dressPath = Path().apply {
                                    moveTo(mBaseScrPt.x - sX * 0.5f, mBaseScrPt.y)
                                    lineTo(mBaseScrPt.x - sX * 0.28f, headY + headR)
                                    lineTo(mBaseScrPt.x + sX * 0.28f, headY + headR)
                                    lineTo(mBaseScrPt.x + sX * 0.5f, mBaseScrPt.y)
                                    close()
                                }
                                val dressColor = if (mom.isChasing) Color(0xFFB71C1C) else Color(0xFFC62828)
                                drawPath(dressPath, brush = SolidColor(dressColor))

                                // Draw white kitchen apron overlay
                                val apronPath = Path().apply {
                                    moveTo(mBaseScrPt.x - sX * 0.28f, mBaseScrPt.y)
                                    lineTo(mBaseScrPt.x - sX * 0.15f, headY + headR + (mBaseScrPt.y - headY) * 0.2f)
                                    lineTo(mBaseScrPt.x + sX * 0.15f, headY + headR + (mBaseScrPt.y - headY) * 0.2f)
                                    lineTo(mBaseScrPt.x + sX * 0.28f, mBaseScrPt.y)
                                    close()
                                }
                                drawPath(apronPath, brush = SolidColor(Color(0xFFFAFAFA)))

                                // Draw Skin head circle
                                drawCircle(
                                    color = Color(0xFFFFCCBC),
                                    radius = headR,
                                    center = Offset(mBaseScrPt.x, headY)
                                )

                                // Draw Motherly grey-brown hair bun on top of head
                                drawCircle(
                                    color = Color(0xFF5D4037),
                                    radius = headR * 0.44f,
                                    center = Offset(mBaseScrPt.x, headY - headR * 0.9f)
                                )

                                // Draw glasses (spectacles loops)
                                val eyeSep = headR * 0.45f
                                val glassesColor = if (mom.isChasing) Color(0xFFE53935) else Color.Black
                                drawCircle(
                                    color = Color.White,
                                    radius = headR * 0.28f,
                                    center = Offset(mBaseScrPt.x - eyeSep, headY),
                                    style = Stroke(2f)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = headR * 0.28f,
                                    center = Offset(mBaseScrPt.x + eyeSep, headY),
                                    style = Stroke(2f)
                                )
                                // Glasses rims / connectors
                                drawCircle(
                                    color = glassesColor,
                                    radius = headR * 0.3f,
                                    center = Offset(mBaseScrPt.x - eyeSep, headY),
                                    style = Stroke(1.8.dp.toPx())
                                )
                                drawCircle(
                                    color = glassesColor,
                                    radius = headR * 0.3f,
                                    center = Offset(mBaseScrPt.x + eyeSep, headY),
                                    style = Stroke(1.8.dp.toPx())
                                )
                                drawLine(glassesColor, Offset(mBaseScrPt.x - eyeSep + headR * 0.15f, headY), Offset(mBaseScrPt.x + eyeSep - headR * 0.15f, headY), 2.5.dp.toPx())

                                // Beady glow eyes inside glasses loop
                                val eyeGlowColor = if (mom.isChasing) Color.Red else Color.Black
                                drawCircle(eyeGlowColor, radius = headR * 0.1f, center = Offset(mBaseScrPt.x - eyeSep, headY))
                                drawCircle(eyeGlowColor, radius = headR * 0.1f, center = Offset(mBaseScrPt.x + eyeSep, headY))

                                // If chasing, draw angry expression indicators (flashing red sweat lines or exclamation!)
                                if (mom.isChasing) {
                                    // Angry red eyebrow lines above glasses
                                    drawLine(Color.Red, Offset(mBaseScrPt.x - headR * 0.9f, headY - headR * 0.4f), Offset(mBaseScrPt.x - headR * 0.1f, headY - headR * 0.2f), 3f)
                                    drawLine(Color.Red, Offset(mBaseScrPt.x + headR * 0.9f, headY - headR * 0.4f), Offset(mBaseScrPt.x + headR * 0.1f, headY - headR * 0.2f), 3f)

                                    // Spooky alert bubble above Bun
                                    val alertCenter = Offset(mBaseScrPt.x, headY - headR * 2.1f)
                                    val alertR = headR * 0.48f
                                    drawCircle(Color(0xFFE53935), radius = alertR, center = alertCenter)
                                    // draw Exclamation point!
                                    drawLine(Color.White, Offset(alertCenter.x, alertCenter.y - alertR * 0.4f), Offset(alertCenter.x, alertCenter.y + alertR * 0.05f), 3f)
                                    drawCircle(Color.White, radius = 2f, center = Offset(alertCenter.x, alertCenter.y + alertR * 0.45f))
                                }
                            }
                        }
                    }
                    5 -> {
                        val exPos = renderable.data as Point2D
                        val ex = exPos.x
                        val ey = exPos.y

                        val gateW = 0.9f
                        val gateH = 1.4f

                        // Draw a glowing emerald green exit frame
                        val verticesFrame = listOf(
                            projectToCameraSpace(ex - gateW / 2, ey, 0f),
                            projectToCameraSpace(ex + gateW / 2, ey, 0f),
                            projectToCameraSpace(ex + gateW / 2, ey, gateH),
                            projectToCameraSpace(ex - gateW / 2, ey, gateH)
                        )
                        val frameScr = clipFaceAndProject(verticesFrame)
                        if (frameScr.size >= 4) {
                            val path = Path().apply {
                                moveTo(frameScr[0].x, frameScr[0].y)
                                for (k in 1 until frameScr.size) {
                                    lineTo(frameScr[k].x, frameScr[k].y)
                                }
                                close()
                            }
                            // Glow fill
                            drawPath(path, brush = SolidColor(Color(0x334CAF50)))
                            // Emerald border
                            drawPath(path, brush = SolidColor(Color(0xFF81C784)), style = Stroke(4.dp.toPx()))

                            // Draw "THOÁT / EXIT" sign board on top of door in 3D
                            val boardTop = Offset(
                                frameScr[3].x + (frameScr[2].x - frameScr[3].x) * 0.5f,
                                frameScr[3].y + (frameScr[2].y - frameScr[3].y) * 0.5f
                            )
                            val textR = (width / renderable.depth) * 0.1f
                            drawCircle(
                                color = Color(0xD91B5E20),
                                radius = textR * 0.4f,
                                center = Offset(boardTop.x, boardTop.y - textR * 0.12f)
                            )
                            // Sign border
                            drawCircle(
                                color = Color(0xFF4CAF50),
                                radius = textR * 0.41f,
                                center = Offset(boardTop.x, boardTop.y - textR * 0.12f),
                                style = Stroke(1.5f)
                            )
                        }
                    }
                }
            }

            // 4. DRAW BOY PLAYER HANDS HUD (First-person hands overlay!)
            // Reaches from bottom left/right into screen and bobs nicely
            if (!player.isHiding) {
                val handTime = elapsed * 8f
                // Lat/Vert displacement bob from running
                val handBobX = if (isMoving) sin(handTime) * 14.dp.toPx() else 0f
                val handBobY = if (isMoving) abs(cos(handTime)) * 10.dp.toPx() else 0f

                // Left Boy Hand (Blue sleeve + peach skin fist)
                val leftSleevePath = Path().apply {
                    moveTo(width * 0.15f + handBobX, height)
                    lineTo(width * 0.26f + handBobX, height * 0.81f + handBobY)
                    lineTo(width * 0.36f + handBobX, height * 0.86f + handBobY)
                    lineTo(width * 0.28f + handBobX, height)
                    close()
                }
                drawPath(leftSleevePath, brush = SolidColor(Color(0xFF0288D1))) // boy shirt blue
                drawCircle(
                    color = Color(0xFFFFAB91), // peach skin
                    radius = 24.dp.toPx(),
                    center = Offset(width * 0.31f + handBobX, height * 0.83f + handBobY)
                )

                // Right Boy Hand
                val rightSleevePath = Path().apply {
                    moveTo(width * 0.85f - handBobX, height)
                    lineTo(width * 0.74f - handBobX, height * 0.81f + handBobY)
                    lineTo(width * 0.64f - handBobX, height * 0.86f + handBobY)
                    lineTo(width * 0.72f - handBobX, height)
                    close()
                }
                drawPath(rightSleevePath, brush = SolidColor(Color(0xFF0288D1)))
                drawCircle(
                    color = Color(0xFFFFAB91),
                    radius = 24.dp.toPx(),
                    center = Offset(width * 0.69f - handBobX, height * 0.83f + handBobY)
                )
            }
        }

        // 5. SCREEN FLASH ALARM (CHASE THRILL ALERT RED OVERLAY SCREEN VIGNETTE)
        if (isChasing) {
            val chaseAlpha by rememberInfiniteTransition(label = "").animateFloat(
                initialValue = 0.1f,
                targetValue = 0.42f,
                animationSpec = infiniteRepeatable(
                    animation = tween(420, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = ""
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(BorderStroke(12.dp, Color(0xFFDE3226).copy(alpha = chaseAlpha + 0.3f)))
                    .background(Color(0xFFDE3226).copy(alpha = chaseAlpha))
            )
        }

        // 6. DETECTED BIG CHASE ALARM BANNER
        if (isChasing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 115.dp)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDE3226)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MẸ ĐÃ PHÁT HIỆN!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }

        // 7. KEY COLLECED NOTICE BANNER
        if (keyFoundMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 170.dp)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB74D)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Key",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = keyFoundMessage!!,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // 8. HIDING IN CABINET FULL INTERIOR SCREEN OVERLAY
        if (player.isHiding) {
            // Dark cabin grid mask
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.78f))
                    .border(BorderStroke(24.dp, Color(0xFF1A120B))),
                contentAlignment = Alignment.Center
            ) {
                // Slat Blinds Shadows Looking Out!
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(6) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .background(Color.Black.copy(alpha = 0.88f))
                        )
                    }
                }

                // Centered instructions HUD
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(Color(0xFF2C1A11), RoundedCornerShape(14.dp))
                        .border(1.5.dp, Color(0xFFFFA000), RoundedCornerShape(14.dp))
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                        .widthIn(max = 420.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Hiding",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "BẠN ĐANG TRỐN TRONG TỦ...  🤫",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mẹ không thấy bạn ở đây. Hãy chờ mẹ đi khuất rồi mới bấm THOÁT TỦ để hành động tiếp!",
                        fontSize = 11.sp,
                        color = Color(0xFFB0BEC5),
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 9. COZY BOY STATUS HUD PROFILE PHOTO IN TOP-LEFT
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 14.dp, start = 72.dp)
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xE61E2130))
                .border(2.dp, if (isChasing) Color(0xFFDE3226) else Color(0xFF29B6F6), CircleShape)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Emoji changes in real time based on boy states!
                    val faceEmoji = when {
                        viewModel.currentScreen.value == GameViewModel.Screen.GAME_OVER -> "😭"
                        isChasing -> "😱"
                        player.isHiding -> "🤫"
                        player.isCrouched -> "😏"
                        else -> "👦"
                    }
                    Text(
                        text = faceEmoji,
                        fontSize = 26.sp
                    )
                }
            }
        }

        // 5. INTERACTION & TOUCH HUD CONTROLLERS SHEET
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // BACK BUTTON TO PAUSE/PAUSE DIALOG
            IconButton(
                onClick = { viewModel.navigateTo(GameViewModel.Screen.LEVEL_SELECTOR) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .background(Color(0xE61E2130), CircleShape)
                    .size(44.dp)
                    .testTag("exit_playing_button")
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Exit to Menu", tint = Color.White)
            }

            // LEVEL TITLE AND TIMERS
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .background(Color(0xD90F111D), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF37474F), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = activeLevelData.VietnameseTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Thời gian: %.1fs".format(elapsed),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD54F)
                )
            }

            // KEYS COUNTER & STATUS BAR
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xE61E2130)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF37474F)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Keys", tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${player.keysCollected} / ${activeLevelData.totalKeysRequired}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            // MINIMAP EMBED (Overlay dashboard top-right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 56.dp)
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC070914))
                    .border(2.dp, Color(0xFFDE3226).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .testTag("minimap")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val mw = size.width
                    val mh = size.height

                    val mColSize = mw / activeLevelData.widthCount
                    val mRowSize = mh / activeLevelData.heightCount

                    // Draw grid layout boundaries
                    for (gy in 0 until activeLevelData.heightCount) {
                        for (gx in 0 until activeLevelData.widthCount) {
                            val tile = activeLevelData.grid[gy][gx]
                            val mapColor = when (tile) {
                                1 -> Color(0xFF546E7A).copy(alpha = 0.5f) // Wall
                                3 -> Color(0xFF4CAF50).copy(alpha = 0.5f) // Exit
                                else -> Color.Transparent
                            }
                            if (mapColor != Color.Transparent) {
                                drawRect(
                                    color = mapColor,
                                    topLeft = Offset(gx * mColSize, gy * mRowSize),
                                    size = Size(mColSize, mRowSize)
                                )
                            }
                        }
                    }

                    // Draw unsearched drawer spots
                    spots.forEach { s ->
                        if (!s.isSearched) {
                            drawCircle(
                                color = Color(0xFFFFC107),
                                radius = 2.5f,
                                center = Offset((s.gridX + 0.5f) * mColSize, (s.gridY + 0.5f) * mRowSize)
                            )
                        }
                    }

                    // Draw Mom dots
                    moms.forEach { m ->
                        drawCircle(
                            color = Color(0xFFE53935),
                            radius = 3.5f,
                            center = Offset(m.gridPos.x * mColSize, m.gridPos.y * mRowSize)
                        )
                    }

                    // Draw player dot
                    drawCircle(
                        color = Color(0xFF29B6F6),
                        radius = 4f,
                        center = Offset(player.gridPos.x * mColSize, player.gridPos.y * mRowSize)
                    )
                }
            }

            // DYNAMIC CAMERA CONTROLS COLUMN (Aligned to middle right edge)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GÓC NHÌN 3D",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f)
                )

                // Rotate Left
                FloatingActionButton(
                    onClick = { viewModel.rotateCameraLeft() },
                    containerColor = Color(0xE61E2130),
                    contentColor = Color.White,
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Xoay Trái",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Rotate Right
                FloatingActionButton(
                    onClick = { viewModel.rotateCameraRight() },
                    containerColor = Color(0xE61E2130),
                    contentColor = Color.White,
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Xoay Phải",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Pitch Up
                FloatingActionButton(
                    onClick = { viewModel.tiltCameraUp() },
                    containerColor = Color(0xE61E2130),
                    contentColor = Color.White,
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Nâng Góc",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Pitch Down
                FloatingActionButton(
                    onClick = { viewModel.tiltCameraDown() },
                    containerColor = Color(0xE61E2130),
                    contentColor = Color.White,
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Hạ Góc",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // VIRTUAL ANALOG JOYSTICK (Lower left quadrant)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 24.dp, start = 12.dp)
                    .size(140.dp)
                    .background(Color(0x33FFFFFF), CircleShape)
                    .border(2.dp, Color(0xFF37474F), CircleShape)
                    .testTag("movement_joystick")
            ) {
                var dragOffset by remember { mutableStateOf(Offset.Zero) }
                val maxRadius = with(density) { 54.dp.toPx() }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    dragOffset = Offset.Zero
                                    viewModel.updateJoystick(0f, 0f)
                                },
                                onDragCancel = {
                                    dragOffset = Offset.Zero
                                    viewModel.updateJoystick(0f, 0f)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val newOffset = dragOffset + dragAmount
                                    val distance = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                                    dragOffset = if (distance <= maxRadius) {
                                        newOffset
                                    } else {
                                        Offset(
                                            (newOffset.x / distance) * maxRadius,
                                            (newOffset.y / distance) * maxRadius
                                        )
                                    }

                                    // Pass normalized joystick coefficients
                                    val jx = dragOffset.x / maxRadius
                                    val jy = dragOffset.y / maxRadius
                                    viewModel.updateJoystick(jx, jy)
                                }
                            )
                        }
                )

                // Dragging Knob
                val knobOffsetDpX = with(density) { (dragOffset.x).toDp() }
                val knobOffsetDpY = with(density) { (dragOffset.y).toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = knobOffsetDpX, y = knobOffsetDpY)
                        .size(54.dp)
                        .align(Alignment.Center)
                        .shadow(4.dp, CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFDE3226), Color(0xFFB71C1C))
                            ),
                            CircleShape
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run Knob",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp).align(Alignment.Center)
                    )
                }
            }

            // CONTROLS TRIGGERS: CROUCH & JUMP & INTERACT ACTIONS (Lower-right quadrant)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // CROUCH posture button
                IconButton(
                    onClick = { if (!player.isHiding) viewModel.toggleCrouch() },
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(6.dp, CircleShape)
                        .background(
                            if (player.isCrouched) Color(0xFF29B6F6) else Color(0xE61E2130),
                            CircleShape
                        )
                        .border(
                            1.5.dp,
                            if (player.isCrouched) Color.White else Color(0x6678909C),
                            CircleShape
                        )
                        .testTag("crouch_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Crouch",
                        tint = if (player.isCrouched) Color.White else Color(0xFF90A4AE),
                        modifier = Modifier.size(26.dp)
                    )
                }

                // JUMP springboard button
                IconButton(
                    onClick = { if (!player.isHiding) viewModel.triggerJump() },
                    enabled = !player.isJumping && !player.isHiding,
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(6.dp, CircleShape)
                        .background(Color(0xE61E2130), CircleShape)
                        .border(1.5.dp, Color(0x6678909C), CircleShape)
                        .testTag("jump_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Jump",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // HIDING AND SEARCHING DECORTATIONS ACTIONS
                val nearbySpot = spots.find { s ->
                    val sPos = Point2D(s.gridX + 0.5f, s.gridY + 0.5f)
                    player.gridPos.distanceTo(sPos) < 1.15f
                }
                
                val nearbyDecorCabinet = activeLevelData.decors.find { d ->
                    val dPos = Point2D(d.x + d.width / 2f, d.y + d.height / 2f)
                    player.gridPos.distanceTo(dPos) < 1.15f
                }

                val targetCabinetId = nearbySpot?.id ?: nearbyDecorCabinet?.id

                if (player.isHiding) {
                    // EXIT TỦ BUTTON
                    Button(
                        onClick = {
                            val activeCabinetId = player.hidingInId
                            if (activeCabinetId != null) {
                                viewModel.toggleHidingInCabinet(activeCabinetId, false)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDE3226)),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(8.dp, CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Thoát tủ", tint = Color.White, modifier = Modifier.size(26.dp))
                            Text("THOÁT TỦ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else if (targetCabinetId != null) {
                    // VÀO TRỐN TỦ BUTTON
                    Button(
                        onClick = {
                            viewModel.toggleHidingInCabinet(targetCabinetId, true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(8.dp, CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Home, contentDescription = "Vài Trốn", tint = Color.White, modifier = Modifier.size(26.dp))
                            Text("VÀO TRỐN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // If it is also a searchable spot, show drawer key search button beside it!
                    if (nearbySpot != null && !nearbySpot.isSearched) {
                        Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
                            Button(
                                onClick = {
                                    if (!searching) {
                                        viewModel.startSearchingSpot(nearbySpot)
                                    } else {
                                        viewModel.cancelSearchingSpot()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (searching) Color(0xFFC62828) else Color(0xFFFFA000)
                                ),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(72.dp)
                                    .shadow(8.dp, CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                                    .testTag("interact_button"),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (searching) Icons.Default.Close else Icons.Default.Search,
                                        contentDescription = "Search Drawer",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Text(
                                        text = if (searching) "HỦY" else "TÌM KHÓA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            if (searching && activeSpot?.id == nearbySpot.id) {
                                CircularProgressIndicator(
                                    progress = { nearbySpot.searchProgress },
                                    color = Color.White,
                                    strokeWidth = 5.dp,
                                    modifier = Modifier.size(72.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. GAME OVER POPUP
@Composable
fun GameOverScreen(
    viewModel: GameViewModel,
    levelId: Int,
    onRestart: () -> Unit,
    onMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6070914)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .widthIn(max = 420.dp)
                .background(Color(0xFF1E2130), RoundedCornerShape(24.dp))
                .border(2.dp, Color(0xFFDE3226), RoundedCornerShape(24.dp))
                .padding(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Caught",
                tint = Color(0xFFDE3226),
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BỊ MẸ TÓM RỒI!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Choose a random funny mother quote in Vietnamese
            val quotes = remember {
                listOf(
                    "Mẹ mắng: 'Bát đũa đã rửa chưa mà đòi đi đá bóng hử?!'",
                    "Mẹ lôi tai: 'Trưa nắng chang chang không ngủ mà lẻn đi đâu?!'",
                    "Mẹ quét chổi: 'Bài tập Tết đã làm hết chưa mà lủi ra ngõ?!'",
                    "Mẹ liếc sắc lẹm: 'Vào nhà khoanh tay xin lỗi ngay cho mẹ!'"
                )
            }
            val quote = remember { quotes[Random().nextInt(quotes.size)] }

            Text(
                text = quote,
                fontSize = 15.sp,
                color = Color(0xFFEF9A9A),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDE3226)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("retry_level_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                Spacer(modifier = Modifier.width(8.dp))
                Text("CHƠI LẠI MÀN NÀY", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onMenu,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Color(0xFF37474F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("QUAY VỀ BẢNG MÀN CHƠI", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 7. LEVEL COMPLETED CELEBRATION SHEET
@Composable
fun LevelWinScreen(
    viewModel: GameViewModel,
    levelId: Int,
    onNextLevel: () -> Unit,
    onMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6070914)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .widthIn(max = 420.dp)
                .background(Color(0xFF1E2130), RoundedCornerShape(24.dp))
                .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(24.dp))
                .padding(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Success",
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TRỐN THOÁT THÀNH CÔNG!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Bạn đã lẻn ra ngõ chơi thả diều an toàn!",
                fontSize = 14.sp,
                color = Color(0xFF81C784),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Star rewards
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                repeat(3) { index ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Button(
                onClick = onNextLevel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("next_level_button")
            ) {
                Text(
                    text = if (levelId < 3) "MÀN TIẾP THEO" else "BÁO LẬP KỶ LỤC",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onMenu,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Color(0xFF37474F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("MÀN CHƠI KHÁC", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 8. FINAL GAME VICTORY SCREEN
@Composable
fun VictoryScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F111D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 480.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Victory Medal",
                tint = Color(0xFFFFA000),
                modifier = Modifier
                    .size(100.dp)
                    .shadow(16.dp, CircleShape)
                    .background(Color(0x1AFFA000), CircleShape)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "THẦN TRỘM LÉN LÚT!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "BẠN ĐÃ KHÁM PHÁ HẾT BIỆT THỰ",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB74D),
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Text(
                text = "Xuất sắc! Bạn vượt qua cả 3 thử thách dồn dập, định hướng bằng bản đồ con cực nhạy bén, hoàn thành sục sạo các ngóc ngách kịch tính và trốn ra ngoài rực rỡ!",
                color = Color(0xFF9EA3B0),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDE3226)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(Icons.Default.Home, contentDescription = "Menu")
                Spacer(modifier = Modifier.width(8.dp))
                Text("QUAY VỀ THƯC ĐƠN CHÍNH", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// DRAW HELPER ROUTINES (Volumetric projections)
// ==========================================

private fun DrawScope.drawVisionCone(
    centerX: Float,
    centerY: Float,
    angleRad: Float,
    fovDeg: Float,
    rangePx: Float,
    density: androidx.compose.ui.unit.Density
) {
    val fovRad = fovDeg * PI.toFloat() / 180f
    val startAngle = angleRad - fovRad / 2
    val endAngle = angleRad + fovRad / 2

    // Build the triangle sector polygon
    val path = Path()
    path.moveTo(centerX, centerY)

    val stepCount = 18
    for (i in 0..stepCount) {
        val t = i.toFloat() / stepCount
        val rayAngle = startAngle + (endAngle - startAngle) * t
        val rx = centerX + cos(rayAngle) * rangePx
        val ry = centerY + sin(rayAngle) * rangePx
        path.lineTo(rx, ry)
    }
    path.close()

    // Render transparent red warn zone using a nice radial gradient (shines and fades out)
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xABDE3226), // Strong warning red near source
                Color(0x3BDE3226),
                Color(0x00DE3226)  // fully transparent at bounds
            ),
            center = Offset(centerX, centerY),
            radius = rangePx
        )
    )
}

private fun DrawScope.draw3DBox(
    baseX: Float,
    baseY: Float,
    w: Float,
    h: Float,
    extX: Float,
    extY: Float,
    topColor: Color,
    sideColor: Color,
    frontColor: Color
) {
    // Oblique Parallel projection box rendering
    // Base footprint vertices:
    // (baseX, baseY), (baseX + w, baseY), (baseX + w, baseY + h), (baseX, baseY + h)
    // Extruded Top footprint vertices (offset by depth parameters):
    val tx = baseX + extX
    val ty = baseY + extY

    // 1. Draw solid front face
    drawRect(
        color = frontColor,
        topLeft = Offset(baseX, baseY),
        size = Size(w, h)
    )

    // 2. Left side shadowed face (drawn only if extrusion is leftwards)
    if (extX < 0) {
        val sidePath = Path().apply {
            moveTo(baseX, baseY)
            lineTo(tx, ty)
            lineTo(tx, ty + h)
            lineTo(baseX, baseY + h)
            close()
        }
        drawPath(sidePath, brush = SolidColor(sideColor))
    }

    // 3. Top side face
    val topPath = Path().apply {
        moveTo(baseX, baseY)
        lineTo(baseX + w, baseY)
        lineTo(tx + w, ty)
        lineTo(tx, ty)
        close()
    }
    drawPath(topPath, brush = SolidColor(topColor))

    // 4. Highlight lines
    drawLine(
        color = Color.White.copy(alpha = 0.22f),
        start = Offset(baseX, baseY),
        end = Offset(baseX + w, baseY),
        strokeWidth = 2.dp.toPx()
    )
    drawLine(
        color = Color.White.copy(alpha = 0.15f),
        start = Offset(baseX, baseY),
        end = Offset(tx, ty),
        strokeWidth = 2.dp.toPx()
    )
}

private fun DrawScope.draw3DPlayerCapsule(
    projX: Float,
    projY: Float,
    r: Float,
    height: Float,
    isCrouched: Boolean,
    headingAngle: Float,
    density: androidx.compose.ui.unit.Density
) {
    // 3D Capsule representation: base circle, side polygon, and top circle crown
    val topY = projY - height

    // Body cylinder sleeve
    val bodyPath = Path().apply {
        moveTo(projX - r, projY)
        lineTo(projX - r, topY)
        lineTo(projX + r, topY)
        lineTo(projX + r, projY)
        close()
    }

    val bodyColor = if (isCrouched) Color(0xFF29B6F6) else Color(0xFF0288D1)
    val topColor = if (isCrouched) Color(0xFF81D4FA) else Color(0xFF29B6F6)

    // Draw cylindrical back shading
    drawPath(bodyPath, brush = SolidColor(bodyColor))

    // Rounded crown top cap
    drawCircle(
        color = topColor,
        radius = r,
        center = Offset(projX, topY)
    )
    drawCircle(
        color = bodyColor,
        radius = r,
        center = Offset(projX, projY)
    )

    // Eyes / Hair indicators to display orientation direction
    val eyeDistance = r * 0.45f
    val lookX = cos(headingAngle) * r * 0.5f
    val lookY = sin(headingAngle) * r * 0.5f

    // Draw simple spectacles/eyes looking towards direction
    drawCircle(
        color = Color.White,
        radius = r * 0.22f,
        center = Offset(projX + lookX - sin(headingAngle) * eyeDistance, topY + lookY + cos(headingAngle) * eyeDistance * 0.2f)
    )
    drawCircle(
        color = Color.White,
        radius = r * 0.22f,
        center = Offset(projX + lookX + sin(headingAngle) * eyeDistance, topY + lookY - cos(headingAngle) * eyeDistance * 0.2f)
    )

    // Small black pupil centers
    drawCircle(
        color = Color.Black,
        radius = r * 0.1f,
        center = Offset(projX + lookX * 1.2f - sin(headingAngle) * eyeDistance, topY + lookY * 1.2f + cos(headingAngle) * eyeDistance * 0.2f)
    )
    drawCircle(
        color = Color.Black,
        radius = r * 0.1f,
        center = Offset(projX + lookX * 1.2f + sin(headingAngle) * eyeDistance, topY + lookY * 1.2f - cos(headingAngle) * eyeDistance * 0.2f)
    )
}

private fun DrawScope.draw3DMomPatroller(
    projX: Float,
    projY: Float,
    r: Float,
    height: Float,
    headingAngle: Float,
    elapsed: Float,
    density: androidx.compose.ui.unit.Density
) {
    val topY = projY - height

    // Mother's colorful clothing - Red/White checkered Cooking Apron body cylinders
    val bodyPath = Path().apply {
        moveTo(projX - r, projY)
        lineTo(projX - r * 0.8f, topY)
        lineTo(projX + r * 0.8f, topY)
        lineTo(projX + r, projY)
        close()
    }

    // Colors representing apron and dress
    val dressColor = Color(0xFFC62828) // Crimson dress
    val apronColor = Color(0xFFF5F5F5) // White apron sleeve

    drawPath(bodyPath, brush = SolidColor(dressColor))

    // Draw white apron panel centered
    val apronPath = Path().apply {
        moveTo(projX - r * 0.4f, projY)
        lineTo(projX - r * 0.3f, topY + height * 0.35f)
        lineTo(projX + r * 0.3f, topY + height * 0.35f)
        lineTo(projX + r * 0.4f, projY)
        close()
    }
    drawPath(apronPath, brush = SolidColor(apronColor))

    // Apron strings
    drawLine(
        color = Color.White,
        start = Offset(projX - r * 0.7f, topY + height * 0.4f),
        end = Offset(projX + r * 0.7f, topY + height * 0.4f),
        strokeWidth = 3f
    )

    // Mom's head
    val headRadius = r * 0.58f
    val headCenter = Offset(projX, topY)
    drawCircle(
        color = Color(0xFFFFCCBC), // skin tone
        radius = headRadius,
        center = headCenter
    )

    // Chef hair buns / hair cover
    drawCircle(
        color = Color(0xFF4E342E), // brown dark hair bun
        radius = headRadius * 0.5f,
        center = Offset(projX, topY - headRadius)
    )

    // Suspicious angry Mom glasses/eyebrows
    val lookX = cos(headingAngle) * headRadius * 0.6f
    val lookY = sin(headingAngle) * headRadius * 0.6f
    val eyeSpan = headRadius * 0.35f

    // Draw glasses frames
    val leftEye = Offset(headCenter.x + lookX - sin(headingAngle) * eyeSpan, headCenter.y + lookY)
    val rightEye = Offset(headCenter.x + lookX + sin(headingAngle) * eyeSpan, headCenter.y + lookY)

    drawCircle(color = Color(0xFF795548), radius = headRadius * 0.3f, center = leftEye, style = Stroke(2f))
    drawCircle(color = Color(0xFF795548), radius = headRadius * 0.3f, center = rightEye, style = Stroke(2f))

    // Red dots inside eyes for alarming alerts!
    drawCircle(color = Color.Red, radius = 3f, center = leftEye)
    drawCircle(color = Color.Red, radius = 3f, center = rightEye)

    // Angry brow lines
    drawLine(
        color = Color.Black,
        start = Offset(leftEye.x - 5f, leftEye.y - 12f),
        end = Offset(leftEye.x + 8f, leftEye.y - 4f),
        strokeWidth = 4f
    )
    drawLine(
        color = Color.Black,
        start = Offset(rightEye.x + 5f, rightEye.y - 12f),
        end = Offset(rightEye.x - 8f, rightEye.y - 4f),
        strokeWidth = 4f
    )

    // Cooking weapon: Ladle/Spoon swaying (running simple sin/cos waving)
    val ladleSway = sin(elapsed * 10f) * 0.3f
    val ladleAngle = headingAngle + PI.toFloat() / 2f + ladleSway

    val handX = projX + cos(headingAngle + 0.8f) * r * 0.9f
    val handY = topY + height * 0.5f + sin(headingAngle + 0.8f) * r * 0.9f

    // Draw hand skin
    drawCircle(color = Color(0xFFFFCCBC), radius = 6f, center = Offset(handX, handY))

    // Draw wooden ladle handle
    val handleLen = r * 1.5f
    val hEndX = handX + cos(ladleAngle) * handleLen
    val hEndY = handY + sin(ladleAngle) * handleLen
    drawLine(
        color = Color(0xFF8D6E63),
        start = Offset(handX, handY),
        end = Offset(hEndX, hEndY),
        strokeWidth = 4f
    )

    // Spoon cup
    drawCircle(
        color = Color(0xFFB0BEC5),
        radius = 11f,
        center = Offset(hEndX, hEndY)
    )
}

// 3D PERSPECTIVE PROJECTS & DRAWING HELPERS
// =========================================

private sealed class Renderable(val depth: Float) {
    class Wall(val gx: Int, val gy: Int, d: Float) : Renderable(d)
    class Decor(val decor: com.example.ui.game.DecorObject, d: Float) : Renderable(d)
    class Spot(val spot: com.example.ui.game.KeySpotState, d: Float) : Renderable(d)
    class Player(val player: com.example.ui.game.PlayerState, d: Float) : Renderable(d)
    class Mom(val mom: com.example.ui.game.MomState, d: Float) : Renderable(d)
}

private fun project(
    gx: Float,
    gy: Float,
    gz: Float,
    px: Float,
    py: Float,
    tileSize: Float,
    rotationRad: Float,
    tiltRad: Float,
    halfW: Float,
    halfH: Float
): Offset {
    val rx = (gx - px) * tileSize
    val ry = (gy - py) * tileSize
    val rz = gz * tileSize

    val cosY = cos(rotationRad)
    val sinY = sin(rotationRad)

    val rotX = rx * cosY - ry * sinY
    val rotY = rx * sinY + ry * cosY

    val cosP = cos(tiltRad)
    val sinP = sin(tiltRad)

    val projX = rotX
    val projY = rotY * cosP - rz * sinP

    return Offset(projX + halfW, projY + halfH)
}

private fun getDepth(
    gx: Float,
    gy: Float,
    px: Float,
    py: Float,
    rotationRad: Float
): Float {
    val rx = gx - px
    val ry = gy - py
    return rx * sin(rotationRad) + ry * cos(rotationRad)
}

private fun DrawScope.drawPerspective3DBox(
    gx: Float,
    gy: Float,
    w: Float,
    h: Float,
    zHeight: Float,
    px: Float,
    py: Float,
    tileSize: Float,
    rotationRad: Float,
    tiltRad: Float,
    halfW: Float,
    halfH: Float,
    topColor: Color,
    sideColor: Color,
    frontColor: Color
) {
    val p0 = project(gx, gy, 0f, px, py, tileSize, rotationRad, tiltRad, halfW, halfH)
    val p1 = project(gx + w, gy, 0f, px, py, tileSize, rotationRad, tiltRad, halfW, halfH)
    val p2 = project(gx + w, gy + h, 0f, px, py, tileSize, rotationRad, tiltRad, halfW, halfH)
    val p3 = project(gx, gy + h, 0f, px, py, tileSize, rotationRad, tiltRad, halfW, halfH)

    val p4 = project(gx, gy, zHeight, px, py, tileSize, rotationRad, tiltRad, halfW, halfH)
    val p5 = project(gx + w, gy, zHeight, px, py, tileSize, rotationRad, tiltRad, halfW, halfH)
    val p6 = project(gx + w, gy + h, zHeight, px, py, tileSize, rotationRad, tiltRad, halfW, halfH)
    val p7 = project(gx, gy + h, zHeight, px, py, tileSize, rotationRad, tiltRad, halfW, halfH)

    val pathLeft = Path().apply {
        moveTo(p0.x, p0.y)
        lineTo(p3.x, p3.y)
        lineTo(p7.x, p7.y)
        lineTo(p4.x, p4.y)
        close()
    }
    val pathRight = Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p6.x, p6.y)
        lineTo(p5.x, p5.y)
        close()
    }
    val pathFront = Path().apply {
        moveTo(p3.x, p3.y)
        lineTo(p2.x, p2.y)
        lineTo(p6.x, p6.y)
        lineTo(p7.x, p7.y)
        close()
    }
    val pathTop = Path().apply {
        moveTo(p4.x, p4.y)
        lineTo(p5.x, p5.y)
        lineTo(p6.x, p6.y)
        lineTo(p7.x, p7.y)
        close()
    }

    drawPath(pathRight, brush = SolidColor(sideColor))
    drawPath(pathLeft, brush = SolidColor(sideColor))
    drawPath(pathFront, brush = SolidColor(frontColor))
    drawPath(pathTop, brush = SolidColor(topColor))

    drawLine(Color.White.copy(alpha = 0.16f), p4, p5, 1.5f)
    drawLine(Color.White.copy(alpha = 0.16f), p5, p6, 1.5f)
    drawLine(Color.White.copy(alpha = 0.16f), p6, p7, 1.5f)
    drawLine(Color.White.copy(alpha = 0.16f), p7, p4, 1.5f)
}

private fun DrawScope.drawPerspective3DPlayer(
    cx: Float, cy: Float,
    topX: Float, topY: Float,
    r: Float,
    isCrouched: Boolean,
    headingAngle: Float,
    density: androidx.compose.ui.unit.Density
) {
    val bodyPath = Path().apply {
        moveTo(cx - r, cy)
        lineTo(topX - r, topY)
        lineTo(topX + r, topY)
        lineTo(cx + r, cy)
        close()
    }

    val bodyColor = if (isCrouched) Color(0xFF29B6F6) else Color(0xFF0288D1)
    val topColor = if (isCrouched) Color(0xFF81D4FA) else Color(0xFF29B6F6)

    drawPath(bodyPath, brush = SolidColor(bodyColor))
    drawCircle(topColor, radius = r, center = Offset(topX, topY))
    drawCircle(bodyColor, radius = r, center = Offset(cx, cy))

    val eyeDistance = r * 0.45f
    val lookX = cos(headingAngle) * r * 0.5f
    val lookY = sin(headingAngle) * r * 0.5f

    drawCircle(
        color = Color.White,
        radius = r * 0.22f,
        center = Offset(topX + lookX - sin(headingAngle) * eyeDistance, topY + lookY + cos(headingAngle) * eyeDistance * 0.2f)
    )
    drawCircle(
        color = Color.White,
        radius = r * 0.22f,
        center = Offset(topX + lookX + sin(headingAngle) * eyeDistance, topY + lookY - cos(headingAngle) * eyeDistance * 0.2f)
    )
    drawCircle(
        color = Color.Black,
        radius = r * 0.1f,
        center = Offset(topX + lookX * 1.2f - sin(headingAngle) * eyeDistance, topY + lookY * 1.2f + cos(headingAngle) * eyeDistance * 0.2f)
    )
    drawCircle(
        color = Color.Black,
        radius = r * 0.1f,
        center = Offset(topX + lookX * 1.2f + sin(headingAngle) * eyeDistance, topY + lookY * 1.2f - cos(headingAngle) * eyeDistance * 0.2f)
    )
}

private fun DrawScope.drawPerspective3DMom(
    cx: Float, cy: Float,
    topX: Float, topY: Float,
    r: Float,
    height: Float,
    headingAngle: Float,
    elapsed: Float,
    density: androidx.compose.ui.unit.Density
) {
    val bodyPath = Path().apply {
        moveTo(cx - r, cy)
        lineTo(topX - r * 0.8f, topY)
        lineTo(topX + r * 0.8f, topY)
        lineTo(cx + r, cy)
        close()
    }

    val dressColor = Color(0xFFC62828)
    val apronColor = Color(0xFFF5F5F5)

    drawPath(bodyPath, brush = SolidColor(dressColor))

    val apronPath = Path().apply {
        moveTo(cx - r * 0.4f, cy)
        lineTo(topX - r * 0.3f, topY + (cy - topY) * 0.65f)
        lineTo(topX + r * 0.3f, topY + (cy - topY) * 0.65f)
        lineTo(cx + r * 0.4f, cy)
        close()
    }
    drawPath(apronPath, brush = SolidColor(apronColor))

    val headRadius = r * 0.58f
    val headCenter = Offset(topX, topY)
    drawCircle(
        color = Color(0xFFFFCCBC),
        radius = headRadius,
        center = headCenter
    )

    drawCircle(
        color = Color(0xFF4E342E),
        radius = headRadius * 0.45f,
        center = Offset(topX, topY - headRadius * 0.85f)
    )

    val eyeDistance = headRadius * 0.45f
    val lookX = cos(headingAngle) * headRadius * 0.5f
    val lookY = sin(headingAngle) * headRadius * 0.5f

    drawCircle(
        color = Color.White,
        radius = headRadius * 0.25f,
        center = Offset(topX + lookX - sin(headingAngle) * eyeDistance, topY + lookY + cos(headingAngle) * eyeDistance * 0.2f)
    )
    drawCircle(
        color = Color.White,
        radius = headRadius * 0.25f,
        center = Offset(topX + lookX + sin(headingAngle) * eyeDistance, topY + lookY - cos(headingAngle) * eyeDistance * 0.2f)
    )
    drawCircle(
        color = Color(0xFFE53935),
        radius = headRadius * 0.12f,
        center = Offset(topX + lookX * 1.2f - sin(headingAngle) * eyeDistance, topY + lookY * 1.2f + cos(headingAngle) * eyeDistance * 0.2f)
    )
    drawCircle(
        color = Color(0xFFE53935),
        radius = headRadius * 0.12f,
        center = Offset(topX + lookX * 1.2f + sin(headingAngle) * eyeDistance, topY + lookY * 1.2f - cos(headingAngle) * eyeDistance * 0.2f)
    )
}

private fun DrawScope.drawVisionCone3D(
    centerX: Float,
    centerY: Float,
    angleRad: Float,
    fovDeg: Float,
    rangePx: Float,
    px: Float,
    py: Float,
    momGridPos: Point2D,
    tileSize: Float,
    rotRad: Float,
    tiltRad: Float,
    halfW: Float,
    halfH: Float,
    density: androidx.compose.ui.unit.Density
) {
    val fovRad = fovDeg * PI.toFloat() / 180f
    val startAngle = angleRad - fovRad / 2
    val endAngle = angleRad + fovRad / 2

    val path = Path()
    path.moveTo(centerX, centerY)

    val stepCount = 18
    val rangeGrid = rangePx / tileSize
    for (i in 0..stepCount) {
        val t = i.toFloat() / stepCount
        val rayAngle = startAngle + (endAngle - startAngle) * t
        val gx = momGridPos.x + cos(rayAngle) * rangeGrid
        val gy = momGridPos.y + sin(rayAngle) * rangeGrid
        val proj = project(gx, gy, 0f, px, py, tileSize, rotRad, tiltRad, halfW, halfH)
        path.lineTo(proj.x, proj.y)
    }
    path.close()

    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xACDE3226),
                Color(0x3ADE3226),
                Color(0x00DE3226)
            ),
            center = Offset(centerX, centerY),
            radius = rangePx
        )
    )
}
