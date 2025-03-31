package org.zzy.curiosityengine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.zzy.curiosityengine.data.model.Question
import org.zzy.curiosityengine.data.repository.QuestionRepository
import java.util.Calendar
import java.util.Date

/**
 * 历史页面视图模型
 * 负责加载历史问题数据，并按照时间分类
 */
class HistoryViewModel(
    private val questionRepository: QuestionRepository
) : ViewModel() {
    
    // 所有问题数据流
    private val _allQuestions = MutableStateFlow<List<Question>>(emptyList())
    
    // 今天的问题
    val todayQuestions: Flow<List<Question>> = _allQuestions.map { questions ->
        questions.filter { isToday(it.timestamp) }
    }
    
    // 昨天的问题
    val yesterdayQuestions: Flow<List<Question>> = _allQuestions.map { questions ->
        questions.filter { isYesterday(it.timestamp) }
    }
    
    // 更早的问题
    val earlierQuestions: Flow<List<Question>> = _allQuestions.map { questions ->
        questions.filter { isEarlier(it.timestamp) }
    }
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误信息
    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()
    
    init {
        loadQuestions()
    }
    
    /**
     * 加载所有问题
     */
    fun loadQuestions() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // 获取所有问题
                val questions = questionRepository.getAllQuestions()
                questions.collect { questionList ->
                    _allQuestions.value = questionList
                }
                
            } catch (e: Exception) {
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 搜索问题
     * @param query 搜索关键词
     */
    fun searchQuestions(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // 如果搜索关键词为空，加载所有问题
                if (query.isBlank()) {
                    loadQuestions()
                    return@launch
                }
                
                // 从仓库搜索问题
                val questions = questionRepository.searchQuestions(query)
                questions.collect { questionList ->
                    _allQuestions.value = questionList
                }
                
            } catch (e: Exception) {
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 判断日期是否是今天
     * @param date 日期
     * @return 是否是今天
     */
    private fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply { time = date }
        
        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * 判断日期是否是昨天
     * @param date 日期
     * @return 是否是昨天
     */
    private fun isYesterday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val calendar = Calendar.getInstance().apply { time = date }
        
        return yesterday.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * 判断日期是否是更早的日期（不是今天也不是昨天）
     * @param date 日期
     * @return 是否是更早的日期
     */
    private fun isEarlier(date: Date): Boolean {
        return !isToday(date) && !isYesterday(date)
    }
}