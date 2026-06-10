package com.example.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.GameAudioEngine
import com.example.data.database.GameDatabase
import com.example.data.database.GameProgress
import com.example.data.database.GameSettings
import com.example.data.repository.GameRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*

class GameViewModel(
    application: Application,
    private val repository: GameRepository
) : AndroidViewModel(application) {

    // Audio Engine
    val audioEngine = GameAudioEngine()

    // Navigation and screen route
    enum class Screen {
        MENU,
        LEVEL_SELECTOR,
        SETTINGS,
        PLAYING,
        GAME_OVER,
        LEVEL_WIN,
        VICTORY_SCREEN // Final success
    }

    private val _currentScreen = MutableStateFlow(Screen.MENU)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Game progress and settings from Room
    val settingsState = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameSettings()
    )

    val progressState = repository.allProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active gameplay variables
    private val _activeLevelId = MutableStateFlow(1)
    val activeLevelId: StateFlow<Int> = _activeLevelId.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _momsState = MutableStateFlow<List<MomState>>(emptyList())
    val momsState: StateFlow<List<MomState>> = _momsState.asStateFlow()

    private val _keySpotsState = MutableStateFlow<List<KeySpotState>>(emptyList())
    val keySpotsState: StateFlow<List<KeySpotState>> = _keySpotsState.asStateFlow()

    private val _activeLevel = MutableStateFlow<LevelData?>(null)
    val activeLevel: StateFlow<LevelData?> = _activeLevel.asStateFlow()

    // Game states
    private val _timeSeconds = MutableStateFlow(0f)
    val timeSeconds: StateFlow<Float> = _timeSeconds.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _activeSearchSpot = MutableStateFlow<KeySpotState?>(null)
    val activeSearchSpot: StateFlow<KeySpotState?> = _activeSearchSpot.asStateFlow()

    // Interactive 3D Perspective controls (rotation in degrees and tilt zoom parameters)
    private val _cameraRotation = MutableStateFlow(-30f) // default isometric -30deg angled perspective
    val cameraRotation: StateFlow<Float> = _cameraRotation.asStateFlow()

    private val _cameraTilt = MutableStateFlow(0f) // default tilt pitch degrees (looking straight ahead in first person!)
    val cameraTilt: StateFlow<Float> = _cameraTilt.asStateFlow()

    private val _isChasing = MutableStateFlow(false)
    val isChasing: StateFlow<Boolean> = _isChasing.asStateFlow()

    private val _keyFoundMessage = MutableStateFlow<String?>(null)
    val keyFoundMessage: StateFlow<String?> = _keyFoundMessage.asStateFlow()

    fun onCameraRotateDrag(dx: Float, dy: Float) {
        val player = _playerState.value
        val sensYaw = 0.005f
        val sensPitch = 0.4f // Pitch in degrees
        
        var newHeading = player.headingAngle + dx * sensYaw
        while (newHeading < -PI) newHeading += (2 * PI).toFloat()
        while (newHeading > PI) newHeading -= (2 * PI).toFloat()
        
        val newTilt = (_cameraTilt.value - dy * sensPitch).coerceIn(-40f, 40f)
        
        _playerState.value = player.copy(headingAngle = newHeading)
        _cameraTilt.value = newTilt
    }

    fun triggerNotification(msg: String) {
        viewModelScope.launch {
            _keyFoundMessage.value = msg
            delay(3500)
            _keyFoundMessage.value = null
        }
    }

    fun toggleHidingInCabinet(cabinetId: String, hide: Boolean) {
        val player = _playerState.value
        val level = _activeLevel.value ?: return
        if (hide) {
            // Find closet position
            val spot = level.keySpots.find { it.id == cabinetId }
            val proposedPos = if (spot != null) {
                Point2D(spot.gridX + 0.5f, spot.gridY + 0.5f)
            } else {
                val dec = level.decors.find { it.id == cabinetId }
                if (dec != null) Point2D(dec.x + dec.width / 2f, dec.y + dec.height / 2f) else player.gridPos
            }
            _playerState.value = player.copy(
                isHiding = true,
                hidingInId = cabinetId,
                gridPos = proposedPos
            )
            audioEngine.playSfx(GameAudioEngine.SfxType.CROUCH)
            _isSearching.value = false
            _activeSearchSpot.value = null
        } else {
            _playerState.value = player.copy(
                isHiding = false,
                hidingInId = null
            )
            audioEngine.playSfx(GameAudioEngine.SfxType.CROUCH)
        }
    }

    fun rotateCameraLeft() {
        _cameraRotation.value = (_cameraRotation.value - 15f + 360f) % 360f
    }

    fun rotateCameraRight() {
        _cameraRotation.value = (_cameraRotation.value + 15f) % 360f
    }

    fun tiltCameraUp() {
        _cameraTilt.value = (_cameraTilt.value + 5f).coerceIn(-40f, 40f)
    }

    fun tiltCameraDown() {
        _cameraTilt.value = (_cameraTilt.value - 5f).coerceIn(-40f, 40f)
    }

    // Joystick movement inputs
    var joystickX = 0f
    var joystickY = 0f

    // Running game loop status
    private var gameLoopJob: Job? = null
    @Volatile
    private var isPlaying = false

    init {
        // Initialize level progresses in DB if empty
        viewModelScope.launch {
            repository.initializeDefaultLevels(3)
            // Sync volumes with audio engine
            settingsState.collect { settings ->
                audioEngine.musicVolume = settings.musicVolume
                audioEngine.sfxVolume = settings.sfxVolume
            }
        }
        audioEngine.startMusic()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        if (screen == Screen.PLAYING) {
            startGameLoop()
        } else {
            stopGameLoop()
        }

        // Handle music loops
        if (screen == Screen.MENU || screen == Screen.SETTINGS || screen == Screen.LEVEL_SELECTOR) {
            audioEngine.startMusic()
        }
    }

    fun selectLevelAndPlay(levelId: Int) {
        _activeLevelId.value = levelId
        loadLevel(levelId)
        navigateTo(Screen.PLAYING)
    }

    private fun loadLevel(levelId: Int) {
        val level = LevelsFactory.getLevel(levelId)
        _activeLevel.value = level
        _playerState.value = PlayerState(gridPos = level.startPlayerPos, headingAngle = 0f)
        _momsState.value = level.moms.map { it.copy() }
        _keySpotsState.value = level.keySpots.map { it.copy() }
        _timeSeconds.value = 0f
        _isSearching.value = false
        _activeSearchSpot.value = null
        joystickX = 0f
        joystickY = 0f
        _cameraRotation.value = 0f
        _cameraTilt.value = 0f
        _isChasing.value = false
        _keyFoundMessage.value = null
    }

    // Controls input callbacks
    fun updateJoystick(x: Float, y: Float) {
        joystickX = x
        joystickY = y
    }

    fun triggerJump() {
        val player = _playerState.value
        if (!player.isJumping) {
            _playerState.value = player.copy(isJumping = true, jumpProgress = 0f)
            audioEngine.playSfx(GameAudioEngine.SfxType.JUMP)
        }
    }

    fun toggleCrouch() {
        val player = _playerState.value
        val newCrouch = !player.isCrouched
        _playerState.value = player.copy(isCrouched = newCrouch)
        audioEngine.playSfx(GameAudioEngine.SfxType.CROUCH)
    }

    fun startSearchingSpot(spot: KeySpotState) {
        _isSearching.value = true
        _activeSearchSpot.value = spot
    }

    fun cancelSearchingSpot() {
        _isSearching.value = false
        _activeSearchSpot.value = null
    }

    // Settings adjustments
    fun updateMusicVolume(vol: Float) {
        viewModelScope.launch {
            val currentSettings = settingsState.value
            val newSettings = currentSettings.copy(musicVolume = vol)
            repository.saveSettings(newSettings)
        }
    }

    fun updateSfxVolume(vol: Float) {
        viewModelScope.launch {
            val currentSettings = settingsState.value
            val newSettings = currentSettings.copy(sfxVolume = vol)
            repository.saveSettings(newSettings)
            audioEngine.playSfx(GameAudioEngine.SfxType.KEY_PICKUP) // Preview notification SFX
        }
    }

    // MAIN GAME LOOP (approx 60 Hz)
    private fun startGameLoop() {
        if (isPlaying) return
        isPlaying = true
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastTime = System.currentTimeMillis()
            while (isPlaying && isActive) {
                val now = System.currentTimeMillis()
                val delta = (now - lastTime) / 1000f
                lastTime = now

                updateGamePhysics(delta)

                delay(16) // ~60fps ticks
            }
        }
    }

    private fun stopGameLoop() {
        isPlaying = false
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private suspend fun updateGamePhysics(delta: Float) {
        val level = _activeLevel.value ?: return
        val player = _playerState.value
        val moms = _momsState.value
        val spots = _keySpotsState.value

        // 1. Advance timer
        _timeSeconds.value += delta

        // 2. Handle Searching Cabinet Progress
        val activeSpot = _activeSearchSpot.value
        if (_isSearching.value && activeSpot != null && !player.isHiding) {
            val updatedSpots = spots.map { s ->
                if (s.id == activeSpot.id) {
                    val newProg = (s.searchProgress + delta * 0.8f).coerceIn(0f, 1f)
                    s.copy(searchProgress = newProg)
                } else s
            }
            _keySpotsState.value = updatedSpots

            val match = updatedSpots.find { it.id == activeSpot.id }
            if (match != null && match.searchProgress >= 1f) {
                // Done searching drawer!
                _isSearching.value = false
                _activeSearchSpot.value = null

                val finalSpots = updatedSpots.map { s ->
                    if (s.id == activeSpot.id) {
                        s.copy(isSearched = true)
                    } else s
                }
                _keySpotsState.value = finalSpots

                if (match.containsKey) {
                    _playerState.value = player.copy(keysCollected = player.keysCollected + 1)
                    audioEngine.playSfx(GameAudioEngine.SfxType.KEY_PICKUP)
                    triggerNotification("BẠN ĐÃ TÌM THẤY CHÌA KHÓA! (${_playerState.value.keysCollected}/${level.totalKeysRequired})")
                } else {
                    audioEngine.playSfx(GameAudioEngine.SfxType.UNLOCK) // Empty click click
                    triggerNotification("NGĂN KÉO RỖNG! Không có chìa khóa.")
                }
            }
            // Cannot walk while actively searching shelves
            return
        }

        // 3. Move Player (First Person Relative)
        var resolvedPos = player.gridPos
        if (!player.isHiding) {
            var speedMod = 2.4f // units per second basic
            if (player.isCrouched) speedMod = 1.1f

            // In first person, we move relative to heading direction!
            val cosHead = cos(player.headingAngle)
            val sinHead = sin(player.headingAngle)

            // joystickY: -1 is forward, 1 is backward
            // joystickX: -1 is strafe left, 1 is strafe right
            val dx = (cosHead * (-joystickY) - sinHead * joystickX) * speedMod * delta
            val dy = (sinHead * (-joystickY) + cosHead * joystickX) * speedMod * delta

            var proposedX = player.gridPos.x + dx
            var proposedY = player.gridPos.y + dy

            proposedX = proposedX.coerceIn(0.5f, level.widthCount - 0.5f)
            proposedY = proposedY.coerceIn(0.5f, level.heightCount - 0.5f)

            resolvedPos = resolveWallCollision(Point2D(proposedX, proposedY), level)
        }

        // Update player jump parabolic height
        var isJumping = player.isJumping
        var jumpProg = player.jumpProgress
        if (isJumping) {
            jumpProg += delta * 2.5f // Jump takes about 0.4s
            if (jumpProg >= 1f) {
                jumpProg = 0f
                isJumping = false
            }
        }

        val updatedPlayer = player.copy(
            gridPos = resolvedPos,
            isJumping = isJumping,
            jumpProgress = jumpProg
        )
        _playerState.value = updatedPlayer

        // 4. Update the Mom(s) AI including pursuit/chase!
        var anyMomChasing = false
        val updatedMoms = moms.map { mom ->
            var pos = mom.gridPos
            var currentIdx = mom.currentWaypointIndex
            var waitTime = mom.waitTimerSeconds
            var heading = mom.headingAngle
            var lookAngle = mom.searchLookAngle
            var activePath = mom.pathNodes.toMutableList()
            var chasing = mom.isChasing
            var lastKnown = mom.lastKnownPlayerPos
            var searchT = mom.chaseSearchTimer

            // Can Mom spot the player now?
            val isPlayerHiding = updatedPlayer.isHiding
            val distToP = pos.distanceTo(updatedPlayer.gridPos)
            var canSeeNow = false

            if (!isPlayerHiding && distToP <= mom.visionRange) {
                val angleToP = atan2(updatedPlayer.gridPos.y - pos.y, updatedPlayer.gridPos.x - pos.x)
                var angleDiff = angleToP - heading
                while (angleDiff < -PI) angleDiff += (2 * PI).toFloat()
                while (angleDiff > PI) angleDiff -= (2 * PI).toFloat()

                val viewHalfAngle = (mom.visionAngleDegree * PI / 180f) / 2f
                if (abs(angleDiff) <= viewHalfAngle) {
                    // Check if vision line is blocked by any walls
                    val pathIsBlocked = isRayBlockedByWalls(pos, updatedPlayer.gridPos, level)
                    if (!pathIsBlocked) {
                        canSeeNow = true
                    }
                }
            }

            if (canSeeNow) {
                // Sighted! Trigger chase!
                if (!chasing) {
                    chasing = true
                    audioEngine.playSfx(GameAudioEngine.SfxType.TRIGGER_ALERT) // Scary alarm alert sfx
                    triggerNotification("MẸ ĐÃ PHÁT HIỆN! Chạy mau!")
                }
                lastKnown = updatedPlayer.gridPos
                searchT = 3.5f // Reset pursuit look around timer
                activePath.clear() // Recalculate immediate pursuit path
            }

            if (chasing) {
                anyMomChasing = true
                val dest = lastKnown ?: updatedPlayer.gridPos
                val distToDest = pos.distanceTo(dest)

                if (distToDest > 0.25f) {
                    // Recalculate path to player/dest if path got empty
                    if (activePath.isEmpty()) {
                        activePath = findPathForMom(pos, dest, level).toMutableList()
                    }
                    if (activePath.isNotEmpty()) {
                        val nextStep = activePath.first()
                        val distToStep = pos.distanceTo(nextStep)
                        if (distToStep < 0.15f) {
                            activePath.removeAt(0)
                        } else {
                            val angle = atan2(nextStep.y - pos.y, nextStep.x - pos.x)
                            heading = angle
                            lookAngle = 0f
                            // Mom chases significantly faster!
                            val chaseSpeed = mom.speed * 1.55f
                            val stepDist = chaseSpeed * delta
                            pos = Point2D(
                                pos.x + cos(angle) * stepDist,
                                pos.y + sin(angle) * stepDist
                            )
                        }
                    }
                } else {
                    // Reached the last known position but player is not visible here
                    searchT -= delta
                    if (searchT > 0f) {
                        // Sweep search look in panic
                        lookAngle = sin(_timeSeconds.value * 7f) * 0.8f
                        heading = heading + lookAngle * delta * 5f
                    } else {
                        // Resets to calm patrol
                        chasing = false
                        lastKnown = null
                        activePath.clear()
                    }
                }
            } else {
                // NORMAL PATROL CYCLE
                if (waitTime > 0f) {
                    waitTime -= delta
                    lookAngle = sin(_timeSeconds.value * 4f) * 0.5f
                    heading = atan2(
                        mom.waypoints[currentIdx].y - pos.y,
                        mom.waypoints[currentIdx].x - pos.x
                    ) + lookAngle
                } else {
                    if (activePath.isEmpty()) {
                        activePath = findPathForMom(pos, mom.waypoints[currentIdx], level).toMutableList()
                    }
                    if (activePath.isNotEmpty()) {
                        val dest = activePath.first()
                        val distToDest = pos.distanceTo(dest)
                        if (distToDest < 0.15f) {
                            activePath.removeAt(0)
                            if (activePath.isEmpty()) {
                                waitTime = 1.5f
                                currentIdx = (currentIdx + 1) % mom.waypoints.size
                            }
                        } else {
                            val angle = atan2(dest.y - pos.y, dest.x - pos.x)
                            heading = angle
                            lookAngle = 0f
                            val stepDist = mom.speed * delta
                            pos = Point2D(
                                pos.x + cos(angle) * stepDist,
                                pos.y + sin(angle) * stepDist
                            )
                        }
                    } else {
                        waitTime = 1.5f
                        currentIdx = (currentIdx + 1) % mom.waypoints.size
                    }
                }
            }

            mom.copy(
                gridPos = pos,
                currentWaypointIndex = currentIdx,
                waitTimerSeconds = waitTime,
                headingAngle = heading,
                searchLookAngle = lookAngle,
                pathNodes = activePath,
                isChasing = chasing,
                lastKnownPlayerPos = lastKnown,
                chaseSearchTimer = searchT
            )
        }
        
        _momsState.value = updatedMoms
        _isChasing.value = anyMomChasing

        // 5. Check if Player got caught!
        var caughtByMom = false
        if (!updatedPlayer.isHiding) {
            for (mom in updatedMoms) {
                val dist = mom.gridPos.distanceTo(updatedPlayer.gridPos)
                if (dist < 0.38f) {
                    caughtByMom = true
                    break
                }
            }
        }

        if (caughtByMom) {
            stopGameLoop()
            audioEngine.playSfx(GameAudioEngine.SfxType.CAUGHT)
            withContext(Dispatchers.Main) {
                navigateTo(Screen.GAME_OVER)
            }
            return
        }

        // 6. Check if player has reached exit door with keys to WIN!
        val exitDist = resolvedPos.distanceTo(level.exitDoorPos)
        if (exitDist < 1.0f && player.keysCollected >= level.totalKeysRequired) {
            stopGameLoop()
            audioEngine.playSfx(GameAudioEngine.SfxType.LEVEL_WIN)

            val currentLvlId = _activeLevelId.value
            val timeTaken = _timeSeconds.value
            val scoreGained = maxOf(100, 1000 - (timeTaken * 8).toInt())

            // Save Progress to DB (Auto-Save Progress)
            viewModelScope.launch {
                val existing = repository.getProgressForLevel(currentLvlId)
                val isBest = existing == null || existing.bestTimeSeconds > timeTaken
                val stars = when {
                    timeTaken < 30f -> 3
                    timeTaken < 60f -> 2
                    else -> 1
                }

                repository.saveProgress(
                    GameProgress(
                        levelId = currentLvlId,
                        isCompleted = true,
                        isUnlocked = true,
                        bestTimeSeconds = if (isBest) timeTaken else (existing?.bestTimeSeconds ?: timeTaken),
                        keysCollected = player.keysCollected,
                        score = maxOf(existing?.score ?: 0, scoreGained)
                    )
                )

                // Unlock NEXT level
                if (currentLvlId < 3) {
                    val nextLvlId = currentLvlId + 1
                    val nextExisting = repository.getProgressForLevel(nextLvlId)
                    repository.saveProgress(
                        GameProgress(
                            levelId = nextLvlId,
                            isCompleted = nextExisting?.isCompleted ?: false,
                            isUnlocked = true, // Force unlock next level
                            bestTimeSeconds = nextExisting?.bestTimeSeconds ?: 9999f,
                            score = nextExisting?.score ?: 0
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    if (currentLvlId >= 3) {
                        navigateTo(Screen.VICTORY_SCREEN) // Cleared final game!
                    } else {
                        navigateTo(Screen.LEVEL_WIN)
                    }
                }
            }
        }
    }

    private fun isPlayerPhysicallyHidden(playerPos: Point2D, level: LevelData, isCrouched: Boolean): Boolean {
        // If player is crouched and is very close to a furniture block, they are hiding under it or crouched behind:
        if (isCrouched) {
            for (decor in level.decors) {
                val decorCenter = Point2D(decor.x + decor.width / 2f, decor.y + decor.height / 2f)
                val dist = playerPos.distanceTo(decorCenter)
                // If nearby a hiding furniture type like dining table, television unit, bed, sofa, wardrobe
                val canHideUnder = decor.type == DecorType.DINING_TABLE ||
                        decor.type == DecorType.SOFA ||
                        decor.type == DecorType.KITCHEN_ISLAND ||
                        decor.type == DecorType.BED
                if (canHideUnder && dist <= 1.2f) {
                    return true
                }
            }
        }
        return false
    }

    private fun isRayBlockedByWalls(start: Point2D, end: Point2D, level: LevelData): Boolean {
        // Perform a simple DDA / Raymarching raycast across grid cells to check for Solid Walls (1)
        val x1 = start.x
        val y1 = start.y
        val x2 = end.x
        val y2 = end.y

        val steps = 30
        for (i in 1..steps) {
            val t = i.toFloat() / steps
            val tx = x1 + (x2 - x1) * t
            val ty = y1 + (y2 - y1) * t

            val gx = tx.toInt().coerceIn(0, level.widthCount - 1)
            val gy = ty.toInt().coerceIn(0, level.heightCount - 1)

            if (level.grid[gy][gx] == 1) {
                return true // Vision line intersects wall block
            }
        }
        return false
    }

    private fun findPathForMom(start: Point2D, end: Point2D, level: LevelData): List<Point2D> {
        val grid = level.grid
        val startX = start.x.toInt().coerceIn(0, level.widthCount - 1)
        val startY = start.y.toInt().coerceIn(0, level.heightCount - 1)
        val endX = end.x.toInt().coerceIn(0, level.widthCount - 1)
        val endY = end.y.toInt().coerceIn(0, level.heightCount - 1)

        if (startX == endX && startY == endY) {
            return listOf(end)
        }

        val queue = ArrayDeque<Pair<Pair<Int, Int>, List<Pair<Int, Int>>>>()
        queue.add(Pair(Pair(startX, startY), listOf(Pair(startX, startY))))

        val visited = mutableSetOf<Pair<Int, Int>>()
        visited.add(Pair(startX, startY))

        val dirs = listOf(
            Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0)
        )

        var foundPath: List<Pair<Int, Int>>? = null

        while (queue.isNotEmpty()) {
            val (curr, path) = queue.removeFirst()
            val (cx, cy) = curr

            if (cx == endX && cy == endY) {
                foundPath = path
                break
            }

            for (d in dirs) {
                val nx = cx + d.first
                val ny = cy + d.second

                if (nx in 0 until level.widthCount && ny in 0 until level.heightCount) {
                    if (grid[ny][nx] != 1 && !visited.contains(Pair(nx, ny))) {
                        visited.add(Pair(nx, ny))
                        val newPath = path.toMutableList()
                        newPath.add(Pair(nx, ny))
                        queue.add(Pair(Pair(nx, ny), newPath))
                    }
                }
            }
        }

        if (foundPath != null && foundPath.size > 1) {
            val result = mutableListOf<Point2D>()
            for (i in 1 until foundPath.size) {
                val node = foundPath[i]
                if (i == foundPath.size - 1) {
                    result.add(end) // end at exact floating coordinate
                } else {
                    result.add(Point2D(node.first + 0.5f, node.second + 0.5f))
                }
            }
            return result
        }

        return listOf(end)
    }

    private fun resolveWallCollision(proposed: Point2D, level: LevelData): Point2D {
        var resolved = proposed
        val px = proposed.x
        val py = proposed.y
        val r = 0.35f // player radius

        val minX = (px - r).toInt().coerceIn(0, level.widthCount - 1)
        val maxX = (px + r).toInt().coerceIn(0, level.widthCount - 1)
        val minY = (py - r).toInt().coerceIn(0, level.heightCount - 1)
        val maxY = (py + r).toInt().coerceIn(0, level.heightCount - 1)

        for (gy in minY..maxY) {
            for (gx in minX..maxX) {
                // Grid walls have collision
                val isSolid = level.grid[gy][gx] == 1
                if (isSolid) {
                    val closestX = px.coerceIn(gx.toFloat(), gx + 1.0f)
                    val closestY = py.coerceIn(gy.toFloat(), gy + 1.0f)
                    val dx = px - closestX
                    val dy = py - closestY
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist > 0.0f && dist < r) {
                        val overlap = r - dist
                        val pushX = (dx / dist) * overlap
                        val pushY = (dy / dist) * overlap
                        resolved = Point2D(resolved.x + pushX, resolved.y + pushY)
                    } else if (dist == 0.0f) {
                        val centerX = gx + 0.5f
                        val centerY = gy + 0.5f
                        val pX = px - centerX
                        val pY = py - centerY
                        val pLen = sqrt(pX * pX + pY * pY)
                        if (pLen > 0f) {
                            resolved = Point2D(px + (pX / pLen) * 0.12f, py + (pY / pLen) * 0.12f)
                        }
                    }
                }
            }
        }

        // Also do collision checks with solid non-hiding Furniture types (like dense bookshevles, refrigerators, stoves, wardrobe)
        val solidDecors = level.decors.filter {
            it.type == DecorType.REFRIGERATOR ||
            it.type == DecorType.BOOKSHELF ||
            it.type == DecorType.STOVE ||
            it.type == DecorType.WARDROBE
        }
        for (decor in solidDecors) {
            val left = decor.x
            val top = decor.y
            val right = decor.x + decor.width
            val bottom = decor.y + decor.height

            val closestX = resolved.x.coerceIn(left, right)
            val closestY = resolved.y.coerceIn(top, bottom)

            val dx = resolved.x - closestX
            val dy = resolved.y - closestY
            val dist = sqrt(dx * dx + dy * dy)
            if (dist > 0f && dist < r) {
                val overlap = r - dist
                val pushX = (dx / dist) * overlap
                val pushY = (dy / dist) * overlap
                resolved = Point2D(resolved.x + pushX, resolved.y + pushY)
            }
        }

        return resolved
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}

class GameViewModelFactory(
    private val application: Application,
    private val repository: GameRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
