package org.zzy.curiosityengine.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * API服务类
 * 负责创建和提供API接口实例
 */
object ApiService {
    private const val BASE_URL = "https://api.deepseek.com"
    
    /**
     * 创建OkHttpClient实例
     * 配置日志拦截器和其他设置
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            android.util.Log.d("ApiService", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                android.util.Log.d("ApiService", "发送请求: ${request.method} ${request.url}")
                val startTime = System.currentTimeMillis()
                
                try {
                    val response = chain.proceed(request)
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    android.util.Log.d("ApiService", "收到响应: ${response.code} (${duration}ms) ${request.url}")
                    response
                } catch (e: Exception) {
                    android.util.Log.e("ApiService", "请求失败: ${e.message}", e)
                    throw e
                }
            }
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * 创建Retrofit实例
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * 创建DeepSeekApi接口实例
     */
    val deepSeekApi: DeepSeekApi by lazy {
        retrofit.create(DeepSeekApi::class.java)
    }
}