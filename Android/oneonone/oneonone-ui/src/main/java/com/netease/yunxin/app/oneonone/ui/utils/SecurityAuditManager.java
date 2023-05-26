// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import android.os.Build;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.yunxin.app.oneonone.ui.utils.security.SecurityAuditModel;

/** 安全通审核管理类 */
public class SecurityAuditManager {
  private static final String TAG = "SecurityAuditManager";
  /** 3000表示实时音视频安全通审核通过 */
  public static final int NORMAL_TYPE = 3000;

  private final Gson gson = new Gson();
  private SecurityAuditCallback callback;
  private final Observer<CustomNotification> customNotificationObserver =
      notification -> {
        LogUtil.i(TAG, Build.BOARD + ",customNotificationObserver:" + notification.getContent());
        try {
          SecurityAuditModel securityAuditModel =
              gson.fromJson(notification.getContent(), SecurityAuditModel.class);
          if (callback != null && securityAuditModel != null) {
            callback.callback(securityAuditModel);
          } else {
            LogUtil.e(TAG, "SecurityAuditManager parse msg error");
          }
        } catch (JsonSyntaxException e) {
          e.printStackTrace();
          LogUtil.e(TAG, "SecurityAuditManager parse msg error,e:" + e);
        }
      };

  public void startAudit(SecurityAuditCallback callback) {
    LogUtil.i(TAG, Build.BOARD + ",startAudit");
    this.callback = callback;
    NIMClient.getService(MsgServiceObserve.class)
        .observeCustomNotification(customNotificationObserver, true);
  }

  public void stopAudit() {
    LogUtil.i(TAG, Build.BOARD + ",stopAudit");
    NIMClient.getService(MsgServiceObserve.class)
        .observeCustomNotification(customNotificationObserver, false);
  }

  public interface SecurityAuditCallback {
    void callback(SecurityAuditModel securityAuditModel);
  }
}
