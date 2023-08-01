// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.config;

import android.annotation.SuppressLint;
import android.content.Context;
import java.util.Locale;

public class AppConfig {
  // 请填写您的AppKey和AppSecret
  private static final String APP_KEY = "your AppKey"; // 填入您的AppKey,可在云信控制台AppKey管理处获取
  public static final String APP_SECRET = "your AppSecret"; // 填入您的AppSecret,可在云信控制台AppKey管理处获取
  public static final boolean IS_OVERSEA = false; // 海外用户填ture,国内用户填false
  /**
   * BASE_URL为服务端地址,请在跑通Server Demo(https://github.com/netease-kit/nemo)后，替换为您自己实际的服务端地址
   * "http://yiyong.netease.im/"仅用于跑通体验Demo,请勿用于正式产品上线
   */
  public static final String BASE_URL = "http://yiyong.netease.im/";

  private static final String ZH = "zh";

  @SuppressLint("StaticFieldLeak")
  private static Context sContext;

  public static void init(Context context) {
    if (sContext == null) {
      sContext = context.getApplicationContext();
    }
  }

  public static String getAppKey() {
    return APP_KEY;
  }

  public static boolean isOversea() {
    return false;
  }

  public static boolean isChineseEnv() {
    return isChineseLanguage() && !isOversea();
  }

  public static boolean isChineseLanguage() {
    return Locale.getDefault().getLanguage().contains(ZH);
  }

  public static String getOneOnOneBaseUrl() {
    return BASE_URL;
  }
}
