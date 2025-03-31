package org.zzy.curiosityengine.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * 类型转换器
 * 用于Room数据库中存储和检索复杂类型
 */
class Converters {
    private val gson = Gson()
    
    /**
     * 将Date转换为Long存储
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * 将Long转换为Date
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
    
    /**
     * 将字符串列表转换为JSON字符串存储
     */
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.let { gson.toJson(it) }
    }
    
    /**
     * 将JSON字符串转换为字符串列表
     */
    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrEmpty()) {
            return emptyList()
        }
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }
}