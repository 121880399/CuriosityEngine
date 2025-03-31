package org.zzy.curiosityengine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.zzy.curiosityengine.data.repository.AnswerRepository
import org.zzy.curiosityengine.data.repository.QuestionRepository
import org.zzy.curiosityengine.ui.screen.AnswerScreen
import org.zzy.curiosityengine.ui.screen.HistoryScreen
import org.zzy.curiosityengine.ui.screen.HomeScreen
import org.zzy.curiosityengine.ui.screen.SplashScreen
import org.zzy.curiosityengine.ui.theme.CuriosityEngineTheme
import org.zzy.curiosityengine.ui.viewmodel.AnswerViewModel
import org.zzy.curiosityengine.ui.viewmodel.HistoryViewModel
import org.zzy.curiosityengine.ui.viewmodel.QuestionViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 获取应用程序实例中的仓库
        val app = application as CuriosityEngineApplication
        val questionRepository = app.questionRepository
        val answerRepository = app.answerRepository

        setContent {
            CuriosityEngineTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(questionRepository, answerRepository)
                }
            }
        }
    }
}

/**
 * 应用导航
 * 管理应用的页面导航
 */
@Composable
fun AppNavigation(
    questionRepository: QuestionRepository,
    answerRepository: AnswerRepository
) {
    val navController = rememberNavController()
    
    // 创建视图模型
    val historyViewModel: HistoryViewModel = viewModel { HistoryViewModel(questionRepository) }
    val answerViewModel: AnswerViewModel = viewModel { AnswerViewModel(questionRepository, answerRepository, context = navController.context) }
    val questionViewModel: QuestionViewModel = viewModel { QuestionViewModel(questionRepository, answerRepository) }
    
    // 是否显示启动页
    var showSplash by remember { mutableStateOf(true) }

    NavHost(
        navController = navController,
        startDestination = if (showSplash) "splash" else "home"
    ) {
        // 启动页
        composable("splash") {
            SplashScreen(onTimeout = {
                showSplash = false
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        
        // 主页
        composable("home") {
            HomeScreen(
                onNavigateToHistory = {
                    navController.navigate("history")
                },
                onNavigateToAnswer = { questionId ->
                    navController.navigate("answer/$questionId")
                },
                onAskQuestion = { question ->
                    // 使用QuestionViewModel处理提问逻辑
                    questionViewModel.submitQuestion(question, "科学", "sk-a848522f7ed0424099504e79e94f9c45") // 实际使用时需要替换为真实的API密钥
                },
                viewModel = questionViewModel
            )
        }
        
        // 历史页面
        composable("history") {
            HistoryScreen(
                viewModel = historyViewModel,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onQuestionClick = { questionId ->
                    navController.navigate("answer/$questionId")
                }
            )
        }
        
        // 答案页面
        composable(
            route = "answer/{questionId}",
            arguments = listOf(navArgument("questionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getLong("questionId") ?: 0L
            val coroutineScope = rememberCoroutineScope()
            AnswerScreen(
                viewModel = answerViewModel,
                questionId = questionId,
                onBackClick = {
                    navController.popBackStack()
                },
                onRelatedQuestionClick = { relatedQuestion ->
                    // 处理相关问题点击，先检查本地数据库是否有对应问题的答案
                    coroutineScope.launch {
                        // 先保存问题
                        val newQuestionId = questionRepository.saveQuestion(relatedQuestion, "科学")
                        
                        // 检查是否已有答案
                        val existingAnswer = answerRepository.getAnswerByQuestionId(newQuestionId)
                        
                        if (existingAnswer == null) {
                            // 如果没有答案，则向DeepSeek发起请求
                            try {
                                // 获取API密钥
                                val apiKey = getApiKey()
                                if (apiKey.isNotEmpty()) {
                                    // 显示加载中状态
                                    answerViewModel.setLoading(true)
                                    
                                    // 发起API请求获取答案
                                    answerRepository.fetchAndSaveAnswer(newQuestionId, apiKey)
                                }
                            } catch (e: Exception) {
                                // 处理异常
                                android.util.Log.e("MainActivity", "获取答案失败: ${e.message}", e)
                            } finally {
                                // 无论成功与否，都导航到答案页面
                                answerViewModel.setLoading(false)
                            }
                        }
                        
                        // 导航到答案页面
                        navController.navigate("answer/$newQuestionId")
                    }
                }
            )
        }
    }
}

/**
 * 获取API密钥
 * @return API密钥
 */
private fun getApiKey(): String {
    // 这里应该从安全存储中获取API密钥，例如EncryptedSharedPreferences或其他安全存储机制
    // 为了演示，这里直接返回一个硬编码的API密钥
    return "sk-a848522f7ed0424099504e79e94f9c45" // 实际使用时应该替换为从安全存储获取的密钥
}