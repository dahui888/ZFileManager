package com.zj.file.listener

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.zj.file.R
import com.zj.file.common.ZFileType
import com.zj.file.content.*
import com.zj.file.type.*
import com.zj.file.ui.ZFileListFragment
import com.zj.file.ui.ZFilePicActivity
import com.zj.file.ui.ZFileVideoPlayActivity
import com.zj.file.ui.dialog.ZFileAudioPlayDialog
import com.zj.file.ui.dialog.ZFileInfoDialog
import com.zj.file.ui.dialog.ZFileRenameDialog
import com.zj.file.ui.dialog.ZFileSelectFolderDialog
import com.zj.file.util.*
import com.zj.file.util.ZFileLog
import com.zj.file.util.ZFileOpenUtil
import com.zj.file.util.ZFileUtil
import java.io.File

/**
 * 图片或视频 显示
 */
abstract class ZFileImageListener {

    /**
     * 图片类型加载
     */
    abstract fun loadImage(imageView: ImageView, file: File)

    /**
     * 视频类型加载
     */
    open fun loadVideo(imageView: ImageView, file: File) {
        loadImage(imageView, file)
    }
}

/**
 * 文件选取 后 的监听
 */
interface ZFileSelectResultListener {

    fun selectResult(selectList: MutableList<ZFileBean>?)


}

/**
 * 完全自定义 获取文件数据
 */
interface ZFileLoadListener {

    /**
     * 获取手机里的文件List
     * @param filePath String           指定的文件目录访问，空为SD卡根目录
     * @return MutableList<ZFileBean>?  list
     */
    fun getFileList(context: Context?, filePath: String?): MutableList<ZFileBean>?
}

/**
 * 嵌套在 Fragment 中 使用
 * [FragmentActivity] 中 对于 [ZFileListFragment] 操作
 */
abstract class ZFragmentListener {

    /**
     * 文件选择
     */
    abstract fun selectResult(selectList: MutableList<ZFileBean>?)

    /**
     * [Activity] 中直接调用 [Activity.finish] 即可，如有需要，重写即可
     */
    open fun onActivityBackPressed(activity: FragmentActivity) {
        activity.finish()
    }

//    abstract fun onActivityBackPressed() 方法已弃用

    /**
     * 获取 [Manifest.permission.WRITE_EXTERNAL_STORAGE] 权限失败
     * @param activity [FragmentActivity]
     */
    open fun onSDPermissionsFiled(activity: FragmentActivity) {
        activity.showToast(activity getStringById R.string.zfile_permission_bad)
    }

    /**
     * 获取 [Environment.isExternalStorageManager] (所有的文件管理) 权限 失败
     * 请注意：Android 11 及以上版本 才有
     */
    open fun onExternalStorageManagerFiled(activity: FragmentActivity) {
        activity.showToast(activity getStringById R.string.zfile_11_bad)
    }
}

/**
 * 完全自定义 QQ、WeChat 获取
 */
abstract class ZQWFileLoadListener {

    /**
     * 获取标题
     * @return Array<String>
     */
    open fun getTitles(): Array<String>? = null

    /**
     * 获取过滤规则
     * @param fileType Int      文件类型 see [ZFILE_QW_PIC] [ZFILE_QW_MEDIA] [ZFILE_QW_DOCUMENT] [ZFILE_QW_OTHER]
     */
    abstract fun getFilterArray(fileType: Int): Array<String>

    /**
     * 获取 QQ 或 WeChat 文件路径
     * @param qwType String         QQ 或 WeChat  see [ZFileConfiguration.QQ] [ZFileConfiguration.WECHAT]
     * @param fileType Int          文件类型 see [ZFILE_QW_PIC] [ZFILE_QW_MEDIA] [ZFILE_QW_DOCUMENT] [ZFILE_QW_OTHER]
     * @return MutableList<String>  文件路径集合（因为QQ或WeChat保存的文件可能存在多个路径）
     */
    abstract fun getQWFilePathArray(qwType: String, fileType: Int): MutableList<String>

