package org.zzy.curiosityengine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.zzy.curiosityengine.data.model.Question
import org.zzy.curiosityengine.data.repository.AnswerRepository
import org.zzy.curiosityengine.data.repository.QuestionRepository

/**
 * 问题视图模型
 * 负责处理问题提交和答案获取的业务逻辑
 */
class QuestionViewModel(
    private val questionRepository: QuestionRepository,
    private val answerRepository: AnswerRepository
) : ViewModel() {
    
    // 提交状态
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()
    
    // 错误信息
    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()
    
    // 提交结果（问题ID）
    private val _submittedQuestionId = MutableStateFlow<Long?>(null)
    val submittedQuestionId: StateFlow<Long?> = _submittedQuestionId.asStateFlow()
    
    /**
     * 提交问题
     * @param content 问题内容
     * @param category 问题类别
     * @param apiKey API密钥
     */
    fun submitQuestion(content: String, category: String = "科学", apiKey: String) {
        android.util.Log.d("QuestionViewModel", "开始提交问题: content=${content.take(50)}..., category=$category")
        viewModelScope.launch {
            try {
                _isSubmitting.value = true
                _error.value = null
                android.util.Log.d("QuestionViewModel", "状态已更新: isSubmitting=true")
                
                // 保存问题
                android.util.Log.d("QuestionViewModel", "准备保存问题到数据库")
                val questionId = questionRepository.saveQuestion(content, category)
                android.util.Log.d("QuestionViewModel", "问题保存成功: questionId=$questionId")
                
                // 获取并保存答案
                android.util.Log.d("QuestionViewModel", "开始调用fetchAndSaveAnswer: questionId=$questionId")
                answerRepository.fetchAndSaveAnswer(questionId, apiKey)
                    .onSuccess { answerId ->
                        android.util.Log.d("QuestionViewModel", "获取答案成功: answerId=$answerId")
                        
                        // 更新问题状态为已回答
                        val question = questionRepository.getQuestionById(questionId)
                        if (question != null) {
                            android.util.Log.d("QuestionViewModel", "更新问题状态为已回答")
                            questionRepository.updateQuestion(question.copy(answered = true))
                        } else {
                            android.util.Log.w("QuestionViewModel", "无法更新问题状态: 问题不存在 questionId=$questionId")
                        }
                        
                        // 设置提交结果
                        _submittedQuestionId.value = questionId
                        android.util.Log.d("QuestionViewModel", "提交完成: submittedQuestionId=$questionId")
                    }
                    .onFailure { e ->
                        android.util.Log.e("QuestionViewModel", "获取答案失败: ${e.message}", e)
                        _error.value = e
                    }
                
            } catch (e: Exception) {
                android.util.Log.e("QuestionViewModel", "提交问题异常: ${e.message}", e)
                _error.value = e
            } finally {
                _isSubmitting.value = false
                android.util.Log.d("QuestionViewModel", "状态已更新: isSubmitting=false")
            }
        }
    }
    
    /**
     * 重置状态
     */
    fun resetState() {
        _isSubmitting.value = false
        _error.value = null
        _submittedQuestionId.value = null
    }
    
    /**
     * 获取随机问题
     * 从问题数据库中获取随机问题列表
     * @param count 需要获取的问题数量
     * @param category 问题类别
     * @return 随机问题列表
     */
    fun getRandomQuestions(count: Int = 3, category: String = "science"): List<String> {
        val questionDatabase = org.zzy.curiosityengine.data.database.QuestionDatabase()
        return questionDatabase.getRandomQuestions(count, category)
    }
}