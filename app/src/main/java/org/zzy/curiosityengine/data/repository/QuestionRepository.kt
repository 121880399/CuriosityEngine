package org.zzy.curiosityengine.data.repository

import kotlinx.coroutines.flow.Flow
import org.zzy.curiosityengine.data.dao.QuestionDao
import org.zzy.curiosityengine.data.model.Question
import java.util.Calendar
import java.util.Date

/**
 * 问题仓库类
 * 处理问题数据的获取和存储
 */
class QuestionRepository(private val questionDao: QuestionDao) {
    
    /**
     * 获取所有问题
     */
    fun getAllQuestions(): Flow<List<Question>> {
        return questionDao.getAllQuestions()
    }
    
    /**
     * 获取今天的问题
     */
    fun getTodayQuestions(): Flow<List<Question>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time
        
        return questionDao.getTodayQuestions(startOfDay)
    }
    
    /**
     * 获取昨天的问题
     */
    fun getYesterdayQuestions(): Flow<List<Question>> {
        val calendar = Calendar.getInstance()
        
        // 今天开始时间
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val endOfYesterday = calendar.time
        
        // 昨天开始时间
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startOfYesterday = calendar.time
        
        return questionDao.getYesterdayQuestions(startOfYesterday, endOfYesterday)
    }
    
    /**
     * 获取更早的问题
     */
    fun getEarlierQuestions(): Flow<List<Question>> {
        val calendar = Calendar.getInstance()
        
        // 昨天开始时间
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val endOfEarlier = calendar.time
        
        return questionDao.getEarlierQuestions(endOfEarlier)
    }
    
    /**
     * 保存问题
     * @param question 要保存的问题
     * @param category 问题类别
     * @return 保存后的问题ID
     */
    suspend fun saveQuestion(question: Question): Long {
        return questionDao.insertQuestion(question)
    }
    
    /**
     * 保存问题（重载方法，直接接收问题内容）
     * @param content 问题内容
     * @param category 问题类别
     * @return 保存后的问题ID
     */
    suspend fun saveQuestion(content: String, category: String = "科学"): Long {
        val question = Question(
            content = content.trim(),
            timestamp = Date(),
            category = category
        )
        return questionDao.insertQuestion(question)
    }
    
    /**
     * 更新问题
     * @param question 要更新的问题
     */
    suspend fun updateQuestion(question: Question) {
        questionDao.updateQuestion(question)
    }
    
    /**
     * 搜索问题
     * @param query 搜索关键词
     * @return 匹配的问题列表流
     */
    fun searchQuestions(query: String): Flow<List<Question>> {
        return questionDao.searchQuestions("%$query%")
    }
    
    /**
     * 根据ID获取问题
     */
    suspend fun getQuestionById(id: Long): Question? {
        return questionDao.getQuestionById(id)
    }
    
    /**
     * 插入问题
     */
    suspend fun insertQuestion(content: String): Long {
        val question = Question(
            content = content,
            timestamp = Date(),
            category = "科学",
            answered = false
        )
        return questionDao.insertQuestion(question)
    }
    
    /**
     * 删除问题
     */
    suspend fun deleteQuestion(question: Question) {
        questionDao.deleteQuestion(question)
    }
}