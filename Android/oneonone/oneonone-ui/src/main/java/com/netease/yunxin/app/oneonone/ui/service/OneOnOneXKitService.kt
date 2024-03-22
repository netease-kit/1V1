/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.app.oneonone.ui.service

import android.content.Context
import androidx.annotation.Keep
import com.netease.yunxin.app.oneonone.ui.utils.CallKitUtil
import com.netease.yunxin.kit.corekit.BuildConfig
import com.netease.yunxin.kit.corekit.XKitService
import com.netease.yunxin.kit.corekit.startup.Initializer

@Keep
class OneOnOneXKitService(override val appKey: String?) : XKitService {
    companion object {
        const val IS_IN_THE_CALL_METHOD = "isInTheCall"
    }

    override val serviceName: String
        get() = "OneOnOne"

    override val versionName: String
        get() = BuildConfig.versionName

    override fun onMethodCall(method: String, param: Map<String, Any?>?): Any? {
        if (IS_IN_THE_CALL_METHOD == method) {
            return CallKitUtil.isInTheCall()
        }
        return null
    }

    override fun create(context: Context): OneOnOneXKitService {
        // expose send team tip method
        @Suppress("UNCHECKED_CAST")
        return this
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
