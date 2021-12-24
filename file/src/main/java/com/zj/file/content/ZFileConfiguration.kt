package com.zj.file.content

import android.os.Parcelable
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.zj.file.ui.ZFileListFragment
import com.zj.file.ui.ZFileVideoPlayer
import com.zj.file.async.ZFileStipulateAsync
import com.zj.file.listener.ZQWFileLoadListener
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

/**
 * 配置信息
 *
 * 1.3.3 主要更新信息：
 * 1）嵌套 Fragment 优化，列表优化，移除废弃方法！
 * 2）新增 [needTwiceClick] 属性
 * 3）新增 对于 m4a音频文件支持
 *
 * 1.3.2 主要更新信息：
 * 1）支持 直接在 Activity 、 Fragment、 Fragment + ViewPager 中使用
 * 2）文件复制优化
 *
 * 1.3.1 主要更新信息：
 * 1）新增 [qwData] QQ、Wechat配置信息，不需要通过自定义 [ZQWFileLoadListener] 即可实现
 * 2）修复 QQ、Wechat 部分路径下无法获取数据的bug
 *
 * 1.3.0 主要更新信息：
 * 1) Android 11 12 支持，完善 WPS 文件类型
 * 2) 新增 [titleGravity] [keepDuplicate] 配置
 * 3) 删除文件崩溃、解压缩中文乱码问题修复
 * 4) [ZFileVideoPlayer] internal ---> open, ZFileAsyncImpl 重命名为 [ZFileStipulateAsync]
 *
 */
class ZFileConfiguration : Serializable {

    companion object {

        /** QQ目录 */
        const val QQ = "ZFILE_QQ_FILE_PATH"
        /** 微信目录 */
        const val WECHAT = "ZFILE_WECHAT_FILE_PATH"

        /** 默认 */
        const val BY_DEFAULT = 0x1000
        /** 根据名字 */
        const val BY_NAME = 0x1001
        /** 根据最后修改时间 */
        const val BY_DATE = 0x1003
        /** 根据大小 */
        const val BY_SIZE = 0x1004

        /** 升序 */
        const val ASC = 0x2001
        /** 降序 */
        const val DESC = 0x2002

        /** 样式一 */
        const val STYLE1 = 1
        /** 样式二 */
        const val STYLE2 = 2

        const val RENAME = "重命名"
        const val COPY = "复制"
        const val MOVE = "移动"
        const val DELETE = "删除"
        const val INFO = "查看详情"

        /** 标题居左 */
        const val TITLE_LEFT = 0
        /** 标题居中 */
        const val TITLE_CENTER = 1
        /** 标题居右 */
        const val TITLE_RIGHT = 2
    }

    /**
     * 起始访问位置，空为SD卡根目录
     * 还可指定QQ或微信目录 see [QQ] [WECHAT]
     */
    var filePath: String? = null

    /**
     * QQ、Wechat 配置信息
     */
    var qwData = ZFileQWData()

    /**
     * 图片资源配置
     */
    var resources = ZFileResources()

    /**
     * 是否显示隐藏文件
     */
    var showHiddenFile = false

    /**
     * 根据什么排序 see [BY_DEFAULT] [BY_NAME] [BY_DATE] [BY_SIZE]
     */
    var sortordBy = BY_DEFAULT

    /**
     * 排序方式 see [ASC] [DESC]
     */
    var sortord = ASC

    /**
     * 过滤规则，默认显示所有的文件类型
     * 如 arrayOf(PNG, JPG, JPEG, GIF) 只显示图片类型
     */
    var fileFilterArray: Array<String>? = null

    /**
     * 文件选取大小的限制，单位：M
     */
    var maxSize = 10

    /**
     * 超过最大选择大小文字提醒
     */
    var maxSizeStr = "您只能选取小于${maxSize}M的文件"

    /**
     * 最大选取数量
     */
    var maxLength = 9

    /**
     * 超过最大选择数量文字提醒
     */
    var maxLengthStr = "您最多可以选取${maxLength}个文件"

    /**
     * 选中的样式 see [STYLE1] [STYLE2]
     */
    var boxStyle = STYLE2

    /**
     * 是否需要长按事件
     */
    var needLongClick = true

    /**
     * 默认只有文件才有长按事件
     * 长按暂不支持对于文件夹的操作，如有需要，请实现 [ZFileOperateListener]
     */
    var isOnlyFileHasLongClick = true

