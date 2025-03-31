package org.zzy.curiosityengine.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.zzy.curiosityengine.data.model.Question
import java.util.Date

/**
 * 问题数据访问对象
 * 定义对问题表的数据库操作方法
 */
@Dao
interface QuestionDao {
    /**
     * 插入一个问题
     * @param question 要插入的问题
     * @return 插入后的问题ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long
    
    /**
     * 更新问题
     * @param question 要更新的问题
     */
    @Update
    suspend fun updateQuestion(question: Question)
    
    /**
     * 删除问题
     * @param question 要删除的问题
     */
    @Delete
    suspend fun deleteQuestion(question: Question)
    
    /**
     * 获取所有问题，按时间降序排列
     * @return 问题列表流
     */
    @Query("SELECT * FROM questions ORDER BY timestamp DESC")
    fun getAllQuestions(): Flow<List<Question>>
    
    /**
     * 根据ID获取问题
     * @param id 问题ID
     * @return 问题对象
     */
    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): Question?
    
    /**
     * 获取今天的问题
     * @param startOfDay 今天开始的时间戳
     * @return 今天的问题列表流
     */
    @Query("SELECT * FROM questions WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getTodayQuestions(startOfDay: Date): Flow<List<Question>>
    
    /**
     * 获取昨天的问题
     * @param startOfYesterday 昨天开始的时间戳
     * @param endOfYesterday 昨天结束的时间戳
     * @return 昨天的问题列表流
     */
    @Query("SELECT * FROM questions WHERE timestamp >= :startOfYesterday AND timestamp < :endOfYesterday ORDER BY timestamp DESC")
    fun getYesterdayQuestions(startOfYesterday: Date, endOfYesterday: Date): Flow<List<Question>>
    
    /**
     * 获取更早的问题
     * @param endOfYesterday 昨天结束的时间戳
     * @return 更早的问题列表流
     */
    @Query("SELECT * FROM questions WHERE timestamp < :endOfYesterday ORDER BY timestamp DESC")
    fun getEarlierQuestions(endOfYesterday: Date): Flow<List<Question>>
    
    /**
     * 搜索问题
     * @param query 搜索关键词
     * @return 匹配的问题列表流
     */
    @Query("SELECT * FROM questions WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchQuestions(query: String): Flow<List<Question>>
}