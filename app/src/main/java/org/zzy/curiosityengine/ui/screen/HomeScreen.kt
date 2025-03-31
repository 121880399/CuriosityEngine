package org.zzy.curiosityengine.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.zzy.curiosityengine.data.service.VoiceRecognitionService
import org.zzy.curiosityengine.ui.components.VoiceInputDialog
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.zzy.curiosityengine.CuriosityEngineApplication
import org.zzy.curiosityengine.R
import org.zzy.curiosityengine.ui.components.BottomNavBar
import org.zzy.curiosityengine.ui.components.FullScreenLoading
import org.zzy.curiosityengine.ui.theme.BackgroundLight
import org.zzy.curiosityengine.ui.theme.Primary
import org.zzy.curiosityengine.ui.theme.TagScience
import org.zzy.curiosityengine.ui.theme.TagText
import org.zzy.curiosityengine.ui.viewmodel.QuestionViewModel

/**
 * 主页面
 * 用户可以在此页面提问问题
 * @param onNavigateToHistory 导航到历史页面的回调
 * @param onNavigateToAnswer 导航到答案页面的回调
 * @param onAskQuestion 提问问题的回调
 * @param viewModel 问题视图模型
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToAnswer: (Long) -> Unit,
    onAskQuestion: (String) -> Unit,
    viewModel: QuestionViewModel
) {
    // 问题输入状态
    var questionText by remember { mutableStateOf("") }

    // 获取ViewModel状态
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val error by viewModel.error.collectAsState()
    val submittedQuestionId by viewModel.submittedQuestionId.collectAsState()

    // 获取Context用于显示Toast
    val context = LocalContext.current

    // 处理提交结果
    LaunchedEffect(submittedQuestionId) {
        submittedQuestionId?.let { questionId ->
            android.util.Log.d("HomeScreen", "收到提交结果: questionId=$questionId, 准备导航到答案页面")
            // 导航到答案页面
            onNavigateToAnswer(questionId)
            // 重置ViewModel状态
            viewModel.resetState()
            android.util.Log.d("HomeScreen", "已重置ViewModel状态")
        }
    }

    // 处理错误状态
    LaunchedEffect(error) {
        error?.let { throwable ->
            android.util.Log.e("HomeScreen", "提交失败: ${throwable.message ?: "未知错误"}", throwable)
            // 显示错误Toast
            android.widget.Toast.makeText(
                context,
                "提交失败: ${throwable.message ?: "未知错误"}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            // 重置错误状态
            viewModel.resetState()
            android.util.Log.d("HomeScreen", "已重置ViewModel状态")
        }
    }

    // 使用问题数据库获取随机问题，并在每次页面显示时刷新
    var randomQuestions by remember { mutableStateOf(emptyList<String>()) }

    // 每次页面显示时刷新随机问题
    LaunchedEffect(Unit) {
        randomQuestions = viewModel.getRandomQuestions(3, "science")
    }

    // 添加全屏加载动画 - 放在Scaffold外部以覆盖整个屏幕
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    currentRoute = "home",
                    onHomeClick = { },
                    onHistoryClick = onNavigateToHistory
                )
            }
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 应用名称
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = Primary,
                    modifier = Modifier.padding(start = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // 历史按钮已移除，因为与底部导航栏功能重复
            }

            // 欢迎文本
            Text(
                text = stringResource(id = R.string.home_greeting),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )

            // 提示文本
            Text(
                text = stringResource(id = R.string.home_question_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            // 随机问题展示
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                randomQuestions.forEach { question ->
                    RandomQuestionItem(question = question) {
                        questionText = question
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 底部输入框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 文本输入框 - 优化响应速度和用户体验
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    placeholder = { Text(stringResource(id = R.string.home_input_hint)) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = Primary,
                        // 优化文本字段颜色变化动画
                        containerColor = Color.White
                    ),
                    maxLines = 1,
                    singleLine = true,
                    // 添加键盘操作处理
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Send
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSend = {
                            if (questionText.isNotEmpty() && !isSubmitting) {
                                android.util.Log.d("HomeScreen", "键盘发送问题: $questionText")
                                onAskQuestion(questionText)
                                questionText = ""
                            }
                        }
                    ),
                    // 当提交中时禁用输入框
                    enabled = !isSubmitting
                )

                // 语音输入按钮
                var showVoiceInputDialog by remember { mutableStateOf(false) }
                val context = LocalContext.current
                val activity = context as ComponentActivity
                val voiceRecognitionService = remember { 
                    VoiceRecognitionService(context)
                }
                
                // 获取应用程序实例中已注册的权限请求启动器
                val app = activity.application as CuriosityEngineApplication
                val permissionLauncher = app.audioPermissionLauncher

                IconButton(
                    onClick = { 
                        // 点击时检查权限并设置回调
                        org.zzy.curiosityengine.utils.PermissionUtils.setPermissionCallbacks(
                            activity = activity,
                            permissionLauncher = permissionLauncher,
                            onPermissionGranted = {
                                // 已有权限或权限被授予后，显示语音输入对话框
                                showVoiceInputDialog = true
                            },
                            onPermissionDenied = {
                                // 权限被拒绝，不显示对话框
                            }
                        )
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Primary),
                    // 当提交中时禁用语音按钮
                    enabled = !isSubmitting
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = stringResource(id = R.string.voice_input),
                        tint = Color.White
                    )
                }

                // 语音输入对话框
                if (showVoiceInputDialog) {
                    VoiceInputDialog(
                        voiceRecognitionService = voiceRecognitionService,
                        onTextRecognized = { recognizedText ->
                            questionText = recognizedText
                        },
                        onDismiss = { showVoiceInputDialog = false }
                    )
                }

                // 发送按钮或加载指示器
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    // 加载指示器
                    Box {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSubmitting,
                            enter = fadeIn(animationSpec = tween(150)),
                            exit = fadeOut(animationSpec = tween(150))
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }

                    // 发送按钮
                    Box {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = questionText.isNotEmpty() && !isSubmitting,
                            enter = fadeIn(animationSpec = tween(150)),
                            exit = fadeOut(animationSpec = tween(150))
                        ) {
                            // 使用可点击区域增大按钮的可点击范围
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Primary)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = rememberRipple(bounded = false, color = Color.White, radius = 24.dp),
                                        onClick = {
                                            if (questionText.isNotEmpty() && !isSubmitting) {
                                                android.util.Log.d("HomeScreen", "点击发送按钮提问: $questionText")
                                                onAskQuestion(questionText)
                                                questionText = ""
                                            }
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "发送",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        // 添加全屏加载动画 - 确保它在最上层显示
        FullScreenLoading(isVisible = isSubmitting)
    }
}

/**
 * 随机问题项
 * @param question 问题文本
 * @param onClick 点击回调
 */
@Composable
fun RandomQuestionItem(question: String, onClick: () -> Unit) {
    // 交互状态
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }

    // 缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true, color = Primary),
                onClick = {
                    onClick()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            // 科学标签
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TagScience)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "科学",
                    style = MaterialTheme.typography.bodySmall,
                    color = TagText
                )
            }
        }
    }
}