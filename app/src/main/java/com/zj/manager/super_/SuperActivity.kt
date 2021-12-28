package com.zj.manager.super_

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zj.file.async.ZFileStipulateAsync
import com.zj.file.content.*
import com.zj.file.dsl.config
import com.zj.file.dsl.result
import com.zj.file.dsl.zfile
import com.zj.manager.JavaSampleActivity
import com.zj.manager.R
import com.zj.manager.content.Content
import com.zj.manager.content.Content.FILTER
import com.zj.manager.content.Content.QQ_MAP
import com.zj.manager.content.Content.TITLES
import com.zj.manager.diy.SunActivity
import com.zj.manager.dsl.DslActivity
import com.zj.manager.fm.FragmentSampleActivity2
import kotlinx.android.synthetic.main.activity_super.*

class SuperActivity : AppCompatActivity() {

    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarTransparent()
        setAndroidNativeLightStatusBar()
        setContentView(R.layout.activity_super)
        dialog = ProgressDialog(this).run {
            setMessage("获取中，请稍后...")
            setCancelable(false)
            this
        }

        super_picTxt.setOnClickListener {
            showDialog(arrayOf(PNG, JPEG, JPG, GIF))
        }

        super_videoTxt.setOnClickListener {
            showDialog(arrayOf(MP4, _3GP))
        }

        super_audioTxt.setOnClickListener {
            showDialog(arrayOf(MP3, AAC, WAV, M4A))
        }

        super_fileTxt.setOnClickListener {
            showDialog(arrayOf(TXT, JSON, XML))
        }

        super_wpsTxt.setOnClickListener {
            showDialog(arrayOf(DOC, DOCX, XLS, XLSX, PPT, PPTX, PDF))
        }

        super_apkTxt.setOnClickListener {
            showDialog(arrayOf("apk"))
        }

        super_fragment.setOnClickListener {
            FragmentSampleActivity2.jump(this, 2)
        }

        super_java.setOnClickListener {
            startActivity(Intent(this, JavaSampleActivity::class.java))
        }

        super_dsl.setOnClickListener {
            startActivity(Intent(this, DslActivity::class.java))
        }

        super_qqTxt.setOnClickListener {
            toQW(ZFileConfiguration.QQ)
        }

        super_wechatTxt.setOnClickListener {
            toQW(ZFileConfiguration.WECHAT)
        }

        super_otherTxt.setOnClickListener {
            Toast.makeText(this, "我只是一个占位格，好看的", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SunActivity::class.java))
        }

        super_innerTxt.setOnClickListener {
            zfile {
                config {
                    getZFileConfig().apply {
                        boxStyle = ZFileConfiguration.STYLE2
                        maxLength = 6
                        titleGravity = ZFileConfiguration.TITLE_CENTER
                        maxLengthStr = "老铁最多6个文件"
                        authority = Content.AUTHORITY
                    }
                }
                result { setResultData(this) }
            }
        }
    }

    private fun toQW(path: String) {
        Log.e("ZFileManager", "请注意：QQ、微信目前只能获取用户手动保存到手机里面的文件，" +
                "且保存文件到手机的目录用户没有修改")
        Log.i("ZFileManager", "参考自腾讯自己的\"腾讯文件\"App，能力有限，部分文件无法获取")
        jump(path)
    }

    private fun jump(path: String) {
        getZFileHelp().setConfiguration(getZFileConfig().apply {
            boxStyle = ZFileConfiguration.STYLE2
            filePath = path
            authority = Content.AUTHORITY
            if (path == ZFileConfiguration.QQ) { // 打开QQ， 模拟自定义获取
                qwData = ZFileQWData().apply {
                    titles = TITLES
                    filterArrayMap = FILTER
                    qqFilePathArrayMap = QQ_MAP
                }
            } else {
                qwData = ZFileQWData()
            }
        }).result(this) {
            setResultData(this)
        }
    }

    private fun showDialog(filterArray: Array<String>) {
        dialog?.show()
        ZFileStipulateAsync(this) {
            dialog?.dismiss()
            if (isNullOrEmpty()) {
                Toast.makeText(this@SuperActivity, "暂无数据", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@SuperActivity, "共找到${size}条数据", Toast.LENGTH_SHORT).show()
                Log.i("ZFileManager", "共找到${this.size}条数据")
                if (this.size > 100) {
                    Log.e("ZFileManager", "这里考虑到传值大小限制，截取前100条数据")
                    SuperDialog.newInstance(changeList(this))
                            .show(supportFragmentManager, "SuperDialog")
                } else {
                    SuperDialog.newInstance(this as ArrayList<ZFileBean>)
                            .show(supportFragmentManager, "SuperDialog")
                }
            }
        }.start(filterArray)
    }

    private fun changeList(oldList: MutableList<ZFileBean>): ArrayList<ZFileBean> {
        val list = ArrayList<ZFileBean>()
        var index = 1
        oldList.forEach forEach@{
            if (index >= 100) {
                return@forEach
            }
            list.add(it)
            index++
        }
        return list
    }

    private fun setResultData(list: MutableList<ZFileBean>?) {
        val sb = StringBuilder()
        list?.forEach {
            sb.append(it).append("\n\n")
        }
        super_resultTxt.text = sb.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 这里重置，防止该页面销毁后其他演示页面无法正常获取数据！
        getZFileHelp().resetAll()
    }
}
