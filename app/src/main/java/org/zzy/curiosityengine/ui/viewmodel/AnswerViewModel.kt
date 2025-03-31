package org.zzy.curiosityengine.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.zzy.curiosityengine.data.model.Answer
import org.zzy.curiosityengine.data.model.Question
import org.zzy.curiosityengine.data.repository.AnswerRepository
import org.zzy.curiosityengine.data.repository.QuestionRepository
import org.zzy.curiosityengine.data.service.ShareService
import java.util.Date

/**
 * 答案页面视图模型
 * 负责加载问题和答案数据，并处理相关业务逻辑
 */
class AnswerViewModel(
    private val questionRepository: QuestionRepository,
    private val answerRepository: AnswerRepository,
    private val context: Context
) : ViewModel() {
    
    // 分享服务
    private val shareService = ShareService(context)
    
    // 问题数据流
    private val _question = MutableStateFlow<Question?>(null)
    val question: StateFlow<Question?> = _question.asStateFlow()
    
    // 答案数据流
    private val _answer = MutableStateFlow<Answer?>(null)
    val answer: StateFlow<Answer?> = _answer.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误信息
    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()
    
    // 相关问题列表
    private val _relatedQuestions = MutableStateFlow<List<String>>(emptyList())
    val relatedQuestions: StateFlow<List<String>> = _relatedQuestions.asStateFlow()
    
    /**
     * 加载问题和答案
     * 优化错误处理和异常情况
     * @param questionId 问题ID
     */
    fun loadQuestionAndAnswer(questionId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // 加载问题 - 添加空值检查
                val questionData = questionRepository.getQuestionById(questionId)
                if (questionData == null) {
                    throw Exception("未找到问题数据，问题ID: $questionId")
                }
                _question.value = questionData
                
                // 加载答案 - 添加空值检查和重试逻辑
                var answerData = answerRepository.getAnswerByQuestionId(questionId)
                
                // 如果答案为空，尝试重新加载一次
                if (answerData == null) {
                    // 短暂延迟后重试
                    kotlinx.coroutines.delay(500)
                    answerData = answerRepository.getAnswerByQuestionId(questionId)
                    
                    // 如果仍然为空，创建一个默认答案
                    if (answerData == null) {
                        answerData = Answer(
                            id = 0,
                            questionId = questionId,
                            content = "很抱歉，无法加载答案数据。请返回重试或联系客服。",
                            relatedQuestions = emptyList(),
                            imageUrl = null
                        )
                    }
                }
                
                _answer.value = answerData
                
                // 更新相关问题 - 优化空值处理
                if (answerData.relatedQuestions.isNotEmpty()) {
                    _relatedQuestions.value = answerData.relatedQuestions
                } else {
                    // 如果没有相关问题，生成一些通用的相关问题
                    _relatedQuestions.value = generateRelatedQuestions(questionData.content)
                }
                
            } catch (e: Exception) {
                _error.value = e
                // 记录错误日志
                android.util.Log.e("AnswerViewModel", "加载问题和答案失败: ${e.message}", e)
                
                // 设置默认值以避免UI崩溃
                if (_question.value == null) {
                    _question.value = Question(
                        id = questionId,
                        content = "加载问题失败",
                        timestamp = Date(),
                        category = "error"
                    )
                }
                
                if (_answer.value == null) {
                    _answer.value = Answer(
                        id = 0,
                        questionId = questionId,
                        content = "很抱歉，无法加载答案数据。请返回重试或联系客服。",
                        relatedQuestions = emptyList(),
                        imageUrl = null
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 分享问题和答案
     */
    fun shareQuestionAndAnswer() {
        val currentQuestion = _question.value
        val currentAnswer = _answer.value
        
        if (currentQuestion != null && currentAnswer != null) {
            shareService.shareQuestionAndAnswer(currentQuestion, currentAnswer)
        }
    }
    
    /**
     * 生成相关问题
     * 根据当前问题内容生成相关的问题
     * @param questionContent 问题内容
     * @return 相关问题列表
     */
    private fun generateRelatedQuestions(questionContent: String): List<String> {
        // 智能分析问题内容，生成更相关的问题
        val questions = mutableListOf<String>()
        
        // 提取问题中的关键词和主题
        val keywords = extractKeywords(questionContent)
        
        // 获取主要关键词和次要关键词
        val primaryKeyword = keywords.firstOrNull() ?: "这个主题"
        val secondaryKeyword = keywords.getOrNull(1) ?: "相关内容"
        val tertiaryKeyword = keywords.getOrNull(2) ?: "这个领域"
        
        // 根据问题类型和关键词生成相关问题
        when {
            questionContent.contains("为什么") -> {
                // 因果类问题
                questions.add("这个${primaryKeyword}的科学原理是什么？")
                questions.add("${primaryKeyword}与${secondaryKeyword}之间有什么关联？")
                questions.add("${primaryKeyword}在历史上是如何被发现和研究的？")
                questions.add("${primaryKeyword}对${tertiaryKeyword}有什么影响？")
                questions.add("为什么${primaryKeyword}在不同条件下会有不同表现？")
            }
            questionContent.contains("怎么") || questionContent.contains("如何") -> {
                // 方法类问题
                questions.add("为什么${primaryKeyword}的方法会有效？")
                questions.add("除了常规方法外，还有哪些创新方式可以${secondaryKeyword}？")
                questions.add("${primaryKeyword}的过程中最容易出现哪些问题？")
                questions.add("专家们是如何高效地${primaryKeyword}的？")
                questions.add("${primaryKeyword}的技术在未来会如何发展？")
            }
            questionContent.contains("是什么") || questionContent.contains("什么是") -> {
                // 定义类问题
                questions.add("${primaryKeyword}的核心特征是什么？")
                questions.add("${primaryKeyword}与${secondaryKeyword}有什么区别和联系？")
                questions.add("${primaryKeyword}在${tertiaryKeyword}中扮演什么角色？")
                questions.add("${primaryKeyword}的发展历程是怎样的？")
                questions.add("为什么${primaryKeyword}对现代社会如此重要？")
            }
            questionContent.contains("有哪些") || questionContent.contains("列举") -> {
                // 列举类问题
                questions.add("这些${primaryKeyword}有什么共同特点和区别？")
                questions.add("在${secondaryKeyword}领域，哪个${primaryKeyword}最具代表性？")
                questions.add("如何评价不同${primaryKeyword}的优劣？")
                questions.add("这些${primaryKeyword}是如何演变发展的？")
                questions.add("未来可能会出现哪些新的${primaryKeyword}？")
            }
            questionContent.contains("区别") || questionContent.contains("不同") -> {
                // 比较类问题
                questions.add("${primaryKeyword}和${secondaryKeyword}在哪些方面最为相似？")
                questions.add("是什么因素导致了${primaryKeyword}和${secondaryKeyword}的差异？")
                questions.add("在实际应用中，如何选择${primaryKeyword}或${secondaryKeyword}？")
                questions.add("${primaryKeyword}和${secondaryKeyword}各自的优势是什么？")
                questions.add("未来${primaryKeyword}和${secondaryKeyword}的发展趋势如何？")
            }
            else -> {
                // 通用问题 - 根据关键词生成更多样化的问题
                if (keywords.isNotEmpty()) {
                    questions.add("${primaryKeyword}的基本原理是什么？")
                    questions.add("${primaryKeyword}在日常生活中有哪些应用？")
                    questions.add("${primaryKeyword}与${secondaryKeyword}之间有什么关系？")
                    questions.add("${primaryKeyword}的历史发展过程是怎样的？")
                    questions.add("未来${primaryKeyword}可能会有哪些创新和突破？")
                    questions.add("${primaryKeyword}在不同文化背景下有什么不同理解？")
                    questions.add("如何评价${primaryKeyword}对社会的影响？")
                } else {
                    // 完全无法提取关键词时的备选问题
                    questions.add("这个现象背后的原理是什么？")
                    questions.add("这个问题在历史上是如何被研究的？")
                    questions.add("这个领域还有哪些相关的重要概念？")
                    questions.add("这个主题与日常生活有什么联系？")
                    questions.add("专家们对这个问题有哪些不同见解？")
                }
            }
        }
        
        // 随机打乱问题顺序，增加多样性，然后取前3个
        return questions.shuffled().take(3)
    }
    
    /**
     * 从问题中提取关键词
     * @param question 问题内容
     * @return 关键词列表
     */
    private fun extractKeywords(question: String): List<String> {
        // 增强实现：更全面地移除常见的问题词和停用词，更智能地分割句子，提取可能的名词短语
        val questionWords = listOf("为什么", "怎么", "如何", "是什么", "有哪些", "列举", "什么是", "怎样", "为何", "如何才能", "能否", "可以吗")
        val stopWords = listOf("的", "是", "在", "了", "吗", "呢", "啊", "哪些", "这个", "那个", "一个", "这些", "那些", "和", "与", "以及", "或者", "还是")
        
        // 第一步：移除问题词
        var processed = question
        questionWords.forEach { word ->
            processed = processed.replace(word, " ")
        }
        
        // 第二步：分词并移除停用词
        val words = processed.split(" ", "，", "。", "？", "！", "、", "：", "；")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        
        val filteredWords = words.filter { word ->
            word.length > 1 && !stopWords.any { stopWord -> word.contains(stopWord) }
        }
        
        // 第三步：合并相邻的单字形成短语（如果单字不在停用词中）
        val singleChars = words.filter { it.length == 1 && !stopWords.contains(it) }
        val phrases = mutableListOf<String>()
        
        if (singleChars.size >= 2) {
            for (i in 0 until singleChars.size - 1) {
                phrases.add(singleChars[i] + singleChars[i + 1])
            }
        }
        
        // 合并结果并按长度排序（优先选择较长的词语，它们通常更有意义）
        return (filteredWords + phrases)
            .distinct()
            .sortedByDescending { it.length }
            .take(5) // 最多取5个关键词，增加多样性
    }
    
    /**
     * 设置加载状态
     * @param loading 是否正在加载
     */
    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    /**
     * 设置错误信息
     * @param error 错误信息
     */
    fun setError(error: Throwable?) {
        _error.value = error
    }
}