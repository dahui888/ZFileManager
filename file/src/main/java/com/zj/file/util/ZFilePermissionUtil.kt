package com.zj.file.util

import android.Manifest
import android.os.Build
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

internal object ZFilePermissionUtil {

    /** 读写SD卡权限  */
    const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val WRITE_EXTERNAL_CODE = 0x1001

    const val FM_CODE = 0x1002

    /**
     * 判断是否申请过权限
     * @param permissions   权限
     * @return true表示没有申请过
     */
    fun hasPermission(context: Context, vararg permissions: String) =
            permissions.any { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }

    /**
     * 请求权限
     * @param code  请求码
     * @param requestPermission 权限
     */
    fun requestPermission(fragmentOrActivity: Any, code: Int, vararg requestPermission: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (fragmentOrActivity) {
                is Activity -> ActivityCompat.requestPermissions(fragmentOrActivity, requestPermission, code)
                is Fragment -> fragmentOrActivity.requestPermissions(requestPermission, code)
            }

        }
    }

}