package org.zzy.curiosityengine.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.zzy.curiosityengine.R
import org.zzy.curiosityengine.data.service.VoiceRecognitionService
import org.zzy.curiosityengine.ui.theme.Primary

/**
 * 语音输入对话框
 * 提供语音输入功能的UI界面，优化响应速度和用户体验
 * @param voiceRecognitionService 语音识别服务
 * @param onTextRecognized 文本识别回调
 * @param onDismiss 对话框关闭回调
 */
@Composable
fun VoiceInputDialog(
    voiceRecognitionService: VoiceRecognitionService,
    onTextRecognized: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // 获取语音识别状态和结果
    val recognitionState by voiceRecognitionService.recognitionState.collectAsState()
    val recognitionResult by voiceRecognitionService.recognitionResult.collectAsState()
    
    // 使用remember记录是否显示错误提示
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // 初始化并启动语音识别
    LaunchedEffect(Unit) {
        try {
            // 先初始化语音识别服务
            voiceRecognitionService.initialize()
            // 然后开始监听
            voiceRecognitionService.startListening()
            showErrorMessage = false
        } catch (e: Exception) {
            errorMessage = e.message ?: "启动语音识别失败"
            showErrorMessage = true
        }
    }
    
    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            voiceRecognitionService.stopListening()
        }
    }
    
    // 当识别状态变化时处理
    LaunchedEffect(recognitionState) {
        when (recognitionState) {
            is VoiceRecognitionService.RecognitionState.Success -> {
                recognitionResult?.let { result ->
                    if (result.isNotEmpty()) {
                        onTextRecognized(result)
                        onDismiss()
                    }
                }
            }
            is VoiceRecognitionService.RecognitionState.Error -> {
                errorMessage = (recognitionState as VoiceRecognitionService.RecognitionState.Error).message
                showErrorMessage = true
            }
            else -> {
                showErrorMessage = false
            }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.voice_input),
                        style = MaterialTheme.typography.titleLarge,
                        color = Primary
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 语音输入动画
                VoiceInputAnimation(recognitionState)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 状态文本 - 优化显示效果和错误处理
                Text(
                    text = when (recognitionState) {
                        is VoiceRecognitionService.RecognitionState.Idle -> stringResource(id = R.string.voice_input_hint)
                        is VoiceRecognitionService.RecognitionState.Listening -> stringResource(id = R.string.voice_listening)
                        is VoiceRecognitionService.RecognitionState.Processing -> stringResource(id = R.string.voice_processing)
                        is VoiceRecognitionService.RecognitionState.Success -> recognitionResult ?: ""
                        is VoiceRecognitionService.RecognitionState.Error -> (recognitionState as VoiceRecognitionService.RecognitionState.Error).message
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = if (recognitionState is VoiceRecognitionService.RecognitionState.Error) 
                        Color.Red.copy(alpha = 0.8f) 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                // 显示部分识别结果以提供即时反馈
                if (recognitionState is VoiceRecognitionService.RecognitionState.Listening && !recognitionResult.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = recognitionResult ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            if (recognitionState is VoiceRecognitionService.RecognitionState.Listening) {
                                voiceRecognitionService.stopListening()
                            } else {
                                voiceRecognitionService.startListening()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .height(48.dp)  // 固定高度
                            .fillMaxWidth(0.6f)  // 设置固定宽度为父容器的60%
                    ) {
                        Text(
                            text = if (recognitionState is VoiceRecognitionService.RecognitionState.Listening) "停止" else "重试",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * 语音输入动画
 * 根据识别状态显示不同的动画效果
 * @param recognitionState 语音识别状态
 */
@Composable
fun VoiceInputAnimation(recognitionState: VoiceRecognitionService.RecognitionState) {
    // 动画参数
    val infiniteTransition = rememberInfiniteTransition(label = "VoiceAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScaleAnimation"
    )
    
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveAnimation"
    )
    
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AlphaAnimation"
    )
    
    Box(
        modifier = Modifier
            .size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // 波纹动画 (仅在监听状态显示)
        if (recognitionState is VoiceRecognitionService.RecognitionState.Listening) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(waveScale)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = waveAlpha))
            )
        }
        
        // 麦克风图标
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (recognitionState is VoiceRecognitionService.RecognitionState.Error) Color.Red else Primary)
                .scale(if (recognitionState is VoiceRecognitionService.RecognitionState.Listening) scale else 1f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "语音输入",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}