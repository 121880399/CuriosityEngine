package org.zzy.curiosityengine.data.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 语音识别服务
 * 负责处理语音输入功能，优化性能和错误处理
 */
class VoiceRecognitionService(private val context: Context) {
    
    // 语音识别器
    private var speechRecognizer: SpeechRecognizer? = null
    
    // 识别状态
    private val _recognitionState = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState.asStateFlow()
    
    // 识别结果
    private val _recognitionResult = MutableStateFlow<String?>(null)
    val recognitionResult: StateFlow<String?> = _recognitionResult.asStateFlow()
    
    // 重试计数器
    private var retryCount = 0
    private val maxRetries = 3
    
    /**
     * 初始化语音识别器
     */
    fun initialize() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
        } else {
            _recognitionState.value = RecognitionState.Error("语音识别功能不可用")
        }
    }
    
    /**
     * 开始语音识别
     * 优化配置和错误处理，支持自动重试
     */
    fun startListening() {
        if (speechRecognizer == null) {
            initialize()
        }
        
        // 重置重试计数
        retryCount = 0
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3) // 增加结果数量以提高准确性
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // 添加额外配置以提高识别质量
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 800L)
        }
        
        try {
            _recognitionState.value = RecognitionState.Listening
            _recognitionResult.value = null
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _recognitionState.value = RecognitionState.Error(e.message ?: "启动语音识别失败")
        }
    }
    
    /**
     * 停止语音识别
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        _recognitionState.value = RecognitionState.Idle
    }
    
    /**
     * 释放资源
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
    
    /**
     * 创建识别监听器
     */
    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _recognitionState.value = RecognitionState.Listening
            }
            
            override fun onBeginningOfSpeech() {
                _recognitionState.value = RecognitionState.Listening
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // 可以用来显示音量变化
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // 接收语音数据缓冲区
            }
            
            override fun onEndOfSpeech() {
                _recognitionState.value = RecognitionState.Processing
            }
            
            override fun onError(error: Int) {
                // 某些错误可以尝试自动重试
                val canRetry = error == SpeechRecognizer.ERROR_NETWORK ||
                        error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT ||
                        error == SpeechRecognizer.ERROR_SERVER ||
                        error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY
                
                if (canRetry && retryCount < maxRetries) {
                    retryCount++
                    // 重新初始化识别器
                    speechRecognizer?.destroy()
                    speechRecognizer = null
                    initialize()
                    // 延迟500ms后重试
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        startListening()
                    }, 500)
                    
                    _recognitionState.value = RecognitionState.Listening
                    _recognitionResult.value = "正在重试...(${retryCount}/${maxRetries})"
                } else {
                    // 错误处理，提供更友好的错误信息
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "无法录制音频，请检查麦克风权限"
                        SpeechRecognizer.ERROR_CLIENT -> "客户端错误，请重试"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "缺少录音权限，请在设置中授予应用录音权限"
                        SpeechRecognizer.ERROR_NETWORK -> "网络连接错误，请检查网络设置"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络连接超时，请稍后重试"
                        SpeechRecognizer.ERROR_NO_MATCH -> "未能识别您的语音，请说得更清晰些"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "语音识别服务正忙，请稍后重试"
                        SpeechRecognizer.ERROR_SERVER -> "服务器错误，请稍后重试"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "未检测到语音输入，请靠近麦克风说话"
                        else -> "未知错误 (错误代码: $error)，请重试"
                    }
                    _recognitionState.value = RecognitionState.Error(errorMessage)
                }
            }
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    _recognitionResult.value = recognizedText
                    _recognitionState.value = RecognitionState.Success
                } else {
                    _recognitionState.value = RecognitionState.Error("未能识别语音")
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    _recognitionResult.value = recognizedText
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // 处理其他事件
            }
        }
    }
    
    /**
     * 语音识别状态
     */
    sealed class RecognitionState {
        object Idle : RecognitionState()
        object Listening : RecognitionState()
        object Processing : RecognitionState()
        object Success : RecognitionState()
        data class Error(val message: String) : RecognitionState()
    }
}