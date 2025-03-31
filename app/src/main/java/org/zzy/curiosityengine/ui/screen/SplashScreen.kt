package org.zzy.curiosityengine.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.zzy.curiosityengine.R
import org.zzy.curiosityengine.ui.components.ParticleAnimation
import org.zzy.curiosityengine.ui.theme.Primary
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 启动页面
 * @param onTimeout 超时回调，导航到主页
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // 启动页显示时间
    val splashDuration = 3000L
    
    // 背景色
    val backgroundColor = Primary
    
    // 使用remember避免重组时重新创建
    val animationStartTime = remember { System.currentTimeMillis() }
    
    // 启动超时导航 - 使用animationStartTime确保计时准确
    LaunchedEffect(animationStartTime) {
        delay(splashDuration)
        onTimeout()
    }
    
    // 主容器
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // 粒子动画背景 - 减少粒子数量以提高性能
        ParticleAnimation(
            particleCount = 40, // 减少粒子数量以提高性能
            baseColor = Primary
        )
        
        // 气泡动画
        BubbleAnimation()
        
        // 中心内容
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            LogoAnimation()
            
            // 应用名称
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            // 副标题
            Text(
                text = stringResource(id = R.string.splash_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Logo动画
 */
@Composable
fun LogoAnimation() {
    // 创建无限循环的动画
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
    
    // 旋转动画
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // 缩放动画
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // 绘制Logo
    Box(
        modifier = Modifier
            .size(120.dp * scale),
        contentAlignment = Alignment.Center
    ) {
        // 绘制圆形背景
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 外圆
            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                radius = size.minDimension / 2
            )
            
            // 中圆
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = size.minDimension / 2.5f
            )
            
            // 内圆
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = size.minDimension / 3.5f
            )
            
            // 中心圆
            drawCircle(
                color = Color.White,
                radius = size.minDimension / 6f
            )
            
            // 绘制围绕中心的彩色小圆点
            drawColorDots(rotation)
        }
    }
}

/**
 * 绘制彩色小圆点
 */
private fun DrawScope.drawColorDots(rotation: Float) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = size.minDimension / 3.2f
    val dotRadius = size.minDimension / 25f
    
    // 彩色点的颜色
    val colors = listOf(
        Color(0xFFFF5252), // 红
        Color(0xFFFFB142), // 橙
        Color(0xFFFFEB3B), // 黄
        Color(0xFF4CAF50), // 绿
        Color(0xFF2196F3), // 蓝
        Color(0xFF9C27B0)  // 紫
    )
    
    // 绘制彩色点
    for (i in colors.indices) {
        val angle = Math.toRadians((360f / colors.size * i + rotation).toDouble())
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        
        drawCircle(
            color = colors[i],
            radius = dotRadius,
            center = Offset(x, y)
        )
    }
}

/**
 * 气泡动画
 */
@Composable
fun BubbleAnimation() {
    // 创建20个气泡
    val bubbleCount = 20
    val bubbles = remember {
        List(bubbleCount) {
            BubbleState(
                x = Random.nextFloat(),
                y = 1f + Random.nextFloat() * 0.5f,
                size = 0.03f + Random.nextFloat() * 0.08f,
                speed = 0.0003f + Random.nextFloat() * 0.0007f,
                alpha = 0.1f + Random.nextFloat() * 0.2f
            )
        }
    }
    
    // 气泡动画状态
    var animationState by remember { mutableStateOf(0) }
    
    // 更新气泡位置
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(16) // 约60fps
            animationState++
            
            // 更新每个气泡的位置
            for (i in bubbles.indices) {
                val bubble = bubbles[i]
                bubble.y -= bubble.speed
                
                // 如果气泡飞出屏幕顶部，重新从底部开始
                if (bubble.y < -0.2f) {
                    bubble.y = 1f + Random.nextFloat() * 0.2f
                    bubble.x = Random.nextFloat()
                }
            }
        }
    }
    
    // 绘制所有气泡
    Canvas(modifier = Modifier.fillMaxSize()) {
        for (bubble in bubbles) {
            drawCircle(
                color = Color.White.copy(alpha = bubble.alpha),
                radius = size.minDimension * bubble.size / 2,
                center = Offset(
                    x = size.width * bubble.x,
                    y = size.height * bubble.y
                )
            )
        }
    }
}

/**
 * 气泡状态数据类
 */
class BubbleState(
    var x: Float,      // x坐标 (0-1)
    var y: Float,      // y坐标 (0-1)
    val size: Float,   // 气泡大小 (相对于屏幕)
    val speed: Float,  // 上升速度
    val alpha: Float   // 透明度
)