package org.zzy.curiosityengine.data.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import org.zzy.curiosityengine.R
import org.zzy.curiosityengine.data.model.Answer
import org.zzy.curiosityengine.data.model.Question

/**
 * 分享服务
 * 负责处理分享功能，支持多种分享方式和平台兼容性
 * 优化错误处理和异常情况处理
 */
class ShareService(private val context: Context) {
    
    /**
     * 分享问题和答案
     * @param question 问题对象
     * @param answer 答案对象
     * @return 分享是否成功
     */
    fun shareQuestionAndAnswer(question: Question, answer: Answer): Boolean {
        val shareTitle = context.getString(R.string.share_title)
        val shareQuestion = context.getString(R.string.share_question)
        val shareAnswer = context.getString(R.string.share_answer)
        val appName = context.getString(R.string.app_name)
        
        // 构建更美观的分享文本，包含应用名称和分隔符
        val shareText = buildString {
            append("$shareTitle\n")
            append("$shareQuestion\n${question.content}\n\n")
            append("$shareAnswer\n${answer.content}\n\n")
            append("——来自 $appName")
        }
        
        try {
            // 如果答案包含图片，则使用带图片的分享方法
            if (!answer.imageUrl.isNullOrEmpty()) {
                return shareTextWithImage(shareText, answer.imageUrl, shareTitle)
            } else {
                // 否则使用纯文本分享
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_SUBJECT, shareTitle) // 添加主题
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                
                val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.share_via))
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(shareIntent)
                return true
            }
        } catch (e: Exception) {
            android.util.Log.e("ShareService", "分享问题和答案失败: ${e.message}", e)
            return false
        }
    }
    
    /**
     * 分享文本内容
     * @param text 要分享的文本
     * @param title 分享对话框标题
     * @param subject 分享主题（邮件等应用会使用）
     * @return 分享是否成功
     */
    fun shareText(text: String, title: String = context.getString(R.string.share_via), subject: String? = null): Boolean {
        try {
            // 检查文本是否为空
            if (text.isBlank()) {
                android.util.Log.w("ShareService", "尝试分享空文本")
                return false
            }
            
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                type = "text/plain"
            }
            
            val shareIntent = Intent.createChooser(sendIntent, title)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
            return true
        } catch (e: Exception) {
            android.util.Log.e("ShareService", "分享文本失败: ${e.message}", e)
            return false
        }
    }
    
    /**
     * 分享带图片的文本内容
     * @param text 要分享的文本
     * @param imageUrl 图片URL
     * @param title 分享对话框标题
     * @return 分享是否成功
     */
    fun shareTextWithImage(text: String, imageUrl: String, title: String = context.getString(R.string.share_via)): Boolean {
        try {
            // 检查URL是否有效
            if (imageUrl.isBlank()) {
                android.util.Log.w("ShareService", "图片URL为空，回退到纯文本分享")
                return shareText(text, title)
            }
            
            // 验证URI格式
            val imageUri = try {
                android.net.Uri.parse(imageUrl)
            } catch (e: Exception) {
                android.util.Log.e("ShareService", "无效的图片URI: $imageUrl", e)
                return shareText(text, title)
            }
            
            // 检查URI是否可访问
            val isUriAccessible = try {
                context.contentResolver.getType(imageUri) != null
            } catch (e: Exception) {
                false
            }
            
            if (!isUriAccessible) {
                android.util.Log.w("ShareService", "无法访问图片URI: $imageUrl，回退到纯文本分享")
                return shareText(text, title)
            }
            
            // 尝试使用图片URL创建分享意图
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_STREAM, imageUri)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val shareIntent = Intent.createChooser(sendIntent, title)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
            return true
        } catch (e: Exception) {
            android.util.Log.e("ShareService", "分享图片失败: ${e.message}，回退到纯文本分享", e)
            // 如果分享图片失败，回退到纯文本分享
            return shareText(text, title)
        }
    }
}