// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;

public class OneOnOneUI {

  private static final String TAG = "OneOnOneUI";

  @SuppressLint("StaticFieldLeak")
  private static volatile OneOnOneUI instance;

  private Context context;

  private boolean isChineseEnv;

  private String appKey;

  private String baseUrl;

  private OneOnOneUI() {}

  public static OneOnOneUI getInstance() {
    if (instance == null) {
      synchronized (OneOnOneUI.class) {
        if (instance == null) {
          instance = new OneOnOneUI();
        }
      }
    }
    return instance;
  }

  public void initialize(Context context, String baseUrl, String appKey) {
    this.context = context;
    this.appKey = appKey;
    this.baseUrl = baseUrl;
    HttpService.getInstance().initialize(context, baseUrl);
    HttpService.getInstance().addHeader("appKey", appKey);
    String language = context.getResources().getConfiguration().locale.getLanguage();
    if (!language.contains("zh")) {
      HttpService.getInstance().addHeader("lang", "en");
    }
  }

  public void addHttpHeader(String accessToken, String accountId) {
    HttpService.getInstance().addHeader("accessToken", accessToken);
    HttpService.getInstance().addHeader("accountId", accountId);
  }

  public void setChineseEnv(boolean chineseEnv) {
    isChineseEnv = chineseEnv;
  }

  public boolean isChineseEnv() {
    return isChineseEnv;
  }

  public String getAppKey() {
    return appKey;
  }

  public String getBaseUrl() {
    return baseUrl;
  }
}
