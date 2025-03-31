package org.zzy.curiosityengine.data.repository

import kotlinx.coroutines.flow.Flow
import org.zzy.curiosityengine.data.api.DeepSeekApi
import org.zzy.curiosityengine.data.api.DeepSeekRequest
import org.zzy.curiosityengine.data.api.Message
import org.zzy.curiosityengine.data.dao.AnswerDao
import org.zzy.curiosityengine.data.dao.QuestionDao
import org.zzy.curiosityengine.data.model.Answer
import org.zzy.curiosityengine.data.model.Question

/**
 * 答案仓库类
 * 处理答案数据的获取和存储，以及与DeepSeek API的通信
 */
class AnswerRepository(
    private val answerDao: AnswerDao,
    private val questionDao: QuestionDao,
    private val deepSeekApi: DeepSeekApi
) {
    /**
     * 获取问题的答案
     * @param questionId 问题ID
     * @return 答案对象
     */
    suspend fun getAnswerByQuestionId(questionId: Long): Answer? {
        return answerDao.getAnswerByQuestionId(questionId)
    }
    
    /**
     * 获取所有答案
     * @return 答案列表流
     */
    fun getAllAnswers(): Flow<List<Answer>> {
        return answerDao.getAllAnswers()
    }
    
    /**
     * 从API获取答案并保存
     * @param questionId 问题ID
     * @param apiKey API密钥
     * @return 保存的答案ID
     */
    suspend fun fetchAndSaveAnswer(questionId: Long, apiKey: String): Result<Long> {
        android.util.Log.d("AnswerRepository", "开始获取答案: questionId=$questionId")
        return try {
            // 获取问题内容
            val question = questionDao.getQuestionById(questionId)
            if (question == null) {
                android.util.Log.e("AnswerRepository", "问题不存在: questionId=$questionId")
                return Result.failure(Exception("问题不存在"))
            }
            android.util.Log.d("AnswerRepository", "成功获取问题: ${question.content}")
            
            // 构建API请求
            val messages = listOf(
                Message(
                    role = "system",
                    content = "你是一个专为4-14岁儿童设计的AI助手，名为'好奇心引擎'。请用简单、生动、有趣的语言回答问题，确保内容准确且适合儿童理解。回答应该包含相关的科学知识，并引导孩子进一步探索。回答中可以适当加入表情符号增加趣味性。\n\n请以JSON格式返回你的回答，格式如下：\n{\n  \"answer\": \"你的回答内容\",\n  \"relatedQuestions\": [\"问题1？\", \"问题2？\", \"问题3？\"]\n}\n\n其中，answer字段包含你对问题的回答，relatedQuestions字段包含3个与当前问题相关的问题，这些问题应该能引导孩子进一步探索相关知识。"
                ),
                Message(
                    role = "user",
                    content = question.content
                )
            )
            
            val request = DeepSeekRequest(
                messages = messages,
                temperature = 0.7f,
                max_tokens = 2000
            )
            
            android.util.Log.d("AnswerRepository", "准备发送API请求: model=${request.model}, maxTokens=${request.max_tokens}")
            
            // 发送API请求
            android.util.Log.d("AnswerRepository", "开始调用DeepSeek API")
            val response = deepSeekApi.getAnswer("Bearer $apiKey", request)
            android.util.Log.d("AnswerRepository", "收到API响应: isSuccessful=${response.isSuccessful}, code=${response.code()}, message=${response.message()}")
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                android.util.Log.d("AnswerRepository", "API响应成功: id=${responseBody.id}, model=${responseBody.model}, choices.size=${responseBody.choices.size}")
                
                val responseContent = responseBody.choices.firstOrNull()?.message?.content
                if (responseContent.isNullOrEmpty()) {
                    android.util.Log.e("AnswerRepository", "API返回的答案为空: choices=${responseBody.choices}")
                    return Result.failure(Exception("API返回的答案为空"))
                }
                
                android.util.Log.d("AnswerRepository", "成功获取API响应内容: ${responseContent.take(100)}...")
                
                // 尝试从JSON中提取答案内容和相关问题
                var answerContent = responseContent
                var relatedQuestions = listOf<String>()
                
                try {
                    // 检查是否是JSON格式
                    if (responseContent.trim().startsWith("{") && responseContent.trim().endsWith("}")) {
                        android.util.Log.d("AnswerRepository", "检测到JSON格式响应，尝试解析")
                        
                        try {
                            // 使用Kotlin的JSON解析库
                            val jsonObject = org.json.JSONObject(responseContent)
                            if (jsonObject.has("answer")) {
                                answerContent = jsonObject.getString("answer")
                                android.util.Log.d("AnswerRepository", "从JSON成功提取到答案内容")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("AnswerRepository", "JSON解析失败: ${e.message}", e)
                            // 回退到正则表达式方法
                            val answerPattern = "\"answer\"\\s*:\\s*\"(.*?)\"(?=,|\\})"
                            val answerMatcher = Regex(answerPattern, RegexOption.DOT_MATCHES_ALL).find(responseContent)
                            
                            if (answerMatcher != null) {
                                answerContent = answerMatcher.groupValues[1]
                                android.util.Log.d("AnswerRepository", "使用正则表达式从JSON提取到答案内容")
                            }
                        }
                        
                        // 提取相关问题
                        relatedQuestions = extractRelatedQuestions(responseContent)
                    } else {
                        // 如果不是JSON格式，使用原来的方法提取相关问题
                        relatedQuestions = extractRelatedQuestions(responseContent)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AnswerRepository", "解析JSON响应异常: ${e.message}", e)
                    // 如果解析失败，使用原始响应内容作为答案内容
                    relatedQuestions = extractRelatedQuestions(responseContent)
                }
                
                android.util.Log.d("AnswerRepository", "提取相关问题: count=${relatedQuestions.size}")
                
                // 创建并保存答案
                val answer = Answer(
                    questionId = questionId,
                    content = answerContent ?: "",  // 确保content不为null
                    relatedQuestions = relatedQuestions
                )
                
                android.util.Log.d("AnswerRepository", "准备保存答案到数据库")
                val answerId = answerDao.insertAnswer(answer)
                android.util.Log.d("AnswerRepository", "答案保存成功: answerId=$answerId")
                
                // 更新问题状态为已回答
                questionDao.updateQuestion(question.copy(answered = true))
                android.util.Log.d("AnswerRepository", "问题状态已更新为已回答")
                
                Result.success(answerId)
            } else {
                val errorMsg = "API请求失败: ${response.code()} ${response.message()}"
                android.util.Log.e("AnswerRepository", errorMsg)
                android.util.Log.e("AnswerRepository", "错误响应体: ${response.errorBody()?.string()}")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("AnswerRepository", "获取答案异常: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 从答案内容中提取相关问题
     * @param content 答案内容
     * @return 相关问题列表
     */
    private fun extractRelatedQuestions(content: String): List<String> {
        android.util.Log.d("AnswerRepository", "开始提取相关问题")
        
        try {
            // 尝试解析JSON格式的响应
            val jsonContent = content.trim()
            if (jsonContent.startsWith("{") && jsonContent.endsWith("}")) {
                android.util.Log.d("AnswerRepository", "检测到JSON格式响应，尝试解析")
                
                try {
                    // 使用Kotlin的JSON解析库
                    val jsonObject = org.json.JSONObject(jsonContent)
                    if (jsonObject.has("relatedQuestions")) {
                        val questionsArray = jsonObject.getJSONArray("relatedQuestions")
                        val extractedQuestions = mutableListOf<String>()
                        
                        for (i in 0 until questionsArray.length()) {
                            extractedQuestions.add(questionsArray.getString(i))
                        }
                        
                        val result = extractedQuestions.take(3)
                        android.util.Log.d("AnswerRepository", "从JSON成功提取到${result.size}个相关问题: $result")
                        return result
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AnswerRepository", "JSON解析失败: ${e.message}", e)
                    // 回退到正则表达式方法
                    val questionsPattern = "\"relatedQuestions\"\\s*:\\s*\\[(.*?)\\](?=,|\\})"
                    
                    val questionsMatcher = Regex(questionsPattern, RegexOption.DOT_MATCHES_ALL).find(jsonContent)
                    if (questionsMatcher != null) {
                        val questionsJson = questionsMatcher.groupValues[1]
                        // 提取引号中的问题
                        val extractedQuestions = Regex("\"(.*?)\"")
                            .findAll(questionsJson)
                            .map { it.groupValues[1] }
                            .toList()
                            .take(3)
                        
                        if (extractedQuestions.isNotEmpty()) {
                            android.util.Log.d("AnswerRepository", "使用正则表达式从JSON提取到${extractedQuestions.size}个相关问题: $extractedQuestions")
                            return extractedQuestions
                        }
                    }
                }
            }
            
            // 如果JSON解析失败，回退到原来的文本分割方法
            android.util.Log.d("AnswerRepository", "JSON解析失败，回退到文本分割方法")
            
            // 主要查找标记："你可能还想知道："
            val primaryMarker = "你可能还想知道："
            val secondaryMarkers = listOf(
                "你可能还想知道",
                "你可能还会问",
                "相关问题",
                "延伸问题",
                "进一步探索"
            )
            
            val questions = mutableListOf<String>()
            
            // 首先尝试查找主要标记
            val primaryMarkerIndex = content.indexOf(primaryMarker)
            if (primaryMarkerIndex != -1) {
                android.util.Log.d("AnswerRepository", "找到主要标记：$primaryMarker")
                val remainingText = content.substring(primaryMarkerIndex + primaryMarker.length)
                
                // 按行分割，每行应该是一个问题
                val lines = remainingText.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotBlank() && it.endsWith("?") }
                    .take(3)
                
                if (lines.isNotEmpty()) {
                    android.util.Log.d("AnswerRepository", "通过行分割找到${lines.size}个问题")
                    questions.addAll(lines)
                } else {
                    // 如果按行分割没有找到问题，尝试按问号分割
                    android.util.Log.d("AnswerRepository", "按行分割未找到问题，尝试按问号分割")
                    val extractedQuestions = remainingText.split('?')
                        .filter { it.isNotBlank() }
                        .take(3)
                        .map { it.trim() + "?" }
                    
                    questions.addAll(extractedQuestions)
                }
            }
            
            // 如果主要标记没有找到问题，尝试次要标记
            if (questions.isEmpty()) {
                android.util.Log.d("AnswerRepository", "主要标记未找到问题，尝试次要标记")
                for (marker in secondaryMarkers) {
                    val markerIndex = content.indexOf(marker)
                    if (markerIndex != -1) {
                        android.util.Log.d("AnswerRepository", "找到次要标记：$marker")
                        val remainingText = content.substring(markerIndex + marker.length)
                        // 按问号分割
                        val extractedQuestions = remainingText.split('?')
                            .filter { it.isNotBlank() }
                            .take(3)
                            .map { it.trim() + "?" }
                        
                        questions.addAll(extractedQuestions)
                        if (questions.isNotEmpty()) {
                            android.util.Log.d("AnswerRepository", "通过次要标记找到${questions.size}个问题")
                            break
                        }
                    }
                }
            }
            
            // 如果仍然没有找到相关问题，生成一些通用的相关问题
            if (questions.isEmpty()) {
                android.util.Log.d("AnswerRepository", "未找到相关问题，使用默认问题")
                // 根据内容生成一些通用问题
                questions.add("为什么会这样呢？")
                questions.add("这个现象还有什么例子？")
                questions.add("这和我们的日常生活有什么关系？")
            }
            
            val result = questions.take(3) // 最多返回3个相关问题
            android.util.Log.d("AnswerRepository", "最终提取到${result.size}个相关问题: $result")
            return result
            
        } catch (e: Exception) {
            android.util.Log.e("AnswerRepository", "提取相关问题异常: ${e.message}", e)
            // 出现异常时返回默认问题
            return listOf(
                "为什么会这样呢？",
                "这个现象还有什么例子？",
                "这和我们的日常生活有什么关系？"
            )
        }
    }
    
    /**
     * 保存答案
     * @param answer 要保存的答案
     * @return 保存的答案ID
     */
    suspend fun saveAnswer(answer: Answer): Long {
        return answerDao.insertAnswer(answer)
    }
    
    /**
     * 更新答案
     * @param answer 要更新的答案
     */
    suspend fun updateAnswer(answer: Answer) {
        answerDao.updateAnswer(answer)
    }
    
    /**
     * 删除答案
     * @param answer 要删除的答案
     */
    suspend fun deleteAnswer(answer: Answer) {
        answerDao.deleteAnswer(answer)
    }
}