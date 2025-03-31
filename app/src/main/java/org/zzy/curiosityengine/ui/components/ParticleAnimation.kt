package org.zzy.curiosityengine.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import org.zzy.curiosityengine.ui.theme.Primary
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 粒子动画状态数据类
 */
class ParticleState(
    var x: Float,          // x坐标 (0-1)
    var y: Float,          // y坐标 (0-1)
    val size: Float,       // 粒子大小 (相对于屏幕)
    var speedX: Float,     // x方向速度
    var speedY: Float,     // y方向速度
    val color: Color,      // 粒子颜色
    val alpha: Float,      // 透明度
    var rotation: Float    // 旋转角度
)

/**
 * 粒子动画组件
 * 创建一个充满活力的粒子效果，粒子会随机移动并旋转
 */
@Composable
fun ParticleAnimation(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    baseColor: Color = Primary
) {
    // 创建粒子列表
    val particles = remember {
        List(particleCount) {
            // 随机生成粒子属性
            val hue = (baseColor.hashCode() % 360 + Random.nextFloat() * 60 - 30 + 360) % 360
            val saturation = 0.7f + Random.nextFloat() * 0.3f
            val lightness = 0.6f + Random.nextFloat() * 0.4f
            
            // 从HSL创建颜色
            val color = Color.hsl(hue, saturation, lightness, 1f)
            
            ParticleState(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = 0.005f + Random.nextFloat() * 0.015f,
                speedX = (Random.nextFloat() - 0.5f) * 0.004f,
                speedY = (Random.nextFloat() - 0.5f) * 0.004f,
                color = color,
                alpha = 0.3f + Random.nextFloat() * 0.5f,
                rotation = Random.nextFloat() * 360f
            )
        }
    }
    
    // 动画状态
    var animationState by remember { mutableStateOf(0) }
    
    // 全局旋转角度
    val rotationAngle = remember { Animatable(0f) }
    
    // 启动全局旋转动画
    LaunchedEffect(key1 = true) {
        rotationAngle.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    
    // 更新粒子位置 - 使用系统时间作为基准以获得更平滑的动画效果
    LaunchedEffect(key1 = true) {
        var lastFrameTime = System.currentTimeMillis()
        
        while (true) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = (currentTime - lastFrameTime) / 16f // 归一化到16ms帧率
            lastFrameTime = currentTime
            
            delay(16) // 目标约60fps
            animationState++
            
            // 更新每个粒子的位置 - 使用deltaTime使动画速度与帧率无关
            for (particle in particles) {
                // 更新位置，应用deltaTime使动画速度保持一致
                particle.x += particle.speedX * deltaTime
                particle.y += particle.speedY * deltaTime
                
                // 边界检查，如果超出边界则反弹
                if (particle.x < 0 || particle.x > 1) {
                    particle.speedX *= -1
                    // 确保粒子不会卡在边界外
                    particle.x = particle.x.coerceIn(0f, 1f)
                }
                if (particle.y < 0 || particle.y > 1) {
                    particle.speedY *= -1
                    // 确保粒子不会卡在边界外
                    particle.y = particle.y.coerceIn(0f, 1f)
                }
                
                // 更新旋转 - 应用deltaTime使旋转速度保持一致
                particle.rotation = (particle.rotation + 0.5f * deltaTime) % 360f
            }
        }
    }
    
    // 绘制所有粒子
    Canvas(modifier = modifier.fillMaxSize()) {
        // 获取中心点
        val center = Offset(size.width / 2, size.height / 2)
        
        // 绘制粒子
        for (particle in particles) {
            // 计算粒子位置，使其围绕中心点
            val distance = size.minDimension * 0.4f * (0.5f + particle.y * 0.5f)
            val angle = rotationAngle.value + particle.x * 360f
            val radians = Math.toRadians(angle.toDouble())
            
            val x = center.x + (distance * cos(radians)).toFloat()
            val y = center.y + (distance * sin(radians)).toFloat()
            
            // 绘制粒子
            rotate(particle.rotation, Offset(x, y)) {
                drawCircle(
                    color = particle.color.copy(alpha = particle.alpha),
                    radius = size.minDimension * particle.size,
                    center = Offset(x, y)
                )
            }
        }
    }
}