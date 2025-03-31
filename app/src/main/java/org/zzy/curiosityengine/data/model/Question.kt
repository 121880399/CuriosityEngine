package org.zzy.curiosityengine.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 问题实体类
 * 用于存储用户提出的问题及其相关信息
 */
@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String, // 问题内容
    val timestamp: Date, // 提问时间
    val category: String = "科学", // 问题类别，默认为科学
    val answered: Boolean = false // 是否已回答
)