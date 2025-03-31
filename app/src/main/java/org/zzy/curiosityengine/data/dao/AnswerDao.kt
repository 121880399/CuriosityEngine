package org.zzy.curiosityengine.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.zzy.curiosityengine.data.model.Answer

/**
 * 答案数据访问对象
 * 定义对答案表的数据库操作方法
 */
@Dao
interface AnswerDao {
    /**
     * 插入一个答案
     * @param answer 要插入的答案
     * @return 插入后的答案ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: Answer): Long
    
    /**
     * 更新答案
     * @param answer 要更新的答案
     */
    @Update
    suspend fun updateAnswer(answer: Answer)
    
    /**
     * 删除答案
     * @param answer 要删除的答案
     */
    @Delete
    suspend fun deleteAnswer(answer: Answer)
    
    /**
     * 根据问题ID获取答案
     * @param questionId 问题ID
     * @return 答案对象
     */
    @Query("SELECT * FROM answers WHERE questionId = :questionId")
    suspend fun getAnswerByQuestionId(questionId: Long): Answer?
    
    /**
     * 根据ID获取答案
     * @param id 答案ID
     * @return 答案对象
     */
    @Query("SELECT * FROM answers WHERE id = :id")
    suspend fun getAnswerById(id: Long): Answer?
    
    /**
     * 获取所有答案
     * @return 答案列表流
     */
    @Query("SELECT * FROM answers")
    fun getAllAnswers(): Flow<List<Answer>>
}