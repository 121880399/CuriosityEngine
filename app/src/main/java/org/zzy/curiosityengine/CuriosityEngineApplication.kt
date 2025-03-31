package org.zzy.curiosityengine

import android.app.Application
import androidx.activity.result.ActivityResultLauncher
import org.zzy.curiosityengine.data.api.ApiService
import org.zzy.curiosityengine.data.database.AppDatabase
import org.zzy.curiosityengine.data.repository.AnswerRepository
import org.zzy.curiosityengine.data.repository.QuestionRepository

/**
 * 应用程序类
 * 负责初始化数据库和仓库实例
 */
class CuriosityEngineApplication : Application() {
    
    // 数据库实例
    private lateinit var database: AppDatabase
    
    // 仓库实例
    lateinit var questionRepository: QuestionRepository
        private set
    
    lateinit var answerRepository: AnswerRepository
        private set
        
    // 音频权限请求启动器
    lateinit var audioPermissionLauncher: ActivityResultLauncher<String>
        internal set
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化数据库
        database = AppDatabase.getDatabase(this)
        
        // 初始化仓库
        questionRepository = QuestionRepository(database.questionDao())
        answerRepository = AnswerRepository(
            database.answerDao(),
            database.questionDao(),
            ApiService.deepSeekApi
        )
    }
}