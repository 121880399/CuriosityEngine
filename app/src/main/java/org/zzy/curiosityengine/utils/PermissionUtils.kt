package org.zzy.curiosityengine.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * 权限工具类
 * 用于处理应用程序的权限请求
 */
class PermissionUtils {
    companion object {
        // 权限请求码
        const val RECORD_AUDIO_PERMISSION_CODE = 1001
        
        /**
         * 检查是否有录音权限
         * @param context 上下文
         * @return 是否有录音权限
         */
        fun hasAudioPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                // Android 6.0以下版本在安装时已经授予权限
                true
            }
        }
        
        /**
         * 请求录音权限（使用传统方式）
         * @param activity Activity实例
         */
        fun requestAudioPermission(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_AUDIO_PERMISSION_CODE
                )
            }
        }
        
        /**
         * 注册权限请求回调（使用ActivityResult API）
         * @param activity ComponentActivity实例
         * @param onPermissionGranted 权限授予时的回调
         * @param onPermissionDenied 权限拒绝时的回调
         * @return ActivityResultLauncher实例
         */
        fun registerForAudioPermissionResult(
            activity: ComponentActivity,
            onPermissionGranted: () -> Unit,
            onPermissionDenied: () -> Unit
        ): ActivityResultLauncher<String> {
            return activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    // 权限被授予
                    Toast.makeText(
                        activity,
                        "麦克风权限已授予，现在可以使用语音功能",
                        Toast.LENGTH_SHORT
                    ).show()
                    onPermissionGranted()
                } else {
                    // 权限被拒绝
                    Toast.makeText(
                        activity,
                        "麦克风权限被拒绝，语音功能将无法使用",
                        Toast.LENGTH_SHORT
                    ).show()
                    onPermissionDenied()
                }
            }
        }
        
        /**
         * 检查并请求录音权限（使用ActivityResult API）
         * @param activity ComponentActivity实例
         * @param permissionLauncher 权限请求启动器
         * @param onPermissionGranted 权限已授予时的回调
         */
        fun checkAndRequestAudioPermission(
            activity: ComponentActivity,
            permissionLauncher: ActivityResultLauncher<String>,
            onPermissionGranted: () -> Unit
        ) {
            if (hasAudioPermission(activity)) {
                // 已经有权限，直接执行操作
                onPermissionGranted()
            } else {
                // 请求权限
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
        
        /**
         * 设置权限请求回调
         * 用于在使用预先注册的权限启动器时动态设置回调函数
         * @param activity ComponentActivity实例
         * @param permissionLauncher 权限请求启动器
         * @param onPermissionGranted 权限授予时的回调
         * @param onPermissionDenied 权限拒绝时的回调
         */
        fun setPermissionCallbacks(
            activity: ComponentActivity,
            permissionLauncher: ActivityResultLauncher<String>,
            onPermissionGranted: () -> Unit,
            onPermissionDenied: () -> Unit = {}
        ) {
            // 在这里我们不能直接设置回调，因为ActivityResultLauncher的回调是在注册时设置的
            // 但我们可以在检查权限时使用传入的回调
            if (hasAudioPermission(activity)) {
                // 已有权限，直接调用授权回调
                onPermissionGranted()
            } else {
                // 请求权限，结果将由MainActivity中注册的回调处理
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}