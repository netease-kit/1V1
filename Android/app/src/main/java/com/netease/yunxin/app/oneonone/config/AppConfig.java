// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.config;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.Locale;

public class AppConfig {
  // 请填写您的appKey,如果您的APP是国内环境，请填写APP_KEY_MAINLAND，如果是海外环境，请填写APP_KEY_OVERSEA
  private static final String APP_KEY_MAINLAND = "your mainland appKey"; // 国内用户填写
  private static final String APP_KEY_OVERSEA = "your oversea appKey"; // 海外用户填写
  public static final boolean IS_OVERSEA= false; // 海外用户填ture,国内用户填false

  // 生成账号参考1对1服务端工程
  public static final String IM_ACCID = "your im accid";
  public static final String IM_TOKEN = "your im token";
  public static final String IM_AVATAR = "your im avatar";
  public static final String IM_NICKNAME = "your im nickname";
  public static final String PHONE_NUMBER = "your phone number";
  public static final String USER_TOKEN = "your user token";
  // 跑通Server Demo后，替换为实际的host
  public static final String BASE_URL="";

  private static final String ZH = "zh";

  @SuppressLint("StaticFieldLeak")
  private static Context sContext;

  public static void init(Context context) {
    if (sContext == null) {
      sContext = context.getApplicationContext();
    }
  }

  public static String getAppKey() {
    if (isOversea()) {
      return APP_KEY_OVERSEA;
    } else {
      return APP_KEY_MAINLAND;
    }
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