    /**
     * 获取数据
     * @param fileType Int                          文件类型 see [ZFILE_QW_PIC] [ZFILE_QW_MEDIA] [ZFILE_QW_DOCUMENT] [ZFILE_QW_OTHER]
     * @param qwFilePathArray MutableList<String>   QQ 或 WeChat 文件路径集合
     * @param filterArray Array<String>             过滤规则
     */
    abstract fun getQWFileDatas(
        fileType: Int,
        qwFilePathArray: MutableList<String>,
        filterArray: Array<String>
    ): MutableList<ZFileBean>

}

/**
 * 文件类型
 */
open class ZFileTypeListener {

    open fun getFileType(filePath: String): ZFileType {
        return when (ZFileHelp.getFileTypeBySuffix(filePath)) {
            PNG, JPG, JPEG, SVG, GIF -> ImageType()
            MP3, AAC, WAV, M4A -> AudioType()
            MP4, _3GP -> VideoType()
            TXT, XML, JSON -> TxtType()
            ZIP -> ZipType()
            DOC, DOCX -> WordType()
            XLS, XLSX -> XlsType()
            PPT, PPTX -> PptType()
            PDF -> PdfType()
            else -> OtherType()
        }
    }
}

/**
 * 打开文件
 */
open class ZFileOpenListener {

    /**
     * 打开音频
     */
    open fun openAudio(filePath: String, view: View) {
        (view.context as? AppCompatActivity)?.apply {
            val tag = "ZFileAudioPlayDialog"
            checkFragmentByTag(tag)
            ZFileAudioPlayDialog.getInstance(filePath).show(supportFragmentManager, tag)
        }
    }

    /**
     * 打开图片
     */
    open fun openImage(filePath: String, view: View) {
        val pic = view.findViewById<ImageView>(R.id.item_zfile_list_file_pic)
        pic.context.startActivity(
            Intent(pic.context, ZFilePicActivity::class.java).apply {
                putExtra("picFilePath", filePath)
            }, ActivityOptions.makeSceneTransitionAnimation(
                pic.context as Activity, pic,
                pic.context getStringById R.string.zfile_sharedElement_pic
            ).toBundle()
        )
    }

    /**
     * 打开视频
     */
    open fun openVideo(filePath: String, view: View) {
        val pic = view.findViewById<ImageView>(R.id.item_zfile_list_file_pic)
        pic.context.startActivity(
            Intent(pic.context, ZFileVideoPlayActivity::class.java).apply {
                putExtra("videoFilePath", filePath)
            }, ActivityOptions.makeSceneTransitionAnimation(
                pic.context as Activity, pic,
                pic.context getStringById R.string.zfile_sharedElement_video
            ).toBundle()
        )
    }

    /**
     * 打开Txt
     */
    open fun openTXT(filePath: String, view: View) {
        ZFileOpenUtil.openTXT(filePath, view)
    }

    /**
     * 打开zip
     */
    open fun openZIP(filePath: String, view: View) {
        val context = view.context
        AlertDialog.Builder(context, R.style.ZFile_Common_Dialog).apply {
            setTitle(context.getString(R.string.zfile_menu_selected))
            setItems(
                arrayOf(
                    context.getString(R.string.zfile_menu_open),
                    context.getString(R.string.zfile_menu_unzip)
                )
            ) { dialog, which ->
                if (which == 0) {
                    ZFileOpenUtil.openZIP(filePath, view)
                } else {
                    zipSelect(filePath, view)
                }
                dialog.dismiss()
            }
            setPositiveButton(R.string.zfile_cancel) { dialog, _ -> dialog.dismiss() }
            show()
        }
    }

    private fun zipSelect(filePath: String, view: View) {
        val activity = view.context
        if (activity is AppCompatActivity) {
            activity.checkFragmentByTag("ZFileSelectFolderDialog")
            val dialog =
                ZFileSelectFolderDialog.newInstance(activity.getString(R.string.zfile_menu_unzip))
            dialog.selectFolder = {
                getZFileHelp().getFileOperateListener().zipFile(filePath, this, activity) {
                    ZFileLog.i(if (this) "解压成功" else "解压失败")
                    val fragment = activity.supportFragmentManager.findFragmentByTag(
                        getZFileConfig().fragmentTag
                    )
                    if (fragment is ZFileListFragment) {
                        fragment.observer(this)
                    } else {
                        ZFileLog.e("文件解压成功，但是无法立刻刷新界面！")
                    }
                }
            }
            dialog.show(activity.supportFragmentManager, "ZFileSelectFolderDialog")
        }
    }

