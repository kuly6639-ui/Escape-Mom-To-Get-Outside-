package com.example.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.R // Thay bằng package chuẩn của dự án của bạn nếu có lỗi import R

@Composable
fun GtaTrollApp() {
    // Quản lý trạng thái màn hình: 0 = Intro Rockstar, 1 = Tải game GTA 6, 2 = Cú lừa
    var currentScreen by remember { mutableStateOf(0) }
    var startLoading by remember { mutableStateOf(false) }
    var isIntroVisible by remember { mutableStateOf(false) }

    // Hiệu ứng chạy thanh tiến trình giả trong 5 giây ở Màn 2
    val animatedProgress by animateFloatAsState(
        targetValue = if (startLoading) 1f else 0f,
        animationSpec = tween(durationMillis = 5000),
        finishedListener = {
            currentScreen = 2 // Chạy xong 100% thì nhảy sang màn Cú lừa
        }
    )

    // Xử lý đếm thời gian cho MÀN 1: INTRO ROCKSTAR GAMES
    LaunchedEffect(key1 = true) {
        delay(500) // Chờ hiệu ứng bắt đầu
        isIntroVisible = true // Hiện logo Rockstar lên mượt mà
        delay(3000) // Giữ màn hình Intro hiển thị trong 3 giây
        isIntroVisible = false // Ẩn logo Rockstar đi (Fade out)
        delay(500) // Chờ ẩn hẳn rồi chuyển màn
        currentScreen = 1 // Chuyển sang màn hình GTA 6 tải game
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Tất cả các màn hình đều dùng nền đen huyền bí
        contentAlignment = Alignment.Center
    ) {
        when (currentScreen) {
            0 -> {
                // ==========================================
                // MÀN HÌNH 1: INTRO ROCKSTAR GAMES
                // ==========================================
                AnimatedVisibility(
                    visible = isIntroVisible,
                    enter = fadeIn(animationSpec = tween(1000)),
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.rockstar_logo),
                            contentDescription = "Rockstar Games Logo",
                            modifier = Modifier.size(180.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "R O C K S T A R   G A M E S",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )
                    }
                }
            }

            1 -> {
                // ==========================================
                // MÀN HÌNH 2: TẢI GAME GTA 6 (THẢ THÍNH)
                // ==========================================
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Gọi hình ảnh Logo GTA 6 thay cho chữ thô sơ
                    Image(
                        painter = painterResource(id = R.drawable.gta_logo),
                        contentDescription = "GTA 6 Logo",
                        modifier = Modifier.size(280.dp).padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "MOBILE EARLY ACCESS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Cyan,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    if (!startLoading) {
                        Text(
                            text = "Dung lượng bản Beta: 2.4 GB\nThiết bị của bạn đủ điều kiện trải nghiệm sớm!",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        
                        Button(
                            onClick = { startLoading = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F))
                        ) {
                            Text(text = "TẢI XUỐNG BETA (MƯỢT 60FPS)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "Đang giải nén bộ cài đặt... ${(animatedProgress * 100).toInt()}%",
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Color(0xFFFF007F),
                            trackColor = Color.DarkGray,
                        )
                        Text(
                            text = "Vui lòng không thoát ứng dụng hoặc tắt màn hình...",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            2 -> {
                // ==========================================
                // MÀN HÌNH 3: CÚ LỪA (TROLL RICKROLL)
                // ==========================================
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "🤣 CÚ LỪA THẾ KỶ! 🤣",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Làm gì có GTA 6 Mobile sớm cho mà chơi!\nLo học và làm việc đi nhé bạn hiền, bớt tin người lại nha!",
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp
                    )
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Button(
                        onClick = { 
                            // Reset lại từ đầu để đem đi troll đứa bạn khác
                            startLoading = false
                            currentScreen = 0 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text(text = "CHỤP MÀN HÌNH ĐI TROLL ĐỨA KHÁC", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
 
