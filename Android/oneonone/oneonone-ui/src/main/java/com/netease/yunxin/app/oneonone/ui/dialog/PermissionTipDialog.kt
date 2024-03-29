/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.app.oneonone.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.LayoutRes
import com.netease.yunxin.kit.common.utils.SizeUtils
import com.netease.yunxin.nertc.ui.R

/**
 * 底部弹窗基类，子类需要实现 顶部view，以及底部view 的渲染即可
 */
class PermissionTipDialog(activity: Activity, private val clickListener: View.OnClickListener) :
    Dialog(
        activity,
        R.style.BottomDialogTheme
    ) {
    private var rootView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        if (window != null) {
            window.decorView.setPadding(SizeUtils.dp2px(20f), 0, SizeUtils.dp2px(20f), 0)
            val wlp = window.attributes
            wlp.gravity = Gravity.CENTER
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = wlp
        }
        setContentView(rootView)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }

    @LayoutRes
    private fun contentLayoutId(): Int {
        return R.layout.view_permission_tip_dialog
    }

    /**
     * 页面渲染
     */
    private fun renderRootView(rootView: View?) {
        if (rootView == null) {
            return
        }
        val button = rootView.findViewById<View>(R.id.tv_tip_ok)
        button.setOnClickListener {
            clickListener.onClick(it)
        }
    }

    override fun show() {
        if (isShowing) {
            return
        }
        renderRootView(rootView)
        try {
            super.show()
        } catch (ignored: Throwable) {
            ignored.printStackTrace()
        }
    }

    override fun dismiss() {
        if (!isShowing) {
            return
        }
        try {
            super.dismiss()
        } catch (ignored: Throwable) {
        }
    }

    init {
        rootView = LayoutInflater.from(context).inflate(contentLayoutId(), null)
    }
}
