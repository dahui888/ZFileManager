package com.zj.manager.dsl

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.zj.manager.R
import com.zj.manager.content.Content
import com.zj.file.content.ZFileBean
import com.zj.file.dsl.config
import com.zj.file.dsl.result
import com.zj.file.dsl.zfile
import kotlinx.android.synthetic.main.activity_dsl.*
import kotlinx.android.synthetic.main.layout_result_txt.*

class DslActivity : AppCompatActivity() {

    private var anim: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dsl)
        anim = getAnim()
        anim?.start()
        dsl_startBtn.setOnClickListener {
            dsl()
        }
        dsl_fragmentBtn.setOnClickListener {
            startActivity(Intent(this, DslFragmentActivity::class.java))
        }
    }

    private fun dsl() {
        zfile {
            config {
                Content.CONFIG
            }
            result {
                setResultData(this)
            }
        }
    }

    private fun setResultData(selectList: MutableList<ZFileBean>?) {
        val sb = StringBuilder()
        selectList?.forEach {
            sb.append(it).append("\n\n")
        }
        main_resultTxt.text = sb.toString()
    }

    override fun onDestroy() {
        anim?.apply {
            end()
            cancel()
            removeAllListeners()
            removeAllUpdateListeners()
        }
        anim = null
        super.onDestroy()
    }

    private fun getAnim() = ObjectAnimator.ofFloat(dsl_dslTxt, "rotation", 0f, 360f).run {
        duration = 5000L
        repeatCount = -1
        interpolator = LinearInterpolator()
        this
    }
}