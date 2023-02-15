// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.utils;

import android.content.Context;
import com.blankj.utilcode.util.SPUtils;
import com.netease.lava.nertc.foreground.ForegroundKit;
import com.netease.yunxin.app.one2one.constant.AppConfig;
import com.netease.yunxin.app.one2one.constant.ErrorCode;

public class HighKeepAliveUtil {
  private static final String KEY = "HIGH_KEEP_ALIVE_KEY";
  private static final int BACKGROUND_TIME = 10000;

  public static boolean isHighKeepAliveOpen() {
    return SPUtils.getInstance().getBoolean(KEY, false);
  }

  public static int openHighKeepAlive(Context context) {
    Context appContext = context.getApplicationContext();
    if (ForegroundKit.getInstance(appContext).checkNotifySetting()) {
      ForegroundKit instance = ForegroundKit.getInstance(appContext);
      int result = instance.init(AppConfig.getAppKey(), BACKGROUND_TIME);
      if (result == ErrorCode.SUCCESS) {
        SPUtils.getInstance().put(KEY, true);
      }
      return result;
    }
    return ErrorCode.ERROR;
  }

  public static void closeHighKeepAlive(Context context) {
    SPUtils.getInstance().put(KEY, false);
    ForegroundKit.getInstance(context.getApplicationContext()).release();
  }

  public static void requestNotifyPermission(Context context) {
    ForegroundKit.getInstance(context.getApplicationContext()).requestNotifyPermission();
  }
}
