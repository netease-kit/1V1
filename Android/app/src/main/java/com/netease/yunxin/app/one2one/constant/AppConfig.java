// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.constant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.Locale;

public class AppConfig {
  // 请填写您的appKey,如果您的APP是国内环境，请填写APP_KEY_MAINLAND，如果是海外环境，请填写APP_KEY_OVERSEA
  public static final String ONLINE_APP_KEY = "your mainland appKey";// 国内用户填写
  public static final String OVERSEA_APP_KEY = "your oversea appKey";// 海外用户填写
  public static final int PARENT_SCOPE = 1;
  public static final int SCOPE = 10;
  public static String BASE_URL = getBaseUrl();

  @SuppressLint("StaticFieldLeak")
  private static Context sContext;

  private static SharedPreferences sp;
  private static DataCenter sDataCenter;
  private static final String KEY_DATA_CENTER = "DATA_CENTER";
  private static final String ZH = "zh";

  public static void init(Context context) {
    if (sContext == null) {
      sContext = context.getApplicationContext();
      sp = sContext.getSharedPreferences("app_config", Context.MODE_PRIVATE);
    }
  }

  public static boolean isOversea() {
    return AppConfig.getDataCenter() == DataCenter.Oversea;
  }

  public static String getAppKey() {
    if (isOversea()) {
      return OVERSEA_APP_KEY;
    }
    return ONLINE_APP_KEY;
  }

  private static String getBaseUrl() {
    return "http://yiyong.netease.im";
  }

  public static final String MAIN_PAGE_ACTION = "https://netease.yunxin.newlive.home";
  public static final String PRIVACY_POLICY_ZH = "http://yunxin.163.com/clauses?serviceType=3";
  public static final String USER_AGREEMENT_ZH = "http://yunxin.163.com/clauses";
  public static final String PRIVACY_POLICY_EN = "https://commsease.com/en/clauses?serviceType=3";
  public static final String USER_AGREEMENT_EN = "https://commsease.com/en/clauses?serviceType=0";

  public static final float DEFAULT_FILTER_LEVEL = 0.7f;

  public static DataCenter getDataCenter() {
    if (sDataCenter == null) {
      int index = sp.getInt(KEY_DATA_CENTER, DataCenter.MainLand.ordinal());
      sDataCenter = DataCenter.values()[index];
    }
    return sDataCenter;
  }

  public static void setDataCenter(DataCenter dataCenter) {
    if (sDataCenter != dataCenter) {
      sDataCenter = dataCenter;
      sp.edit().putInt(KEY_DATA_CENTER, dataCenter.ordinal()).commit();
    }
  }

  public static boolean isChineseEnv() {
    return isChineseLanguage() && !isOversea();
  }

  public static boolean isChineseLanguage() {
    return Locale.getDefault().getLanguage().contains(ZH);
  }

  public static String getPrivacyPolicyUrl() {
    if (isChineseLanguage()) {
      return PRIVACY_POLICY_ZH;
    } else {
      return PRIVACY_POLICY_EN;
    }
  }

  public static String getUserAgreementUrl() {
    if (isChineseLanguage()) {
      return USER_AGREEMENT_ZH;
    } else {
      return USER_AGREEMENT_EN;
    }
  }

  public enum DataCenter {
    MainLand,
    Oversea,
  }
}