    /**
     * 长按后需要显示的操作类型 see [RENAME] [COPY] [MOVE] [DELETE] [INFO]
     * 空默认为 arrayOf(RENAME, COPY, MOVE, DELETE, INFO)
     * 目前只可以是这几种类型，个数、顺序可以自定义，文字不支持自定义
     */
    var longClickOperateTitles: Array<String>? = null

    /**
     * 是否只需要显示文件夹
     * 慎用！！！
     */
    var isOnlyFolder = false

    /**
     * 是否只需要显示文件
     * 慎用！！！
     */
    var isOnlyFile = false

    /**
     * 打开文件需要 [FileProvider] 一般都是包名 + xxxFileProvider
     * 如果项目中已经存在或其他原因无法修改，请自己实现 [ZFileOpenListener]
     */
    var authority = ""

    /**
     * 是否需要显示 已选择的文件个数 提示
     */
    var showSelectedCountHint = false

    /**
     * 标题位置 see [TITLE_LEFT] [TITLE_CENTER]
     * 设置标题 重写 [R.string.zfile_title] 即可自定义
     */
    var titleGravity = TITLE_LEFT
        set(value) {
            if (value in TITLE_LEFT..TITLE_RIGHT) {
                field = value
            } else {
                throwError("titleGravity")
            }
        }

    /**
     * 复制、移动、重命名、解压缩 操作 如果存在相同的文件时 是否需要保留副本
     * false：默认直接覆盖或者移除； true：表示保留原文件，同时新增副本文件
     */
    var keepDuplicate = false

    /**
     * 是否开启懒加载
     * 嵌套在 VP + Fragment 使用
     */
    var needLazy = true

    /**
     * Fragment TAG，可以通过 [FragmentManager.findFragmentByTag] 获取 [ZFileListFragment]
     * 嵌套在 VP + Fragment 使用，see [FragmentPagerAdapter.makeFragmentName]
     */
    var fragmentTag = ZFILE_FRAGMENT_TAG

    /**
     * 是否显示 返回按钮图标
     */
    var showBackIcon = true

    /**
     * 是否需要两次点击后才能选择文件
     */
    var needTwiceClick = true

    /**
     * 是否显示日志
     */
    var showLog = true

    /**
     * 方便java同学调用
     */
    class Build {

        private var filePath: String? = null
        private var qwData = ZFileQWData()
        private var resources = ZFileResources()
        private var showHiddenFile = false
        private var sortordBy = BY_DEFAULT
        private var sortord = ASC
        private var fileFilterArray: Array<String>? = null
        private var maxSize = 10
        private var maxSizeStr = "您只能选取小于${maxSize}M的文件"
        private var maxLength = 9
        private var maxLengthStr = "您最多可以选取${maxLength}个文件"
        private var boxStyle = STYLE1
        private var needLongClick = true
        private var isOnlyFileHasLongClick = true
        private var longClickOperateTitles: Array<String>? = null
        private var isOnlyFolder = false
        private var isOnlyFile = false
        private var authority = ""
        private var showSelectedCountHint = false
        private var titleGravity = TITLE_LEFT
        private var keepDuplicate = false
        private var needLazy = true
        private var showBackIcon = true
        private var needTwiceClick = true
        private var showLog = true

        fun filePath(filePath: String?): Build {
            this.filePath = filePath
            return this
        }

        fun qwData(qwData: ZFileQWData): Build {
            this.qwData = qwData
            return this
        }

        fun resources(resources: ZFileResources): Build {
            this.resources = resources
            return this
        }

        fun showHiddenFile(showHiddenFile: Boolean): Build {
            this.showHiddenFile = showHiddenFile
            return this
        }

        fun sortordBy(sortordBy: Int): Build {
            this.sortordBy = sortordBy
            return this
        }

        fun sortord(sortord: Int): Build {
            this.sortord = sortord
            return this
        }

        fun fileFilterArray(fileFilterArray: Array<String>?): Build {
            this.fileFilterArray = fileFilterArray
            return this
        }

        fun maxSize(maxSize: Int): Build {
            this.maxSize = maxSize
            return this
        }

        fun maxSizeStr(maxSizeStr: String): Build {
            this.maxSizeStr = maxSizeStr
            return this
        }

        fun maxLength(maxLength: Int): Build {
            this.maxLength = maxLength
            return this
        }

        fun maxLengthStr(maxLengthStr: String): Build {
            this.maxLengthStr = maxLengthStr
            return this
        }

        fun boxStyle(boxStyle: Int): Build {
            this.boxStyle = boxStyle
            return this
        }

        fun needLongClick(needLongClick: Boolean): Build {
            this.needLongClick = needLongClick
            return this
        }

