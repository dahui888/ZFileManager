package com.zj.file.util

import com.zj.file.common.ZFileTypeManage
import com.zj.file.content.getFileType
import com.zj.file.content.toFile
import java.io.File
import java.util.*

/**
 * 对外工具类
 */
object ZFileHelp {

    /**
     * 获取文件大小
     */
    @JvmStatic
    fun getFileSize(filePath: String) = ZFileOtherUtil.getFileSize(filePath.toFile().length())

    /**
     * 获取文件类型
     */
    @JvmStatic
    fun getFileType(filePath: String) = ZFileTypeManage.getTypeManager().getFileType(filePath)

    /**
     * 根据后缀文件类型
     */
    @JvmStatic
    fun getFileTypeBySuffix(filePath: String) = filePath.getFileType().lowercase(Locale.CHINA)

    /**
     * 获取文件 格式化后的Modified
     */
    @JvmStatic
    fun getFormatFileDate(file: File) = ZFileOtherUtil.getFormatFileDate(file.lastModified())

}