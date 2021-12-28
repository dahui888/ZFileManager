package com.zj.file.content

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.zj.file.R
import com.zj.file.async.ZFileStipulateAsync
import com.zj.file.common.ZFileManageDialog
import com.zj.file.common.ZFileManageHelp
import com.zj.file.util.ZFileLog
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

const val PNG = "png"
const val JPG = "jpg"
const val JPEG = "jpeg"
const val GIF = "gif"
const val MP3 = "mp3"
const val AAC = "aac"
const val WAV = "wav"
const val M4A = "m4a"
const val MP4 = "mp4"
const val _3GP = "3gp"
const val TXT = "txt"
const val XML = "xml"
const val JSON = "json"
const val DOC = "doc"
const val DOCX = "docx"
const val XLS = "xls"
const val XLSX = "xlsx"
const val PPT = "ppt"
const val PPTX = "pptx"
const val PDF = "pdf"
const val ZIP = "zip"

/** 默认资源 */
const val ZFILE_DEFAULT = -1

/** 图片 */
const val ZFILE_QW_PIC = 0
/** 媒体 */
const val ZFILE_QW_MEDIA = 1
/** 文档 */
const val ZFILE_QW_DOCUMENT = 2
/** 其他 (过滤规则可以使用 "" 代替) */
const val ZFILE_QW_OTHER = 3

/** onActivityResult requestCode */
const val ZFILE_REQUEST_CODE = 0x1000
/** onActivityResult resultCode */
const val ZFILE_RESULT_CODE = 0x1001
/**
 * onActivityResult data key  --->>>
 * val list = data?.getParcelableArrayListExtra<[ZFileBean]>([ZFILE_SELECT_DATA_KEY])
 */
const val ZFILE_SELECT_DATA_KEY = "ZFILE_SELECT_RESULT_DATA"

fun getZFileHelp() = ZFileManageHelp.getInstance()
fun getZFileConfig() = getZFileHelp().getConfiguration()

@Deprecated("请使用ZFileStipulateAsync替代")
typealias ZFileAsyncImpl = ZFileStipulateAsync

// 下面属性、方法暂不对外开放 =======================================================================

internal const val ZFILE_FRAGMENT_TAG = "ZFileListFragment"

internal const val QW_SIZE = 4

internal const val COPY_TYPE = 0x2001
internal const val CUT_TYPE = 0x2002
internal const val DELTE_TYPE = 0x2003
internal const val ZIP_TYPE = 0x2004

internal const val FILE = 0
internal const val FOLDER = 1

internal const val QQ_PIC = "/storage/emulated/0/tencent/QQ_Images/" // 保存的图片
internal const val QQ_PIC_MOVIE = "/storage/emulated/0/Pictures/QQ/" // 保存的图片和视频
// 保存的文档（未保存到手机的图片和视频也在这个位置）
internal const val QQ_DOWLOAD1 = "/storage/emulated/0/Android/data/com.tencent.mobileqq/Tencent/QQfile_recv/"
internal const val QQ_DOWLOAD2 = "/storage/emulated/0/Android/data/com.tencent.mobileqq/Tencent/QQ_business/"

internal const val WECHAT_FILE_PATH = "/storage/emulated/0/tencent/MicroMsg/"
internal const val WECHAT_PHOTO_VIDEO = "WeiXin/" // 图片、视频保存位置
internal const val WECHAT_DOWLOAD = "Download/" // 其他文件保存位置

internal const val LOG_TAG = "ZFileManager"
internal const val ERROR_MSG = "fragmentOrActivity is not Activity or Fragment"
internal const val QW_FILE_TYPE_KEY = "QW_fileType"
internal const val FILE_START_PATH_KEY = "fileStartPath"

internal fun Context.getStatusBarHeight() = getSystemHeight("status_bar_height")
internal fun Context.getSystemHeight(name: String, defType: String = "dimen") =
    resources.getDimensionPixelSize(
        resources.getIdentifier(name, defType, "android")
    )

internal fun Context.getZDisplay() = IntArray(2).apply {
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    manager.defaultDisplay.getSize(point)
    this[0] = point.x
    this[1] = point.y
}
internal infix fun FragmentActivity.checkFragmentByTag(tag: String) {
    val fragment = supportFragmentManager.findFragmentByTag(tag)
    if (fragment != null) {
        supportFragmentManager.beginTransaction().remove(fragment).commit()
    }
}

fun Activity.setStatusBarTransparent() {
    val decorView = window.decorView
    val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    decorView.systemUiVisibility = option
    window.statusBarColor = Color.TRANSPARENT
}

/**
 * 隐藏ime
 */
fun View.hideIme() {
    clearFocus()
    val controller = ViewCompat.getWindowInsetsController(this)
    controller?.hide(WindowInsetsCompat.Type.ime())
}