        fun isOnlyFileHasLongClick(isOnlyFileHasLongClick: Boolean): Build {
            this.isOnlyFileHasLongClick = isOnlyFileHasLongClick
            return this
        }

        fun longClickOperateTitles(longClickOperateTitles: Array<String>?): Build {
            this.longClickOperateTitles = longClickOperateTitles
            return this
        }

        fun isOnlyFolder(isOnlyFolder: Boolean): Build {
            this.isOnlyFolder = isOnlyFolder
            return this
        }

        fun isOnlyFile(isOnlyFile: Boolean): Build {
            this.isOnlyFile = isOnlyFile
            return this
        }

        fun authority(authority: String): Build {
            this.authority = authority
            return this
        }

        fun showSelectedCountHint(showSelectedCountHint: Boolean): Build {
            this.showSelectedCountHint = showSelectedCountHint
            return this
        }

        fun titleGravity(titleGravity: Int): Build {
            if (titleGravity in TITLE_LEFT..TITLE_RIGHT) {
                this.titleGravity = titleGravity
            } else {
                throwError("boxStyle")
            }
            return this
        }

        fun keepDuplicate(keepDuplicate: Boolean): Build {
            this.keepDuplicate = keepDuplicate
            return this
        }

        fun needLazy(needLazy: Boolean): Build {
            this.needLazy = needLazy
            return this
        }

        fun showBackIcon(showBackIcon: Boolean): Build {
            this.showBackIcon = showBackIcon
            return this
        }

        fun needTwiceClick(needTwiceClick: Boolean): Build {
            this.needTwiceClick = needTwiceClick
            return this
        }

        fun showLog(showLog: Boolean): Build {
            this.showLog = showLog
            return this
        }

        fun build() = ZFileConfiguration().apply {
            this.filePath = this@Build.filePath
            this.qwData = this@Build.qwData
            this.resources = this@Build.resources
            this.showHiddenFile = this@Build.showHiddenFile
            this.sortordBy = this@Build.sortordBy
            this.sortord = this@Build.sortord
            this.fileFilterArray = this@Build.fileFilterArray
            this.maxSize = this@Build.maxSize
            this.maxSizeStr = this@Build.maxSizeStr
            this.maxLength = this@Build.maxLength
            this.maxLengthStr = this@Build.maxLengthStr
            this.boxStyle = this@Build.boxStyle
            this.needLongClick = this@Build.needLongClick
            this.isOnlyFileHasLongClick = this@Build.isOnlyFileHasLongClick
            this.longClickOperateTitles = this@Build.longClickOperateTitles
            this.isOnlyFolder = this@Build.isOnlyFolder
            this.isOnlyFile = this@Build.isOnlyFile
            this.authority = this@Build.authority
            this.showSelectedCountHint = this@Build.showSelectedCountHint
            this.titleGravity = this@Build.titleGravity
            this.keepDuplicate = this@Build.keepDuplicate
            this.needLazy = this@Build.needLazy
            this.showBackIcon = this@Build.showBackIcon
            this.needTwiceClick = this@Build.needTwiceClick
            this.showLog = this@Build.showLog
        }

    }

    /**
     * 图片相关资源配置 设置 [ZFILE_DEFAULT] 将使用默认资源 各种文件类型的图片 建议 128 * 128
     * @property audioRes Int        音频
     * @property txtRes Int          文本
     * @property pdfRes Int          PDF
     * @property pptRes Int          PPT
     * @property wordRes Int         Word
     * @property excelRes Int        Excel
     * @property zipRes Int          ZIP
     * @property otherRes Int        其他类型
     * @property emptyRes Int        空资源
     * @property folderRes Int       文件夹
     * @property lineColor Int       列表分割线颜色
     */
    @Parcelize
    data class ZFileResources @JvmOverloads constructor(
        var audioRes: Int = ZFILE_DEFAULT,
        var txtRes: Int = ZFILE_DEFAULT,
        var pdfRes: Int = ZFILE_DEFAULT,
        var pptRes: Int = ZFILE_DEFAULT,
        var wordRes: Int = ZFILE_DEFAULT,
        var excelRes: Int = ZFILE_DEFAULT,
        var zipRes: Int = ZFILE_DEFAULT,
        var otherRes: Int = ZFILE_DEFAULT,
        var emptyRes: Int = ZFILE_DEFAULT,
        var folderRes: Int = ZFILE_DEFAULT,
        var lineColor: Int = ZFILE_DEFAULT
    ) : Serializable, Parcelable

}


