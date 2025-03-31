package org.zzy.curiosityengine.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * DeepSeek API接口
 * 用于与DeepSeek大模型API进行通信
 */
interface DeepSeekApi {
    /**
     * 发送问题获取回答
     * @param apiKey API密钥
     * @param request 请求体
     * @return API响应
     */
    @POST("/chat/completions")
    suspend fun getAnswer(
        @Header("Authorization") apiKey: String,
        @Body request: DeepSeekRequest
    ): Response<DeepSeekResponse>
}

/**
 * DeepSeek请求体
 * @param model 使用的模型名称
 * @param messages 对话消息列表
 * @param temperature 温度参数，控制随机性
 * @param max_tokens 最大生成的token数量
 */
data class DeepSeekRequest(
    val model: String = "deepseek-chat",
    val messages: List<Message>,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 2000
)

/**
 * 对话消息
 * @param role 角色（system/user/assistant）
 * @param content 消息内容
 */
data class Message(
    val role: String,
    val content: String
)

/**
 * DeepSeek响应体
 * @param id 响应ID
 * @param object 对象类型
 * @param created 创建时间戳
 * @param model 使用的模型
 * @param choices 生成的回答选项
 */
data class DeepSeekResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>
)

/**
 * 回答选项
 * @param index 选项索引
 * @param message 回答消息
 * @param finish_reason 结束原因
 */
data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String
)