/**
 * 显示ime
 */
fun View.showIme() {
    requestFocus()
    val controller = ViewCompat.getWindowInsetsController(this)
    controller?.show(WindowInsetsCompat.Type.ime())
}

/**
 * 状态栏反色
 */
fun Activity.setAndroidNativeLightStatusBar() {
    val controller = ViewCompat.getWindowInsetsController(window.decorView)
    controller?.isAppearanceLightStatusBars = !isDarkMode()
}

/**
 * 获取当前是否为深色模式
 * 深色模式的值为:0x21
 * 浅色模式的值为:0x11
 * @return true 为是深色模式   false为不是深色模式
 */
fun Context.isDarkMode(): Boolean {
    return resources.configuration.uiMode == 0x21
}

internal fun ZFileManageDialog.setNeedWH() {
    val display = context?.getZDisplay()
    val width = if (display?.isEmpty() == true) ViewGroup.LayoutParams.MATCH_PARENT else (display!![0] * 0.88f).toInt()
    dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
}
internal fun SwipeRefreshLayout.property(
    color: Int = R.color.zfile_base_color,
    scale: Boolean = false,
    height: Int = 0,
    block: () -> Unit
): SwipeRefreshLayout {
    setColorSchemeColors(context getColorById color)
    if (scale) setProgressViewEndTarget(scale, height)
    setOnRefreshListener(block)
    return this
}
fun View.onSafeClick(time: Long = 600L, block: View.() -> Unit) {
    triggerDelay = time
    setOnClickListener {
        if (clickEnable()) block(it) else ZFileLog.e("点击间隔少于${time}ms，本次点击不做任何处理")
    }
}
private var View.triggerLastTime: Long
    get() = getTag(1123460103) as? Long ?: -601L
    set(value) {
        setTag(1123460103, value)
    }

private var View.triggerDelay: Long
    get() = getTag(1123461123) as? Long ?: 600L
    set(value) {
        setTag(1123461123, value)
    }

private fun View.clickEnable(): Boolean {
    var flag = false
    val currentClickTime = System.currentTimeMillis()
    if (currentClickTime - triggerLastTime >= triggerDelay) {
        flag = true
    }
    triggerLastTime = currentClickTime
    return flag
}

internal infix fun Context.dip2pxF(dpValue: Float) = dpValue * resources.displayMetrics.density + 0.5f
internal infix fun Context.dip2px(dpValue: Float) = dip2pxF(dpValue).toInt()
internal infix fun Context.getColorById(colorID: Int) = ContextCompat.getColor(this, colorID)
internal infix fun Context.getStringById(stringID: Int) = resources.getString(stringID)
internal infix fun <E> Set<E>.indexOf(value: String): Boolean {
    var flag = false
    forEach forEach@{
        if ((it?.toString()?.indexOf(value) ?: -1) >= 0) {
            flag = true
            return@forEach
        }
    }
    return flag
}
internal fun File.getFileType() = this.path.getFileType()
internal fun String.getFileType() = this.run {
    substring(lastIndexOf(".") + 1, length)
}
internal infix fun String.accept(type: String) =
    this.endsWith(type.lowercase(Locale.CHINA)) || this.endsWith(type.uppercase(Locale.CHINA))
internal fun String.getFileName() = File(this).name
internal fun String.getFileNameOnly() = getFileName().run {
    substring(0, lastIndexOf("."))
}
internal fun String.toFile() = File(this)
internal fun String?.isNull() =
    if (this == null || this.isNullOrEmpty()) true else this.replace(" ".toRegex(), "").isEmpty()
internal fun ZFileBean.toPathBean() = ZFilePathBean().apply {
    fileName = this@toPathBean.fileName
    filePath = this@toPathBean.filePath
}
internal infix fun ZFileBean.toQWBean(isSelected: Boolean) = ZFileQWBean(this, isSelected)
internal fun File.toPathBean() = ZFilePathBean().apply {
    fileName = this@toPathBean.name
    filePath = this@toPathBean.path
}
internal fun ArrayMap<String, ZFileBean>.toFileList(): MutableList<ZFileBean> {
    val list = ArrayList<ZFileBean>()
    for ((_, v) in this) {
        list.add(v)
    }
    return list
}
internal fun throwError(title: String) {
    ZFileException.throwConfigurationError(title)
}
internal val SD_ROOT: String
    get() {
        return Environment.getExternalStorageDirectory().path
    }
internal val folderRes: Int
    get() {
        return if (getZFileConfig().resources.folderRes == ZFILE_DEFAULT) R.drawable.ic_zfile_folder
        else getZFileConfig().resources.folderRes
    }
internal val lineColor: Int
    get() {
        return if (getZFileConfig().resources.lineColor == ZFILE_DEFAULT) R.color.zfile_line_color
        else getZFileConfig().resources.lineColor
    }