    /**
     * 打开word
     */
    open fun openDOC(filePath: String, view: View) {
        ZFileOpenUtil.openDOC(filePath, view)
    }

    /**
     * 打开表格
     */
    open fun openXLS(filePath: String, view: View) {
        ZFileOpenUtil.openXLS(filePath, view)
    }

    /**
     * 打开PPT
     */
    open fun openPPT(filePath: String, view: View) {
        ZFileOpenUtil.openPPT(filePath, view)
    }

    /**
     * 打开PDF
     */
    open fun openPDF(filePath: String, view: View) {
        ZFileOpenUtil.openPDF(filePath, view)
    }

    open fun openOther(filePath: String, view: View) {
        ZFileLog.e("【${filePath.getFileType()}】不支持预览该文件 ---> $filePath")
        ZFileOpenUtil.openAppStore(filePath, view.context)
    }
}

/**
 * 文件操作，耗时的文件操作建议放在 非 UI线程中
 */
open class ZFileOperateListener {

    /**
     * 文件重命名（该方式需要先弹出重命名弹窗或其他页面，再执行重命名逻辑）
     * @param filePath String   文件路径
     * @param context Context   Context
     * @param block Function2<Boolean, String, Unit> Boolean：成功或失败；String：新名字
     */
    open fun renameFile(
        filePath: String,
        context: Context,
        block: (Boolean, String) -> Unit
    ) {
        (context as? AppCompatActivity)?.let {
            it.checkFragmentByTag("ZFileRenameDialog")
            ZFileRenameDialog.newInstance(filePath.getFileNameOnly()).apply {
                reanameDown = {
                    renameFile(filePath, this, context, block)
                }
            }.show(it.supportFragmentManager, "ZFileRenameDialog")
        }
    }

    /**
     * 文件重命名（该方式只需要实现重命名逻辑即可）
     * @param filePath String       文件路径
     * @param fileNewName String    新名字
     * @param context Context       Context
     * @param block Function2<Boolean, String, Unit> Boolean：成功或失败；String：新名字
     */
    open fun renameFile(
        filePath: String,
        fileNewName: String,
        context: Context,
        block: (Boolean, String) -> Unit
    ) = ZFileUtil.renameFile(filePath, fileNewName, context, block)

    /**
     * 复制文件
     * @param sourceFile String     源文件地址
     * @param targetFile String     目标文件地址
     * @param context Context       Context
     */
    open fun copyFile(
        sourceFile: String,
        targetFile: String,
        context: Context,
        block: Boolean.() -> Unit
    ) = ZFileUtil.copyFile(sourceFile, targetFile, context, block)

    /**
     * 移动文件
     * @param sourceFile String     源文件地址
     * @param targetFile String     目标文件地址
     * @param context Context       Context
     */
    open fun moveFile(
        sourceFile: String,
        targetFile: String,
        context: Context,
        block: Boolean.() -> Unit
    ) = ZFileUtil.cutFile(sourceFile, targetFile, context, block)

    /**
     * 删除文件
     * @param filePath String   源文件地址
     */
    open fun deleteFile(filePath: String, context: Context, block: Boolean.() -> Unit) {
        context.commonDialog(
            title = R.string.zfile_11_title,
            content = R.string.zfile_dialog_delete_content,
            finish = R.string.zfile_dialog_delete,
            finishListener = {
                ZFileUtil.deleteFile(filePath, context, block)
            })
    }

    /**
     * 解压文件
     * 请注意，文件解压目前只支持压缩包里面只有一个文件的情况，多个暂不支持，如有需要，请自己实现
     * @param sourceFile String     源文件地址
     * @param targetFile String     目标文件地址
     */
    open fun zipFile(
        sourceFile: String,
        targetFile: String,
        context: Context,
        block: Boolean.() -> Unit
    ) {
        ZFileUtil.zipFile(sourceFile, targetFile, context, block)
    }

    /**
     * 文件详情
     */
    open fun fileInfo(bean: ZFileBean, context: Context) {
        val tag = ZFileInfoDialog::class.java.simpleName
        (context as? AppCompatActivity)?.apply {
            checkFragmentByTag(tag)
            ZFileInfoDialog.newInstance(bean).show(supportFragmentManager, tag)
        }

    }
}
