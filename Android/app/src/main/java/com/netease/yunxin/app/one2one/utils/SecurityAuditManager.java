/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.utils;

import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.passthrough.PassthroughServiceObserve;
import com.netease.nimlib.sdk.passthrough.model.PassthroughNotifyData;
import com.netease.yunxin.app.one2one.utils.security.SecurityAuditModel;

/**
 * 安全通审核管理类
 */
public class SecurityAuditManager {
    private static final String TAG = "SecurityAuditManager";
    /**
     * 3000表示实时音视频安全通审核通过
     */
    public static final int NORMAL_TYPE = 3000;
    private final Gson gson = new Gson();
    private SecurityAuditCallback callback;
    Observer<PassthroughNotifyData> p2pMessage = new Observer<PassthroughNotifyData>() {
        @Override
        public void onEvent(PassthroughNotifyData passthroughNotifyData) {
            LogUtil.i(TAG, Build.BOARD +",passthroughNotifyData:" + passthroughNotifyData.getBody());
            try {
                SecurityAuditModel securityAuditModel = gson.fromJson(passthroughNotifyData.getBody(), SecurityAuditModel.class);
                if (callback != null && securityAuditModel != null) {
                    callback.callback(securityAuditModel);
                } else {
                    LogUtil.e(TAG, "SecurityAuditManager parse msg error");
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                LogUtil.e(TAG, "SecurityAuditManager parse msg error,e:" + e);
            }
        }
    };

    public void startAudit(SecurityAuditCallback callback) {
        this.callback = callback;
        NIMClient.getService(PassthroughServiceObserve.class)
                .observePassthroughNotify(p2pMessage, true);
    }

    public void stopAudit() {
        NIMClient.getService(PassthroughServiceObserve.class)
                .observePassthroughNotify(p2pMessage, false);
    }


    public interface SecurityAuditCallback {
        void callback(SecurityAuditModel securityAuditModel);
    }

}
