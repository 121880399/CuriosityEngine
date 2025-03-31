package org.zzy.curiosityengine.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * 答案实体类
 * 用于存储问题的回答及其相关信息
 */
@Entity(
    tableName = "answers",
    foreignKeys = [
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index("questionId")
    ]
)
data class Answer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: Long, // 关联的问题ID
    val content: String, // 答案内容
    val imageUrl: String? = null, // 答案相关的图片URL，可为空
    val relatedQuestions: List<String> = emptyList(), // 相关问题列表
    val experiments: List<String> = emptyList(), // 推荐的小实验列表
    val games: List<String> = emptyList() // 推荐的互动小游戏列表
)