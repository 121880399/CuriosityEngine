package org.zzy.curiosityengine.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.zzy.curiosityengine.data.dao.AnswerDao
import org.zzy.curiosityengine.data.dao.QuestionDao
import org.zzy.curiosityengine.data.model.Answer
import org.zzy.curiosityengine.data.model.Converters
import org.zzy.curiosityengine.data.model.Question

/**
 * 应用数据库类
 * 使用Room框架管理本地SQLite数据库
 */
@Database(
    entities = [Question::class, Answer::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * 获取问题DAO
     */
    abstract fun questionDao(): QuestionDao
    
    /**
     * 获取答案DAO
     */
    abstract fun answerDao(): AnswerDao
    
    companion object {
        // 单例模式
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * 获取数据库实例
         * @param context 应用上下文
         * @return 数据库实例
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "curiosity_engine_database"
                )
                    .fallbackToDestructiveMigration() // 数据库版本变化时重建数据库
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}