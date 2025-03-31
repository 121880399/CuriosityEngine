package org.zzy.curiosityengine.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.zzy.curiosityengine.R
import org.zzy.curiosityengine.data.model.Answer
import org.zzy.curiosityengine.data.model.Question
import org.zzy.curiosityengine.ui.theme.BackgroundLight
import org.zzy.curiosityengine.ui.theme.Primary
import org.zzy.curiosityengine.ui.theme.PrimaryVariant
import org.zzy.curiosityengine.ui.viewmodel.AnswerViewModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 答案页面
 * 展示问题的回答和相关问题
 * @param viewModel 答案页面视图模型
 * @param questionId 问题ID
 * @param onBackClick 返回按钮点击回调
 * @param onRelatedQuestionClick 相关问题点击回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerScreen(
    viewModel: AnswerViewModel,
    questionId: Long,
    onBackClick: () -> Unit,
    onRelatedQuestionClick: (String) -> Unit
) {
    // 加载问题和答案
    LaunchedEffect(questionId) {
        viewModel.loadQuestionAndAnswer(questionId)
    }

    // 获取问题和答案状态
    val question by viewModel.question.collectAsState()
    val answer by viewModel.answer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.answer_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.shareQuestionAndAnswer() }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(id = R.string.share))
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // 加载中
            if (isLoading) {
                CircularProgressIndicator(color = Primary)
            }
            // 错误信息
            else if (error != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "获取答案时出错：${error?.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadQuestionAndAnswer(questionId) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("重试")
                    }
                }
            }
            // 显示答案内容
            else if (question != null && answer != null) {
                AnswerContent(
                    question = question!!,
                    answer = answer!!,
                    onRelatedQuestionClick = onRelatedQuestionClick
                )
            }
        }
    }
}


/**
 * 相关问题项
 * @param question 问题内容
 * @param onClick 点击回调
 */
@Composable
fun RelatedQuestionItem(
    question: String,
    onClick: () -> Unit
) {
    // 动画效果
    val infiniteTransition = rememberInfiniteTransition(label = "RelatedQuestionAnimation")
    val animatedBrush by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BrushAnimation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    // 添加微妙的动画边框效果
                    val brush = Brush.linearGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.2f),
                            PrimaryVariant.copy(alpha = 0.3f),
                            Primary.copy(alpha = 0.2f)
                        ),
                        start = Offset(size.width * animatedBrush, 0f),
                        end = Offset(0f, size.height * (1f - animatedBrush))
                    )
                    drawRect(brush = brush, size = size)
                }
                .padding(12.dp)
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * 答案内容
 * @param question 问题对象
 * @param answer 答案对象
 * @param onRelatedQuestionClick 相关问题点击回调
 */
@Composable
fun AnswerContent(
    question: Question,
    answer: Answer,
    onRelatedQuestionClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    // 格式化时间
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(question.timestamp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 问题卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 问题图标和内容
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "?",
                            style = MaterialTheme.typography.titleMedium,
                            color = Primary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = question.content,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // 时间和分类
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Text(
                        text = question.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // 答案卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 答案内容
                Text(
                    text = answer.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // 如果有图片，显示图片
                if (!answer.imageUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // 图片加载动画
                    var isLoading by remember { mutableStateOf(true) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        // 加载图片
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(answer.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            onLoading = { isLoading = true },
                            onSuccess = { isLoading = false },
                            onError = { isLoading = false }
                        )

                        // 加载指示器
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = Primary
                            )
                        }
                    }
                }
            }
        }

        // 相关问题
        if (answer.relatedQuestions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.related_questions),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 相关问题列表
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        answer.relatedQuestions.forEach { relatedQuestion ->
                            RelatedQuestionItem(
                                question = relatedQuestion,
                                onClick = { onRelatedQuestionClick(relatedQuestion) }
                            )
                        }
                    }
                }
            }
        }

    }
}